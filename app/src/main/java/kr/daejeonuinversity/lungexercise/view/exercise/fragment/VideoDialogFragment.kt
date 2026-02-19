package kr.daejeonuinversity.lungexercise.view.exercise.fragment

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.OptIn
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.util.util.MaskBluetoothManager
import java.io.File

class VideoDialogFragment : DialogFragment(), MaskBluetoothManager.BreathingEventListener {

    private var player: ExoPlayer? = null
    private lateinit var playerView: PlayerView
    private var userProgressAnimator: ObjectAnimator? = null
    private var userTimerHandler: Handler? = null
    private var userTimerRunnable: Runnable? = null
    private var elapsedTimeMs = 0L // 경과 시간 (ms)
    private val time = 8000 // 최대 내쉬는 시간 (ms)

    private var handler: Handler? = null
    private var checkProgressBarRunnable: Runnable? = null

    private lateinit var progressBarUser: ProgressBar
    private lateinit var constraintBreathing: ConstraintLayout
    private lateinit var txPercent: TextView
    private lateinit var txBreathingTime: TextView

    private var playbackPosition: Long = 0L

    @OptIn(UnstableApi::class)
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext(), android.R.style.Theme_Black_NoTitleBar_Fullscreen)
        val view = requireActivity().layoutInflater.inflate(R.layout.dialog_fullscreen_video, null)
        dialog.setContentView(view)
        val orientation = resources.configuration.orientation

        val maskPopupVisibility = context?.getSharedPreferences("Mask Popup", Context.MODE_PRIVATE)
            ?.getBoolean("mask popup", false)

        val file = File(requireContext().getExternalFilesDir(null), "lungexercise.mp4")
        Log.d("파일 경로", "경로: ${file.absolutePath}, 존재 여부: ${file.exists()}")
        // View 초기화
        playerView = view.findViewById(R.id.playerView)
        progressBarUser = view.findViewById(R.id.progress_bar_user_video)
        constraintBreathing = view.findViewById(R.id.constraint_breathing)
        txPercent = view.findViewById(R.id.tx_percent)
        txBreathingTime = view.findViewById(R.id.tx_breathing_time)

        constraintBreathing.visibility = View.GONE

        // 가로 모드일 때만 fill 모드로 설정
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FILL

            val params = constraintBreathing.layoutParams as ConstraintLayout.LayoutParams
            params.matchConstraintPercentWidth = 0.26f // 가로일 때 비율
            constraintBreathing.layoutParams = params

        } else {
            playerView.resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT

            val params = constraintBreathing.layoutParams as ConstraintLayout.LayoutParams
            params.matchConstraintPercentWidth = 0.5f // 세로일 때 비율
            constraintBreathing.layoutParams = params
        }

        // 컨트롤러 사용 가능하게
        playerView.useController = true

        // 복원된 재생 위치가 있으면 사용
        playbackPosition = savedInstanceState?.getLong("playback_position") ?: 0L

        // ExoPlayer 초기화
        player = ExoPlayer.Builder(requireContext()).build().also { exoPlayer ->
            playerView.player = exoPlayer
            val uri =
                Uri.parse("android.resource://${requireContext().packageName}/${R.raw.lungexercise}")
            exoPlayer.setMediaItem(MediaItem.fromUri(uri))
            exoPlayer.prepare()
            exoPlayer.seekTo(playbackPosition)
            exoPlayer.play()

            // 호흡 UI 표시 타이밍 체크
            handler = Handler(Looper.getMainLooper())
            checkProgressBarRunnable = object : Runnable {
                override fun run() {
                    val currentPos = exoPlayer.currentPosition
                    if (currentPos >= 150_000L) {
                        if (maskPopupVisibility == true) {
                            constraintBreathing.visibility = View.VISIBLE
                        }
                    } else {
                        if (constraintBreathing.visibility != View.GONE) {
                            constraintBreathing.visibility = View.GONE
                            userProgressAnimator?.cancel()
                        }
                    }
                    handler?.postDelayed(this, 500)
                }
            }
            handler?.post(checkProgressBarRunnable!!)
        }

        // 블루투스 연결
        MaskBluetoothManager.setBreathingEventListener(this)
//        MaskBluetoothManager.connectToDevice(requireContext(), deviceName = "MASK7", true)

        return dialog
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong("playback_position", player?.currentPosition ?: 0L)
    }

    override fun onExhaleStart() {
        activity?.runOnUiThread {
            if (constraintBreathing.visibility == View.VISIBLE) {
                progressBarUser.progress = 0
                userProgressAnimator?.cancel()
                userTimerHandler?.removeCallbacks(userTimerRunnable ?: Runnable {})
                elapsedTimeMs = 0L

                // ProgressBar 애니메이션 시작
                userProgressAnimator =
                    ObjectAnimator.ofInt(progressBarUser, "progress", 0, 100).apply {
                        duration = time.toLong()
                        interpolator = LinearInterpolator()
                        start()
                    }

                // 초, 퍼센트 텍스트뷰 갱신
                userTimerHandler = Handler(Looper.getMainLooper())
                userTimerRunnable = object : Runnable {
                    @SuppressLint("SetTextI18n")
                    override fun run() {
                        elapsedTimeMs += 1000L
                        val seconds = (elapsedTimeMs / 1000).toInt()
                        val percent =
                            ((elapsedTimeMs.toDouble() / time) * 100).toInt().coerceAtMost(100)

                        txBreathingTime.text = "$seconds 초"
                        txPercent.text = "$percent%"

                        if (elapsedTimeMs < time) {
                            userTimerHandler?.postDelayed(this, 1000L)
                        }
                    }
                }
                userTimerHandler?.post(userTimerRunnable!!)
            }
        }
    }

    override fun onExhaleEnd(durationMs: Long) {
        activity?.runOnUiThread {
            userProgressAnimator?.cancel()
            userTimerHandler?.removeCallbacks(userTimerRunnable ?: Runnable {})
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        saveAndRelease()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        saveAndRelease()
    }

    override fun onDestroy() {
        super.onDestroy()
        saveAndRelease()
    }

    private fun saveAndRelease() {
        playbackPosition = player?.currentPosition ?: 0L

        handler?.removeCallbacksAndMessages(null)
        checkProgressBarRunnable = null
        handler = null

        userProgressAnimator?.cancel()
        userTimerHandler?.removeCallbacks(userTimerRunnable ?: Runnable {})
        userProgressAnimator = null

        player?.release()
        player = null

    }
}

