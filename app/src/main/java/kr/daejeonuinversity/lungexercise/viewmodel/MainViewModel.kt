package kr.daejeonuinversity.lungexercise.viewmodel

import android.Manifest
import android.app.Activity
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.util.Log
import android.view.MotionEvent
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.util.util.HeartRateReceiver
import java.io.IOException
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _goToLungExercise = MutableLiveData<Boolean>()
    val goToLungExercise = _goToLungExercise

    private val _btnHistoryClicked = MutableLiveData<Boolean>()
    val btnHistoryClicked = _btnHistoryClicked


    fun btnLungExerciseDetail() {
        goToLungExercise.value = true
    }

    fun btnHistory() {
        _btnHistoryClicked.value = true
    }

    private val _breathData = MutableLiveData<String>()
    val breathData: LiveData<String> = _breathData

    private val bluetoothAdapter: BluetoothAdapter? = BluetoothAdapter.getDefaultAdapter()
    private var bluetoothSocket: BluetoothSocket? = null

    private val deviceName = "MASK7"
    private val devicePin = "BTWIN"
    private val uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // SPP UUID

    private var isConnected = false
    private var receiveThread: Thread? = null

    private val baselineSamples = mutableListOf<Double>()
    private val baselineSampleCountLimit = 10
    private var baseline: Double? = null

    private val sensorThreshold = 0.5 // 편차 임계치

    // 들숨 상태 변수
    private var isInhaling = false
    private var inhaleStartTime = 0L

    // 날숨 상태 변수
    private var isExhaling = false
    private var exhaleStartTime = 0L

    fun connectToDevice() {
        viewModelScope.launch(Dispatchers.IO) {
            Log.d("마스크 응답", "connectToDevice 호출됨 (Background)")

            if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled) {
                Log.d("마스크 응답", "블루투스 어댑터 없음 또는 비활성")
                return@launch
            }

            val device = if (ActivityCompat.checkSelfPermission(
                    getApplication(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.d("마스크 응답", "BLUETOOTH_CONNECT 권한 없음")
                return@launch
            } else {
                bluetoothAdapter.bondedDevices.firstOrNull { it.name == deviceName }
            }

            if (device == null) {
                Log.d("마스크 응답", "연결할 디바이스($deviceName) 미발견")
                return@launch
            }

            try {
                bluetoothSocket = device.createRfcommSocketToServiceRecord(uuid)
                bluetoothSocket?.connect()
                isConnected = true
                Log.d("마스크 응답", "블루투스 연결 성공")
                sendBoardDataRequest()
                startReceivingData()
                startPolling()
            } catch (e: IOException) {
                e.printStackTrace()
                Log.d("마스크 응답", "블루투스 연결 실패: ${e.message}")
                isConnected = false
            }
        }
    }

    private fun startPolling() {
        viewModelScope.launch(Dispatchers.IO) {
            while (isConnected) {
                sendBoardDataRequest()
                delay(1000)  // 1초마다 명령어 전송
            }
        }
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

                val readBuffer = mutableListOf<Byte>() // 누적 버퍼

                while (isConnected) {
                    Log.d("마스크 응답", "read() 대기 중…")
                    bytes = inputStream.read(buffer)
                    Log.d("마스크 응답", "read() 완료: $bytes 바이트")

                    if (bytes <= 0) continue

                    // 받은 바이트들을 누적 버퍼에 추가
                    for (i in 0 until bytes) {
                        readBuffer.add(buffer[i])
                    }

                    // 11바이트 이상 쌓였을 때 패킷 처리 시도
                    while (readBuffer.size >= 11) {
                        if (readBuffer[0] == 0xF0.toByte() && readBuffer[10] == 0x0D.toByte()) {
                            val packet = readBuffer.subList(0, 11)
                            handlePacket(packet)
                            // 처리한 패킷 제거
                            repeat(11) { readBuffer.removeAt(0) }
                        } else {
                            // 패킷 시작/끝이 맞지 않으면 한 바이트 버림 후 재검사
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

        val airflow1 = (packet[3].toInt() and 0xFF) * 256 + (packet[4].toInt() and 0xFF)  // MSB, LSB 조합
        val airflow2 = (packet[5].toInt() and 0xFF) * 256 + (packet[6].toInt() and 0xFF)
        val airflow3 = (packet[7].toInt() and 0xFF) * 256 + (packet[8].toInt() and 0xFF)

        val airflowValues = listOf(airflow1, airflow2, airflow3)
        val avgFlow = airflowValues.average()

        Log.d("마스크 응답", "패킷 처리됨 → airflow 평균: $avgFlow")
        Log.d("디버깅", "airflow raw values: $airflowValues")

        if (baseline == null) {
            baselineSamples.add(avgFlow)
            Log.d(
                "마스크 응답",
                "baseline 샘플 수집중: ${baselineSamples.size} / $baselineSampleCountLimit, 값: $avgFlow"
            )

            if (baselineSamples.size >= baselineSampleCountLimit) {
                baseline = baselineSamples.average()
                Log.d("마스크 응답", "baseline 설정 완료! 값: $baseline")
            }
            return
        }

        val deviation = avgFlow - baseline!!
        Log.d("마스크 응답", "airflow 평균: $avgFlow, baseline: $baseline, 편차: $deviation")

        _breathData.postValue("airflow 평균: $avgFlow (편차: $deviation)")

        // 들이마심 감지
        if (!isInhaling && deviation > sensorThreshold) {
            isInhaling = true
            inhaleStartTime = System.currentTimeMillis()
            Log.d("숨 감지", "들이마심 감지! airflow=$avgFlow, 편차=$deviation")
        }

        // 들이마심 종료 감지
        if (isInhaling && deviation <= sensorThreshold) {
            isInhaling = false
            val inhaleEndTime = System.currentTimeMillis()
            val durationSec = (inhaleEndTime - inhaleStartTime) / 1000.0
            _breathData.postValue("들이마심 지속시간: $durationSec 초")
            Log.d("숨 감지", "들이마심 종료, 지속시간: $durationSec 초")
        }

        // 내쉬기 감지
        if (!isExhaling && deviation < -sensorThreshold) {
            isExhaling = true
            exhaleStartTime = System.currentTimeMillis()
            Log.d("숨 감지", "내쉬는 중 감지! airflow=$avgFlow, 편차=$deviation")
        }

        // 내쉬기 종료 감지
        if (isExhaling && deviation >= -sensorThreshold) {
            isExhaling = false
            val exhaleEndTime = System.currentTimeMillis()
            val exhaleDuration = (exhaleEndTime - exhaleStartTime) / 1000.0
            _breathData.postValue("날숨 지속시간: $exhaleDuration 초")
            Log.d("숨 감지", "내쉬기 종료, 지속시간: $exhaleDuration 초")
        }
    }

    fun disconnect() {
        try {
            isConnected = false
            bluetoothSocket?.close()
            receiveThread?.interrupt()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
