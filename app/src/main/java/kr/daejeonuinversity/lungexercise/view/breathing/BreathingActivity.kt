package kr.daejeonuinversity.lungexercise.view.breathing

import android.Manifest
import android.animation.ObjectAnimator
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityBreathingBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.event.ExhaleEvent
import kr.daejeonuinversity.lungexercise.util.event.ResultEvent
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.exercise.LungExerciseActivity
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.BreathingViewModel
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// BreathingActivity.kt
// BreathingActivity.kt
class BreathingActivity : BaseActivity<ActivityBreathingBinding>(R.layout.activity_breathing) {

    private val bViewModel: BreathingViewModel by inject()
    private var userProgressAnimator: ObjectAnimator? = null
    private val time = 8000
    private var userSeconds = 7000
    private var hasExhaleHandled = false
    private val backPressedCallback = BackPressedCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@BreathingActivity
            viewmodel = bViewModel
            lifecycleOwner = this@BreathingActivity
        }

        backPressedCallback.addCallbackActivity(this, LungExerciseActivity::class.java)

        observe()
    }

    private fun observe() = bViewModel.let{ vm ->
        vm.backClicked.observe(this) {
            if (it) {
                val intent = Intent(this@BreathingActivity, LungExerciseActivity::class.java)
                startActivityAnimation(intent, this@BreathingActivity)
                finish()
            }
        }

        vm.btnStartState.observe(this) {
            if (it) {
                binding.btnStart.visibility = View.GONE
                binding.btnStop.visibility = View.VISIBLE
                resetProgressBar()
                hasExhaleHandled = false
            }
        }

        vm.isResultAvailable.observe(this@BreathingActivity) {
            if (it) {
                val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

                vm.saveBreathData(time, userSeconds, date)

            }
        }

        vm.btnStopState.observe(this) {
            if (it) {
                binding.btnStart.visibility = View.VISIBLE
                binding.btnStop.visibility = View.GONE
                stopUserProgress()
            }
        }

        vm.exhaleEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { exhale ->
                if (hasExhaleHandled) return@observe

                when (exhale) {
                    is ExhaleEvent.Start -> startUserProgress()
                    is ExhaleEvent.End -> {
                        stopUserProgress()
                        userSeconds = exhale.duration.toInt()
                        hasExhaleHandled = true
                    }
                }
            }
        }

        vm.userTime.observe(this) {
            binding.txUserTime.text = it
        }

        vm.resultEvent.observe(this) { event ->
            event.getContentIfNotHandled()?.let { result ->
                when (result) {
                    ResultEvent.ShowResultDialog -> {
                        showDialog()

                    }
                    ResultEvent.ShowResultToast -> Toast.makeText(this, "호흡 연습을 완료하세요.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun onStartClicked(view: View) {
        bViewModel.btnStart()
    }

    fun onStopClicked(view: View) {
        bViewModel.btnStop()
    }

    private fun resetProgressBar() {
        binding.progressBarUser.progress = 0
    }

    private fun startUserProgress() {
        val progressBar = binding.progressBarUser
        progressBar.progress = 0
        userProgressAnimator?.cancel()
        userProgressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, 100).apply {
            duration = time.toLong()
            interpolator = LinearInterpolator()
            start()
        }
    }

    private fun stopUserProgress() {
        userProgressAnimator?.cancel()
        userProgressAnimator = null
    }

    private fun showDialog() {
        val dlg = BreathingDialog(this)
        dlg.show(
            (time / 1000).toLong(),
            bViewModel.userTime.value?.replace(" 초", "")?.toLongOrNull() ?: 0L
        )
    }

}



