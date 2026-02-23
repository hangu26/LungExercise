package kr.daejeonuinversity.lungexercise.util.util

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.IOException
import java.util.UUID
import kotlin.math.pow

object MaskBluetoothManager {

    interface BreathingEventListener {
        fun onExhaleStart()
        // 기존 인터페이스에 폐기능 결과값 추가
        fun onExhaleEnd(durationMs: Long, fvc: Double, fev1: Double, ratio: Double)
    }

    private var breathingEventListener: BreathingEventListener? = null

    fun setBreathingEventListener(listener: BreathingEventListener) {
        breathingEventListener = listener
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null

    private val deviceName = "MASK7"
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID

    private var isConnected = false
    val isConnectedPublic: Boolean
        get() = isConnected

    private var connectedDeviceName: String? = null
    val connectedDeviceNamePublic: String?
        get() = connectedDeviceName

    private var receiveThread: Thread? = null
    private var pollingThread: Thread? = null // 변수명 명시적 변경

    private val baselineSamples = mutableListOf<Double>()
    private val baselineSampleCountLimit = 15
    private var baseline: Double? = null

    private val sensorThreshold = 0.1 // 편차 임계치

    private var isInhaling = false
    private var inhaleStartTime = 0L

    var isExhaling = false
    private var exhaleStartTime = 0L

    // [추가] 폐기능 계산용 변수
    private val currentExhaleSamples = mutableListOf<Double>()
    private const val MASK_AREA_M2 = 0.0005 // 단면적 (사용자 환경에 맞게 조정 가능)

    interface ConnectCallback {
        fun onDeviceFound(deviceName: String)
        fun onDeviceNotFound(deviceName: String)
        fun onConnectSuccess()
        fun onConnectFailed(reason: String)
    }

    var connectCallback: ConnectCallback? = null

    fun connectToDevice(context: Context, deviceName: String, connectImmediately: Boolean) {
        Thread {
            if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
                Log.d("마스크 응답", "블루투스 어댑터 없음 또는 비활성")
                connectCallback?.onConnectFailed("블루투스 어댑터 없음 또는 비활성")
                return@Thread
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("마스크 응답", "BLUETOOTH_CONNECT 권한 없음")
                    connectCallback?.onConnectFailed("블루투스 권한이 없습니다")
                    return@Thread
                }
            }

            val device = bluetoothAdapter.bondedDevices.firstOrNull { it.name == deviceName }

            if (device == null) {
                Log.d("마스크 응답", "연결할 디바이스($deviceName) 미발견")
                connectCallback?.onDeviceNotFound(deviceName)
                return@Thread
            } else {
                connectCallback?.onDeviceFound(device.name)
            }

            if (connectImmediately) {
                connecting(device, context)
            }
        }.start()
    }

    @SuppressLint("CommitPrefEdits")
    private fun connecting(device: BluetoothDevice, context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                    return
                }
            }
            bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothSocket?.connect()
            isConnected = true
            connectedDeviceName = device.name
            Log.d("마스크 응답", "블루투스 연결 성공")
            connectCallback?.onConnectSuccess()

            sendBoardDataRequest()
            startReceivingData()
            startPolling()
        } catch (e: IOException) {
            Log.d("마스크 응답", "블루투스 연결 실패: ${e.message}")
            isConnected = false
            connectCallback?.onConnectFailed("블루투스 연결 실패: ${e.message}")
        }
    }

    private fun startPolling() {
        pollingThread = Thread {
            while (isConnected) {
                sendBoardDataRequest()
                // FEV1 계산을 위해 샘플링 주기를 30ms로 상향 (기존 1000ms는 너무 느림)
                Thread.sleep(30)
            }
        }
        pollingThread?.start()
    }

    private fun sendBoardDataRequest() {
        val command = byteArrayOf(
            0xf0.toByte(), 0x01, 0x00, 0x00, 0x00,
            0x00, 0x00, 0x00, 0x00, 0x00, 0x0d
        )
        try {
            bluetoothSocket?.outputStream?.write(command)
        } catch (e: IOException) {
            Log.e("마스크 응답", "명령어 전송 실패: ${e.message}")
        }
    }

    private fun startReceivingData() {
        receiveThread = Thread {
            try {
                val inputStream = bluetoothSocket?.inputStream ?: return@Thread
                val buffer = ByteArray(1024)
                var bytes: Int
                val readBuffer = mutableListOf<Byte>()

                while (isConnected) {
                    bytes = inputStream.read(buffer)
                    if (bytes <= 0) continue

                    for (i in 0 until bytes) {
                        readBuffer.add(buffer[i])
                    }

                    while (readBuffer.size >= 11) {
                        if (readBuffer[0] == 0xF0.toByte() && readBuffer[10] == 0x0D.toByte()) {
                            val packet = readBuffer.subList(0, 11).toList()
                            handlePacket(packet)
                            repeat(11) { readBuffer.removeAt(0) }
                        } else {
                            readBuffer.removeAt(0)
                        }
                    }
                }
            } catch (e: IOException) {
                Log.e("마스크 응답", "데이터 수신 실패: ${e.message}")
                isConnected = false
            }
        }
        receiveThread?.start()
    }

    private fun handlePacket(packet: List<Byte>) {
        val dataType = packet[1].toInt() and 0xFF
        if (dataType != 0x01) return

        val airflow1 = (packet[3].toInt() and 0xFF) * 256 + (packet[4].toInt() and 0xFF)
        val airflow2 = (packet[5].toInt() and 0xFF) * 256 + (packet[6].toInt() and 0xFF)
        val airflow3 = (packet[7].toInt() and 0xFF) * 256 + (packet[8].toInt() and 0xFF)

        val airflowValues = listOf(airflow1.toDouble(), airflow2.toDouble(), airflow3.toDouble())
        val avgFlow = airflowValues.average()

        logDynamicPressure(avgFlow)

        if (baseline == null) {
            baselineSamples.add(avgFlow)
            if (baselineSamples.size >= baselineSampleCountLimit) {
                baseline = baselineSamples.average()
                Log.d("마스크 응답", "baseline 설정 완료! 값: $baseline")
            }
            return
        }

        val deviation = avgFlow - baseline!!
        Log.d("마스크 응답", "airflow 평균: $avgFlow, baseline: $baseline, 편차: $deviation")

        // 들이마심 감지 (기존 유지)
        if (!isInhaling && deviation > sensorThreshold) {
            isInhaling = true
            inhaleStartTime = System.currentTimeMillis()
            Log.d("숨 감지", "들이마심 감지! airflow=$avgFlow, 편차=$deviation")
        }

        if (isInhaling && deviation <= sensorThreshold) {
            isInhaling = false
            val inhaleEndTime = System.currentTimeMillis()
            val durationSec = (inhaleEndTime - inhaleStartTime) / 1000.0
            Log.d("숨 감지", "들이마심 종료, 지속시간: $durationSec 초")
        }

        // 내쉬기 감지 (기존 유지 + 데이터 수집 추가)
        if (!isExhaling && deviation < -sensorThreshold) {
            isExhaling = true
            exhaleStartTime = System.currentTimeMillis()
            currentExhaleSamples.clear() // 샘플 초기화
            Log.d("숨 감지", "내쉬는 중 감지! airflow=$avgFlow, 편차=$deviation")
            breathingEventListener?.onExhaleStart()
        }

        if (isExhaling) {
            // 내쉬는 동안 유량 샘플을 지속적으로 리스트에 추가
            currentExhaleSamples.add(calculateFlowLps(avgFlow))

            if (deviation >= -sensorThreshold) {
                isExhaling = false
                val exhaleDuration = System.currentTimeMillis() - exhaleStartTime
                Log.d("숨 감지", "내쉬기 종료, 지속시간: ${exhaleDuration / 1000.0} 초")

                // 폐기능 검사 결과 계산 및 로그 출력
                processLungFunctionData(exhaleDuration)
            }
        }
    }

    private fun calculateFlowLps(raw: Double): Double {
        val idleRaw = 21761.0
        val minRaw = 21000.0
        val maxVelocity = 15.0
        var v = ((idleRaw - raw) / (idleRaw - minRaw)) * maxVelocity
        v = v.coerceIn(0.0, maxVelocity)
        return (MASK_AREA_M2 * v) * 1000.0 // L/s
    }

    private fun processLungFunctionData(durationMs: Long) {
        if (currentExhaleSamples.isEmpty()) return
        val dt = (durationMs / 1000.0) / currentExhaleSamples.size
        val fvc = currentExhaleSamples.sum() * dt
        val samplesInOneSec = (1.0 / dt).toInt()
        val fev1 = currentExhaleSamples.take(samplesInOneSec).sum() * dt
        val ratio = if (fvc > 0) (fev1 / fvc) * 100 else 0.0

        // 요청하신 전용 로그 추가
        Log.d("폐기능검사", "================================")
        Log.d("폐기능검사", "FVC(L): ${"%.2f".format(fvc)}")
        Log.d("폐기능검사", "FEV1(L): ${"%.2f".format(fev1)}")
        Log.d("폐기능검사", "FEV1/FVC(%%): ${"%.1f".format(ratio)}%")
        Log.d("폐기능검사", "================================")

        breathingEventListener?.onExhaleEnd(durationMs, fvc, fev1, ratio)
    }

    private fun logDynamicPressure(rawVelocity: Double) {
        val idleRaw = 21761.0
        val minRaw = 21000.0
        val maxVelocity = 15.0
        val rho = 1.18
        var v = ((idleRaw - rawVelocity) / (idleRaw - minRaw)) * maxVelocity
        v = v.coerceIn(0.0, maxVelocity)
        val pressure = 0.5 * rho * v.pow(2.0)
        Log.d("PressureSensor", "Raw: ${rawVelocity.toInt()} -> 속도: ${String.format("%.2f", v)} m/s -> 압력: ${String.format("%.2f", pressure)} Pa")
    }

    fun disconnect() {
        isConnected = false
        receiveThread?.interrupt()
        pollingThread?.interrupt()
        try {
            bluetoothSocket?.close()
        } catch (_: IOException) {}
        bluetoothSocket = null
    }
}

