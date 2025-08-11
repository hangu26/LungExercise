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

object MaskBluetoothManager {

    interface BreathingEventListener {
        fun onExhaleStart()
        fun onExhaleEnd(durationMs: Long)
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

    // 연결된 기기 이름 저장용 변수 (연결 성공시 저장)
    private var connectedDeviceName: String? = null
    val connectedDeviceNamePublic: String?
        get() = connectedDeviceName

    private var receiveThread: Thread? = null

    private val baselineSamples = mutableListOf<Double>()
    private val baselineSampleCountLimit = 15
    private var baseline: Double? = null

    private val sensorThreshold = 0.1 // 편차 임계치

    private var isInhaling = false
    private var inhaleStartTime = 0L

    var isExhaling = false
    private var exhaleStartTime = 0L

    interface ConnectCallback {
        fun onDeviceFound(deviceName: String)
        fun onDeviceNotFound(deviceName: String)
        fun onConnectSuccess()
        fun onConnectFailed(reason: String)
    }

    var connectCallback: ConnectCallback? = null

    // 연결 및 데이터 수신 함수들 (viewModelScope 못 쓰므로 Thread, CoroutineScope 따로 구현 필요)
    fun connectToDevice(context: Context, deviceName: String, connectImmediately: Boolean) {
        Thread {
            if (bluetoothAdapter == null || !bluetoothAdapter!!.isEnabled) {
                Log.d("마스크 응답", "블루투스 어댑터 없음 또는 비활성")
                connectCallback?.onConnectFailed("블루투스 어댑터 없음 또는 비활성")
                return@Thread
            }

            // Android 12 이상부터 BLUETOOTH_CONNECT 권한 체크
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

            if (connectImmediately) {
                connecting(device, context)
            }
        }.start()
    }

    @SuppressLint("CommitPrefEdits")
    private fun connecting(device: BluetoothDevice, context: Context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        context,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
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
        Thread {
            while (isConnected) {
                sendBoardDataRequest()
                Thread.sleep(1000)
            }
        }.start()
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
                            val packet = readBuffer.subList(0, 11)
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

        val airflowValues = listOf(airflow1, airflow2, airflow3)
        val avgFlow = airflowValues.average()

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

        // 들이마심 감지
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

        // 내쉬기 감지
        if (!isExhaling && deviation < -sensorThreshold) {
            isExhaling = true
            exhaleStartTime = System.currentTimeMillis()
            Log.d("숨 감지", "내쉬는 중 감지! airflow=$avgFlow, 편차=$deviation")

            breathingEventListener?.onExhaleStart()
        }

        if (isExhaling && deviation >= -sensorThreshold) {
            isExhaling = false
            val exhaleDuration = System.currentTimeMillis() - exhaleStartTime
            Log.d("숨 감지", "내쉬기 종료, 지속시간: ${exhaleDuration / 1000.0} 초")

            breathingEventListener?.onExhaleEnd(exhaleDuration)
        }
    }

    fun disconnect() {
        isConnected = false
        receiveThread?.interrupt()
        try {
            bluetoothSocket?.close()
        } catch (_: IOException) {
        }
        bluetoothSocket = null
    }
}

