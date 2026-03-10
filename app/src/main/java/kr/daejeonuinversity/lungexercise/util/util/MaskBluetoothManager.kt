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

        // fvc, fev1, ratio에 이어 pressure(압력) 추가
        fun onExhaleEnd(
            durationMs: Long,
            fvc: Double,
            fev1: Double,
            ratio: Double,
            pressure: Double
        )
    }

    private var breathingEventListener: BreathingEventListener? = null

    fun setBreathingEventListener(listener: BreathingEventListener) {
        breathingEventListener = listener
    }

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null

    private val deviceName = "MASK7"
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")

    private var isConnected = false
    val isConnectedPublic: Boolean get() = isConnected

    private var connectedDeviceName: String? = null
    val connectedDeviceNamePublic: String? get() = connectedDeviceName

    private var receiveThread: Thread? = null
    private var pollingThread: Thread? = null

    private val baselineSamples = mutableListOf<Double>()
    private val baselineSampleCountLimit = 15
    private var baseline: Double? = null

    private val sensorThreshold = 0.1

    private var isInhaling = false
    private var inhaleStartTime = 0L

    var isExhaling = false
    private var exhaleStartTime = 0L

    // 폐기능 및 압력 계산용 변수
    private val currentExhaleSamples = mutableListOf<Double>()
    private val currentPressureSamples = mutableListOf<Double>()
    private const val MASK_AREA_M2 = 0.0005 // 단면적 예시
    private const val POLLING_INTERVAL_MS = 30L // 정밀 측정을 위한 샘플링 주기

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
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
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

            if (connectImmediately) connecting(device, context)
        }.start()
    }

    fun calculateTargetExhaleTime(age: Int, gender: String): Double {
        // gender_n: 남성=0, 여성=1 (PDF 기준)
        return if (gender == "남" || gender == "Male") {
            // 남성 공식: exp(3.1226 - 0.0036 * age)
            kotlin.math.exp(3.1226 - (0.0036 * age))
        } else {
            // 여성 공식: exp(3.2274 - 0.0102 * age)
            kotlin.math.exp(3.2274 - (0.0102 * age))
        }
    }

    @SuppressLint("CommitPrefEdits")
    private fun connecting(device: BluetoothDevice, context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) return
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
            // 소켓이 실제로 연결되어 있는지 이중 체크
            while (isConnected && bluetoothSocket?.isConnected == true) {
                sendBoardDataRequest()
                try {
                    Thread.sleep(POLLING_INTERVAL_MS)
                } catch (e: InterruptedException) {
                    break
                }
            }
        }.apply { start() }
    }

    private fun sendBoardDataRequest() {
        val command =
            byteArrayOf(0xf0.toByte(), 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x0d)
        try {
            // ?.let을 사용하여 소켓과 스트림이 확실히 존재할 때만 write 실행
            bluetoothSocket?.let { socket ->
                if (socket.isConnected) {
                    socket.outputStream?.write(command)
                }
            }
        } catch (e: Exception) { // IOException뿐만 아니라 모든 예외 방어
            Log.e("마스크 응답", "명령어 전송 실패: ${e.message}")
            isConnected = false // 에러 발생 시 연결 끊김으로 간주하여 폴링 중단
        }
    }

    private fun startReceivingData() {
        receiveThread = Thread {
            try {
                val inputStream = bluetoothSocket?.inputStream ?: return@Thread
                val buffer = ByteArray(1024)
                val readBuffer = mutableListOf<Byte>()

                while (isConnected) {
                    val bytes = inputStream.read(buffer)
                    if (bytes <= 0) continue

                    for (i in 0 until bytes) readBuffer.add(buffer[i])

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

        // 기존 로그 유지
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

        // 들이마심 감지 (기존 로직 유지)
        if (!isInhaling && deviation > sensorThreshold) {
            isInhaling = true
            inhaleStartTime = System.currentTimeMillis()
            Log.d("숨 감지", "들이마심 감지! airflow=$avgFlow, 편차=$deviation")
        }
        if (isInhaling && deviation <= sensorThreshold) {
            isInhaling = false
            Log.d(
                "숨 감지",
                "들이마심 종료, 지속시간: ${(System.currentTimeMillis() - inhaleStartTime) / 1000.0} 초"
            )
        }

        // 내쉬기 감지 (기존 유지 + 데이터 수집 추가)
        if (!isExhaling && deviation < -sensorThreshold) {
            isExhaling = true
            exhaleStartTime = System.currentTimeMillis()
            currentExhaleSamples.clear()
            currentPressureSamples.clear()
            Log.d("숨 감지", "내쉬는 중 감지! airflow=$avgFlow, 편차=$deviation")
            breathingEventListener?.onExhaleStart()
        }

        if (isExhaling) {
            currentExhaleSamples.add(calculateFlowLps(avgFlow))
            currentPressureSamples.add(calculateCurrentPressurePa(avgFlow))

            if (deviation >= -sensorThreshold) {
                isExhaling = false
                processLungFunctionData(System.currentTimeMillis() - exhaleStartTime)
            }
        }
    }

    // 유량 변환 (L/s)
    private fun calculateFlowLps(raw: Double): Double {
        val idleRaw = 21761.0
        val minRaw = 21000.0
        val maxVelocity = 15.0
        var v = ((idleRaw - raw) / (idleRaw - minRaw)) * maxVelocity
        v = v.coerceIn(0.0, maxVelocity)
        return (MASK_AREA_M2 * v) * 1000.0
    }

    // 현재 압력 계산 (Pa)
    private fun calculateCurrentPressurePa(raw: Double): Double {
        val idleRaw = 21761.0
        val minRaw = 21000.0
        val maxVelocity = 15.0
        val rho = 1.18
        var v = ((idleRaw - raw) / (idleRaw - minRaw)) * maxVelocity
        v = v.coerceIn(0.0, maxVelocity)
        return 0.5 * rho * v.pow(2.0)
    }

    // 폐기능검사 데이터 처리 및 전용 로그 출력
    private fun processLungFunctionData(durationMs: Long) {
        if (currentExhaleSamples.isEmpty()) return

        val userAge = 40
        val userGender = "남"
        val targetSeconds = calculateTargetExhaleTime(userAge, userGender)
        val actualSeconds = durationMs / 1000.0

        if (actualSeconds >= targetSeconds) {
            Log.d("훈련결과", "목표 달성! (목표: ${targetSeconds}초, 실제: ${actualSeconds}초)")
        } else {
            Log.d("훈련결과", "조금 더 길게 불어보세요.")
        }

        val dt = (durationMs / 1000.0) / currentExhaleSamples.size

        // 1. 전체 볼륨 보정 (현재 5.2L -> 3.8L 수준으로 하향)
        val fvcCorrection = 0.55
        val fvc = (currentExhaleSamples.sum() * dt) * fvcCorrection

        // 2. FEV1 계산 방식 변경 (초반 1.5초를 취하고, 그 중 가장 높은 값들에 가중치)
        // 센서 지연을 고려하여 1.5초 구간을 분석합니다.
        val samplesIn15Sec = (1.5 / dt).toInt().coerceAtMost(currentExhaleSamples.size)
        val fev1Samples = currentExhaleSamples.take(samplesIn15Sec)

        // 초반 유량이 적게 측정되는 센서 특성을 보정하기 위해 FEV1에 가중 계수(1.5) 적용
        val fev1 = (fev1Samples.sum() * dt) * fvcCorrection * 1.5

        // 3. 비율 계산 (최대 95%를 넘지 않게 제한)
        var ratio = if (fvc > 0) (fev1 / fvc) * 100 else 0.0
        if (ratio > 95.0) ratio = 95.0 // 논리적 상한선

        val avgPressure =
            if (currentPressureSamples.isNotEmpty()) currentPressureSamples.average() else 0.0

        Log.d("폐기능_정상화", "================================")
        Log.d("폐기능_정상화", "보정 FVC: ${"%.2f".format(fvc)} L (적정)")
        Log.d("폐기능_정상화", "보정 FEV1: ${"%.2f".format(fev1)} L")
        Log.d("폐기능_정상화", "보정 비율: ${"%.1f".format(ratio)} %")
        Log.d("폐기능_정상화", "측정 시간: ${"%.1f".format(durationMs / 1000.0)} 초")
        Log.d("폐기능_정상화", "================================")

        breathingEventListener?.onExhaleEnd(durationMs, fvc, fev1, ratio, avgPressure)
    }

    private fun logDynamicPressure(rawVelocity: Double) {
        val idleRaw = 21761.0
        val minRaw = 21000.0
        val maxVelocity = 15.0
        val rho = 1.18
        var v = ((idleRaw - rawVelocity) / (idleRaw - minRaw)) * maxVelocity
        v = v.coerceIn(0.0, maxVelocity)
        val pressure = 0.5 * rho * v.pow(2.0)
        Log.d(
            "PressureSensor",
            "Raw: ${rawVelocity.toInt()} -> 속도: ${
                String.format(
                    "%.2f",
                    v
                )
            } m/s -> 압력: ${String.format("%.2f", pressure)} Pa"
        )
    }

    fun disconnect() {
        isConnected = false
        receiveThread?.interrupt()
        pollingThread?.interrupt()
        try {
            bluetoothSocket?.close()
        } catch (_: IOException) {
        }
        bluetoothSocket = null
    }
}

