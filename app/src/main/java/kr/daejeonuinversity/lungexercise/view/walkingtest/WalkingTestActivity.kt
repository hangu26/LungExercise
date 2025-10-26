package kr.daejeonuinversity.lungexercise.view.walkingtest

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.MotionEvent
import android.view.View
import com.google.android.gms.wearable.Wearable
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityWalkingTestBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.util.util.HeartTimerView
import kr.daejeonuinversity.lungexercise.util.util.MiBandReceiver
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.view.walkingtest.result.WalkingResultActivity
import kr.daejeonuinversity.lungexercise.viewmodel.WalkingTestViewModel
import org.koin.android.ext.android.inject
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WalkingTestActivity :
    BaseActivity<ActivityWalkingTestBinding>(R.layout.activity_walking_test) {

    companion object {
        private const val TEST_DURATION_MS = 1 * 60 * 1000L
    }

    private val wViewModel: WalkingTestViewModel by inject()
    private lateinit var heartTimerView: HeartTimerView
    private var countDownTimer: CountDownTimer? = null
    private var totalTime = TEST_DURATION_MS
    private val backPressedCallback = BackPressedCallback(this)
    private var remainingTime: Long = totalTime
    private var isRunning = false
    private var userWeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            activity = this@WalkingTestActivity
            viewmodel = wViewModel
            lifecycleOwner = this@WalkingTestActivity
        }

        heartTimerView = binding.heartTimerView
        sendLaunchSignalToWatch()
        initButton()
        observe()
        backPressedCallback.addCallbackActivity(this, MainActivity::class.java)

    }

    @SuppressLint("ClickableViewAccessibility")
    fun initButton() {
        sendResetMessageToWatch()
        binding.btnStart.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)

            if (event?.action == MotionEvent.ACTION_UP) {

                remainingTime = TEST_DURATION_MS

                heartTimerView.updateProgress(1f, "06:00") // 초기 상태로 리셋

                if (!isRunning) {
                    startTimer() // 처음 실행 또는 pause 후 resume

                    wViewModel.heartRate.observe(this) {
                        binding.txHeartRate.text = "$it"
                    }

                    wViewModel.stepCount.observe(this) {
                        binding.txStepCount.text = "$it"
                    }

                    wViewModel.startReceiving()

                }


            }

            false
        }

        binding.btnStop.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)

            if (event?.action == MotionEvent.ACTION_UP) {

                if (isRunning) {
                    pauseTimer()
                    wViewModel.stopReceiving()
                }


            }

            false
        }

        binding.btnReset.setOnTouchListener { v, event ->

            setTouchAnimation(v, event)

            if (event?.action == MotionEvent.ACTION_UP) {

                stopTimer()
                wViewModel.stopReceiving()
                binding.txStepCount.text = "0"
                binding.txHeartRate.text = "0"
                binding.txDistanceValue.text = "0"

            }

            false

        }

    }

    private fun observe() = wViewModel.let { vm ->

        vm.fetchUserInfo()

        vm.backClicked.observe(this@WalkingTestActivity) {
            if (it) {
                val intent = Intent(this@WalkingTestActivity, MainActivity::class.java)
                startActivityBackAnimation(intent, this@WalkingTestActivity)
                finish()
            }
        }

        vm.btnStartState.observe(this@WalkingTestActivity) {

            if (it) {

                remainingTime = TEST_DURATION_MS

                heartTimerView.updateProgress(1f, "06:00") // 초기 상태로 리셋

                vm.isReset()

                sendResetMessageToWatch()

                binding.btnStart.visibility = View.GONE
                binding.btnResult.visibility = View.GONE
                binding.btnStop.visibility = View.VISIBLE

                sendStartSignalToWatch()

            }

        }

        vm.btnStopState.observe(this@WalkingTestActivity) {

            if (it) {

                binding.btnStart.visibility = View.VISIBLE
                binding.btnResult.visibility = View.VISIBLE
                binding.txStart.text = "다시하기"
                binding.btnStop.visibility = View.GONE
                sendStopMessageToWatch()
                vm.isEnded()

                vm.saveData()

            }

        }

        vm.btnResetState.observe(this@WalkingTestActivity) {

            if (it) {

                binding.btnStart.visibility = View.VISIBLE
                binding.btnStop.visibility = View.GONE
                sendResetMessageToWatch()

                vm.isReset()

            }

        }

        vm.isEndedState.observe(this@WalkingTestActivity) {

            if (it) {

                binding.btnResult.visibility = View.VISIBLE

            }

        }

        vm.btnResultState.observe(this@WalkingTestActivity) {

            if (it) {

                val intent = Intent(this@WalkingTestActivity, WalkingResultActivity::class.java)

                val distance = vm.txWalkDistance.value ?: "0 m"
                val calories = vm.calories.value ?: 0.0
                val steps = vm.stepCount.value ?: 0

                intent.putExtra("distance", distance)
                intent.putExtra("calories", calories)
                intent.putExtra("steps", steps)

                Log.d("칼로리", "넘기는 데이터 -> 거리: $distance, 칼로리: $calories, 걸음 수: $steps")
                sendResetMessageToWatch()
                startActivityAnimation(intent, this@WalkingTestActivity)
                finish()

            }

        }


    }

    /**
     * 기존 방식 워치 통신 함수
    private fun sendStartSignalToWatch() {
    val nodeClient = Wearable.getNodeClient(this)
    val messageClient = Wearable.getMessageClient(this)

    nodeClient.connectedNodes.addOnSuccessListener { nodes ->
    nodes.forEach { node ->
    messageClient.sendMessage(node.id, "/start_heart_rate_service", byteArrayOf())
    .addOnSuccessListener {
    Log.d("PhoneApp", "시작 신호 전송 성공")
    }
    .addOnFailureListener {
    Log.e("PhoneApp", "시작 신호 전송 실패", it)
    }
    }
    }
    }

     **/

    private fun sendLaunchSignalToWatch() {
        val nodeClient = Wearable.getNodeClient(this)
        val messageClient = Wearable.getMessageClient(this)

        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, "/launch_app", null)
                    .addOnSuccessListener {
                        Log.d("PhoneApp", "워치 앱 실행 신호 전송 성공")
                    }
                    .addOnFailureListener {
                        Log.e("PhoneApp", "워치 앱 실행 신호 전송 실패", it)
                    }
            }
        }
    }

    private fun sendStartSignalToWatch() {
        val nodeClient = Wearable.getNodeClient(this)
        val messageClient = Wearable.getMessageClient(this)
        val exerciseTime = 6 * 60 * 1000L

        val payload = ByteBuffer.allocate(Long.SIZE_BYTES).putLong(exerciseTime).array()

        nodeClient.connectedNodes.addOnSuccessListener { nodes ->
            nodes.forEach { node ->
                messageClient.sendMessage(node.id, "/start_heart_rate_service", payload)
                    .addOnSuccessListener {
                        Log.d("PhoneApp", "시작 신호 전송 성공")
                    }
                    .addOnFailureListener {
                        Log.e("PhoneApp", "시작 신호 전송 실패", it)
                    }
            }
        }
    }


    private fun sendResetMessageToWatch() {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                Wearable.getMessageClient(this).sendMessage(
                    node.id,
                    "/reset_step_count", // 워치에서 수신하는 path
                    ByteArray(0)
                ).addOnSuccessListener {
                    Log.d("시계 걸음 수 초기화", "📤 워치 걸음수 초기화 메시지 전송 성공")
                }.addOnFailureListener {
                    Log.e("시계 걸음 수 초기화", "❌ 워치 초기화 메시지 전송 실패", it)
                }
            }
        }
    }

    private fun sendStopMessageToWatch() {
        Wearable.getNodeClient(this).connectedNodes.addOnSuccessListener { nodes ->
            for (node in nodes) {
                Wearable.getMessageClient(this).sendMessage(
                    node.id,
                    "/stop_step_count", // 워치에서 수신하는 path
                    ByteArray(0)
                ).addOnSuccessListener {
                    Log.d("시계 걸음 수 정지", "📤 워치 걸음수 초기화 메시지 전송 성공")
                }.addOnFailureListener {
                    Log.e("시계 걸음 수 정지", "❌ 워치 초기화 메시지 전송 실패", it)
                }
            }
        }
    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                val percentage = remainingTime.toFloat() / totalTime
                val totalSeconds = ((remainingTime + 500) / 1000).toInt()
                val minutes = totalSeconds / 60
                val seconds = totalSeconds % 60
                val timeText = String.format("%02d:%02d", minutes, seconds)

                heartTimerView.updateProgress(percentage, timeText)
            }

            override fun onFinish() {
                remainingTime = 0L
                stopTimer()
                wViewModel.stopReceiving()
                wViewModel.btnStop()
                wViewModel.isEnded()
            }
        }.also {
            it.start()
        }
        isRunning = true
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        isRunning = false
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        remainingTime = totalTime
        isRunning = false
        heartTimerView.updateProgress(1f, "06:00") // 초기 상태로 리셋
    }

}