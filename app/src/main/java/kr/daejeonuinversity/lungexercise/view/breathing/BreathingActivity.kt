package kr.daejeonuinversity.lungexercise.view.breathing

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.model.StopStartState
import kr.daejeonuinversity.lungexercise.databinding.ActivityBreathingBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.event.ResultEvent
import kr.daejeonuinversity.lungexercise.view.lungexercise.LungExerciseActivity
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.BreathingViewModel
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BreathingActivity : BaseActivity<ActivityBreathingBinding>(R.layout.activity_breathing) {

    private val bViewModel: BreathingViewModel by inject()
    private var progressAnimator: ObjectAnimator? = null
    private var userProgressAnimator: ObjectAnimator? = null
    private val time = 8000
    private val userSeconds = 7000


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@BreathingActivity
            viewmodel = bViewModel
            lifecycleOwner = this@BreathingActivity
        }

        observe()

    }

    private fun observe() = bViewModel.let { vm ->
        vm.backClicked.observe(this@BreathingActivity) {
            if (it) {

                val intent = Intent(this@BreathingActivity, LungExerciseActivity::class.java)
                startActivityAnimation(intent, this@BreathingActivity)
                finish()

            }
        }

        vm.breathingState.observe(this@BreathingActivity) {

            when (it) {
                StopStartState.STOP -> {
                    startBreathingProgress()
                }

                StopStartState.START -> {
                    stopBreathingProgress()
                }

                else -> {

                }
            }
        }

        vm.isResultAvailable.observe(this@BreathingActivity) {
            if (it) {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                vm.saveBreathData(time, userSeconds, date)

            }
        }

        vm.resultEvent.observe(this@BreathingActivity) { event ->

            event.getContentIfNotHandled()?.let { result ->
                when (result) {

                    ResultEvent.ShowResultDialog -> {
                        showDialog()
                    }

                    ResultEvent.ShowResultToast -> {
                        Toast.makeText(this@BreathingActivity, "호흡 연습을 완료하세요.", Toast.LENGTH_SHORT)
                            .show()
                    }

                }
            }

        }

    }

    private fun startBreathingProgress() {
        val progressBar = binding.progressBarNormal

        binding.btnStart.background =
            ContextCompat.getDrawable(this, R.drawable.border_btn_stop_timer)

        binding.txStart.text = resources.getString(R.string.tx_stop)

        progressAnimator?.cancel()

        progressBar.progress = 1000

        /** 테스트용으로 잠깐 주석처리 **/
        progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 100, 100).apply {
            duration = time.toLong()
            start()
        }

        startUserProgress()

        bViewModel.apply {
            startCounting(time / 1000)
            startUserCounting(userSeconds / 1000)
        }
    }

    private fun stopBreathingProgress() {

        binding.btnStart.background =
            ContextCompat.getDrawable(this, R.drawable.border_btn_start_timer)

        binding.txStart.text = resources.getString(R.string.tx_start)

        progressAnimator?.cancel()
        progressAnimator = null

        stopUserProgress()

        bViewModel.stopCounting()
    }

    /** 유저 테스트용 프로그래스바. 추후 수정 **/
    private fun startUserProgress() {
        val progressBar = binding.progressBarUser

        userProgressAnimator?.cancel()

        // 사용자 시간에 맞춰 0 ~ 87%만 진행되도록 설정 (7초 동안)
        val maxProgressForUserTime = ((userSeconds.toFloat() / time) * 100).toInt() // 87

        userProgressAnimator =
            ObjectAnimator.ofInt(progressBar, "progress", 0, maxProgressForUserTime).apply {
                duration = userSeconds.toLong() // 7초 동안
                start()
            }
    }

    private fun stopUserProgress() {

        userProgressAnimator?.cancel()
        userProgressAnimator = null

    }

    private fun showDialog() {

        val txTime: Long = (time / 1000).toLong()
        val txUserTime: Long = (userSeconds / 1000).toLong()

        val dlg = BreathingDialog(context = this@BreathingActivity)
        dlg.show(txTime, txUserTime)

    }

}