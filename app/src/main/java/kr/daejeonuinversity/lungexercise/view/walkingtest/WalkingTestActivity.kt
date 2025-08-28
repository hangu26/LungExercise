package kr.daejeonuinversity.lungexercise.view.walkingtest

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import android.view.View
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityWalkingTestBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.util.util.HeartTimerView
import kr.daejeonuinversity.lungexercise.util.util.MiBandReceiver
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.WalkingTestViewModel
import org.koin.android.ext.android.inject

class WalkingTestActivity :
    BaseActivity<ActivityWalkingTestBinding>(R.layout.activity_walking_test) {

    private val wViewModel: WalkingTestViewModel by inject()
    private lateinit var heartTimerView: HeartTimerView
    private var countDownTimer: CountDownTimer? = null
    private var totalTime = 6 * 60 * 1000L // 6분
    private val backPressedCallback = BackPressedCallback(this)
    private var remainingTime: Long = totalTime
    private var isRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            activity = this@WalkingTestActivity
            viewmodel = wViewModel
            lifecycleOwner = this@WalkingTestActivity
        }

        heartTimerView = binding.heartTimerView

        initButton()
        observe()
        backPressedCallback.addCallbackActivity(this, MainActivity::class.java)

    }

    @SuppressLint("ClickableViewAccessibility")
    fun initButton() {

        binding.btnStart.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)

            if (event?.action == MotionEvent.ACTION_UP) {

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
        vm.backClicked.observe(this@WalkingTestActivity) {
            if (it) {
                val intent = Intent(this@WalkingTestActivity, MainActivity::class.java)
                startActivityBackAnimation(intent, this@WalkingTestActivity)
                finish()
            }
        }

        vm.btnStartState.observe(this@WalkingTestActivity) {

            if (it) {

                binding.btnStart.visibility = View.GONE
                binding.btnStop.visibility = View.VISIBLE


            }

        }

        vm.btnStopState.observe(this@WalkingTestActivity) {

            if (it) {

                binding.btnStart.visibility = View.VISIBLE
                binding.btnStop.visibility = View.GONE

            }

        }

        vm.btnResetState.observe(this@WalkingTestActivity) {

            if (it) {

                binding.btnStart.visibility = View.VISIBLE
                binding.btnStop.visibility = View.GONE

            }

        }


    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(remainingTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                remainingTime = millisUntilFinished
                val percentage = remainingTime.toFloat() / totalTime
                val minutes = (remainingTime / 1000) / 60
                val seconds = (remainingTime / 1000) % 60
                val timeText = String.format("%02d:%02d", minutes, seconds)

                heartTimerView.updateProgress(percentage, timeText)
            }

            override fun onFinish() {
                remainingTime = 0L
                isRunning = false
                heartTimerView.updateProgress(0f, "00:00")
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