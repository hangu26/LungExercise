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
        val dialog = Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = layoutInflater.inflate(R.layout.dialog_fullscreen_video, null)
        dialog.setContentView(view)

        val playerView = view.findViewById<PlayerView>(R.id.playerView)

        // ExoPlayer 생성
        val player = ExoPlayer.Builder(this).build()
        playerView.player = player

        // raw 리소스 URI 만들기
        val uri = Uri.parse("android.resource://${packageName}/${R.raw.lungexercise}")
        val mediaItem = MediaItem.fromUri(uri)
        player.setMediaItem(mediaItem)

        // 자동 재생 및 종료
        player.prepare()
        player.play()

        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    player.release()
                    dialog.dismiss()
                }
            }
        })

        dialog.setOnDismissListener {
            player.release()
        }

        dialog.show()
    }


}