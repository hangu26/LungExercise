package kr.daejeonuinversity.lungexercise.view.breathing

import android.animation.ObjectAnimator
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.content.ContextCompat
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.data.model.StopStartState
import kr.daejeonuinversity.lungexercise.databinding.ActivityBreathingBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.view.lungexercise.LungExerciseActivity
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.BreathingViewModel
import org.koin.android.ext.android.inject

class BreathingActivity : BaseActivity<ActivityBreathingBinding>(R.layout.activity_breathing) {

    private val bViewModel: BreathingViewModel by inject()
    private var progressAnimator: ObjectAnimator? = null

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

    }

    private fun startBreathingProgress() {
        val progressBar = binding.progressBarNormal

        binding.btnStart.background =
            ContextCompat.getDrawable(this, R.drawable.border_btn_stop_timer)

        binding.txStart.text = resources.getString(R.string.tx_stop)

        progressAnimator?.cancel()

        progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100).apply {
            duration = 8000
            start()
        }
    }

    private fun stopBreathingProgress() {

        binding.btnStart.background =
            ContextCompat.getDrawable(this, R.drawable.border_btn_start_timer)

        binding.txStart.text = resources.getString(R.string.tx_start)

        progressAnimator?.cancel()
        progressAnimator = null
    }


}