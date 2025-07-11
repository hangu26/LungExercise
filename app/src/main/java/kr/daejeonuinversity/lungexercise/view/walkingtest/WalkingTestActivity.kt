package kr.daejeonuinversity.lungexercise.view.walkingtest

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.view.MotionEvent
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityWalkingTestBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            activity = this@WalkingTestActivity
            viewmodel = wViewModel
            lifecycleOwner = this@WalkingTestActivity
        }

        heartTimerView = binding.heartTimerView

        val miBandReceiver = MiBandReceiver(this)
        miBandReceiver.scanMiBand()

        initButton()
        observe()

    }
    @SuppressLint("ClickableViewAccessibility")
    fun initButton() {

        binding.btnStart.setOnTouchListener { v, event ->
            setTouchAnimation(v,event)

            if (event?.action == MotionEvent.ACTION_UP) {

                startTimer()

                wViewModel.heartRate.observe(this) {
                    binding.txHeartRate.text = "$it"
                }

                wViewModel.stepCount.observe(this) {
                    binding.txStepCount.text = "$it"
                }

                wViewModel.startReceiving()

            }

            false
        }

        binding.btnStop.setOnTouchListener { v, event ->
            setTouchAnimation(v,event)

            if (event?.action == MotionEvent.ACTION_UP) {

                stopTimer()
                wViewModel.stopReceiving()

            }

            false
        }

    }

    private fun observe() = wViewModel.let { vm ->
        vm.backClicked.observe(this@WalkingTestActivity) {
            if (it) {
                val intent = Intent(this@WalkingTestActivity, MainActivity::class.java)
                startActivityAnimation(intent, this@WalkingTestActivity)
                finish()
            }
        }

        vm.btnStartState.observe(this@WalkingTestActivity) {

            if (it) {



            }

        }

        vm.btnStopState.observe(this@WalkingTestActivity) {

            if (it) {

                stopTimer()
                vm.stopReceiving()

            }

        }



    }

    private fun startTimer() {
        countDownTimer = object : CountDownTimer(totalTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remaining = millisUntilFinished
                val percentage = remaining.toFloat() / totalTime
                val minutes = (remaining / 1000) / 60
                val seconds = (remaining / 1000) % 60
                val timeText = String.format("%02d:%02d", minutes, seconds)

                heartTimerView.updateProgress(percentage, timeText)
            }

            override fun onFinish() {
                heartTimerView.updateProgress(0f, "00:00")
            }
        }.also {
            it.start()
        }
    }

    private fun stopTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
        heartTimerView.updateProgress(1f, "06:00") // 초기 상태로 리셋
    }

}