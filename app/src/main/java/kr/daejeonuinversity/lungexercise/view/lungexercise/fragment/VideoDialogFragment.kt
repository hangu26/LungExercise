package kr.daejeonuinversity.lungexercise.view.lungexercise.fragment

import android.animation.ObjectAnimator
import android.app.Dialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ProgressBar
import androidx.fragment.app.DialogFragment
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import kr.daejeonuinversity.lungexercise.R

class VideoDialogFragment : DialogFragment() {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private var userProgressAnimator: ObjectAnimator? = null
    private var normalProgressAnimator: ObjectAnimator? = null
    private val time = 8000
    private val userSeconds = 7000

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("playback_position", player?.currentPosition ?: 0L)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_fullscreen_video, null)
        dialog.setContentView(view)

        playerView = view.findViewById(R.id.playerView)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar_user_video)
        progressBar.visibility = View.GONE // 처음에는 숨김
        val normalProgressBar = view.findViewById<ProgressBar>(R.id.progress_bar_normal_video)
        normalProgressBar.visibility = View.GONE // 처음에는 숨김 처리

        val startPosition = savedInstanceState?.getLong("playback_position") ?: 0L

        player = ExoPlayer.Builder(requireContext()).build().also {
            playerView.player = it
            val uri =
                Uri.parse("android.resource://${requireContext().packageName}/${R.raw.lungexercise}")
            it.setMediaItem(MediaItem.fromUri(uri))
            it.prepare()
            it.seekTo(startPosition) // 재생 위치 복원
            it.play()

            val handler = Handler(Looper.getMainLooper())
            val checkProgressBarRunnable = object : Runnable {
                override fun run() {
                    val currentPos = it.currentPosition
                    if (currentPos >= 150_000L) { // 2분 30초(150,000ms)부터
                        if (progressBar.visibility != View.VISIBLE) {
                            progressBar.visibility = View.VISIBLE
                            normalProgressBar.visibility = View.VISIBLE  // 추가: normalProgressBar도 보이게
                            startUserProgress(view)  // 유저 프로그래스바 시작
                            startNormalProgress(view) // 노멀 프로그래스바 시작
                        }
                    } else {
                        if (progressBar.visibility != View.GONE) {
                            progressBar.visibility = View.GONE
                        }
                        if (normalProgressBar.visibility != View.GONE) {
                            normalProgressBar.visibility = View.GONE  // 추가: 안 보이게
                        }
                        userProgressAnimator?.cancel()
                        normalProgressAnimator?.cancel()
                    }
                    handler.postDelayed(this, 500) // 0.5초마다 체크
                }
            }
            handler.post(checkProgressBarRunnable)

            return dialog
        }
    }

    /** 유저 테스트용 프로그래스바. 추후 수정 **/
    private fun startUserProgress(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar_user_video)

        userProgressAnimator?.cancel()

        val maxProgressForUserTime = ((userSeconds.toFloat() / time) * 100).toInt() // 87

        userProgressAnimator =
            ObjectAnimator.ofInt(progressBar, "progress", 0, maxProgressForUserTime).apply {
                duration = userSeconds.toLong() // 7초 동안
                start()
            }

    }

    private fun startNormalProgress(view: View) {
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar_normal_video)

        normalProgressAnimator?.cancel()

        normalProgressAnimator =
            ObjectAnimator.ofInt(progressBar, "progress", 100, 100).apply {
                duration = time.toLong() // 7초 동안
                start()
            }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        player?.release()
        player = null
    }
}