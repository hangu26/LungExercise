package kr.daejeonuinversity.lungexercise.view.lungexercise

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.widget.VideoView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityLungExerciseBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.view.breathing.BreathingActivity
import kr.daejeonuinversity.lungexercise.view.lungexercise.fragment.VideoDialogFragment
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.LungExerciseViewModel
import org.koin.android.ext.android.inject

class LungExerciseActivity :
    BaseActivity<ActivityLungExerciseBinding>(R.layout.activity_lung_exercise) {

    private val lViewModel: LungExerciseViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@LungExerciseActivity
            viewmodel = lViewModel
            lifecycleOwner = this@LungExerciseActivity
        }

        initButton()
        observe()

    }

    private fun observe() = lViewModel.let { vm ->
        vm.backClicked.observe(this@LungExerciseActivity) {
            if (it) {
                val intent = Intent(this@LungExerciseActivity, MainActivity::class.java)
                startActivityAnimation(intent, this@LungExerciseActivity)
                finish()
            }
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    fun initButton() {

        binding.constraintBreath.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)

            if (event?.action == MotionEvent.ACTION_UP) {
                val intent = Intent(this@LungExerciseActivity, BreathingActivity::class.java)
                startActivityAnimation(intent,this@LungExerciseActivity)
            }

            false
        }

        binding.constraintVideo.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)

            if (event?.action == MotionEvent.ACTION_UP) {
                showExoPlayerPopup()
            }

            false
        }
    }

    private fun showExoPlayerPopup() {
        val fragment = VideoDialogFragment()
        fragment.show(supportFragmentManager, "video_dialog")
    }


}