package kr.daejeonuinversity.lungexercise.view.breathing

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityTestBreathBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.exercise.LungExerciseActivity
import kr.daejeonuinversity.lungexercise.view.main.MainActivity

class TestBreathActivity :
    BaseActivity<ActivityTestBreathBinding>(R.layout.activity_test_breath) {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false

    private val RECORD_AUDIO_PERMISSION = Manifest.permission.RECORD_AUDIO
    private val PERMISSION_REQUEST_CODE = 1000
    private val backPressedCallback = BackPressedCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            activity = this@TestBreathActivity
            lifecycleOwner = this@TestBreathActivity
        }

        checkAudioPermission()

        // 버튼 클릭 확인 로그
        binding.btnStart.setOnClickListener {
            Log.e("마이크 입력", "시작 버튼 눌림!")
            startListening()
        }

        binding.btnStop02.setOnClickListener {
            Log.e("마이크 입력", "중지 버튼 눌림!")
            stopListening()
        }

        binding.btnBack.setOnClickListener {
            val intent = Intent(this@TestBreathActivity, MainActivity::class.java)
            startActivityAnimation(intent, this@TestBreathActivity)
        }

        backPressedCallback.addCallbackActivity(this, MainActivity::class.java)

    }

    /** 권한 체크 **/
    private fun checkAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, RECORD_AUDIO_PERMISSION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(RECORD_AUDIO_PERMISSION),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.e("마이크 입력", "마이크 권한 허용됨")
            } else {
                Log.e("마이크 입력", "마이크 권한 거부됨")
                Toast.makeText(this, "마이크 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    /** 마이크 입력 시작 **/
    private fun startListening() {
        if (isRecording) return

        val sampleRate = 8000
        val bufferSize = AudioRecord.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        audioRecord = AudioRecord(
            MediaRecorder.AudioSource.MIC,
            sampleRate,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            bufferSize
        )

        Log.e("마이크 입력", "state = ${audioRecord?.state}")

        if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
            Log.e("마이크 입력", "초기화 실패")
            return
        }

        audioRecord?.startRecording()
        isRecording = true

        // 마이크 쓰레드
        Thread {
            Log.e("마이크 입력", "쓰레드 시작됨")
            val buffer = ShortArray(bufferSize)
            while (isRecording) {
                val read = audioRecord?.read(buffer, 0, buffer.size) ?: 0
                if (read > 0) {
                    val amplitude = buffer.maxOrNull()?.toInt() ?: 0
                    Log.e("마이크 입력", "read=$read, amplitude=$amplitude")
                }
                Thread.sleep(200)
            }
        }.start()
    }

    /** 마이크 입력 중지 **/
    private fun stopListening() {
        if (!isRecording) return
        isRecording = false
        audioRecord?.stop()
        audioRecord?.release()
        audioRecord = null
        Log.e("마이크 입력", "마이크 중지됨")
    }

    override fun onDestroy() {
        super.onDestroy()
        stopListening()
    }
}



