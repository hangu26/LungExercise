package kr.daejeonuinversity.lungexercise.view.breathing

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.Window
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.DialogResultBreathingBinding
import kotlin.math.roundToInt

class BreathingDialog(private val context: AppCompatActivity) {

    private lateinit var binding: DialogResultBreathingBinding
    private var progressAnimator: ObjectAnimator? = null
    private val dlg = Dialog(context)

    @SuppressLint("SetTextI18n")
    fun show(normalTime: Long, userTime: Long) {

        binding = DialogResultBreathingBinding.inflate(context.layoutInflater)

        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(binding.root)
        dlg.setCancelable(false)
        dlg.window!!.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dlg.window!!.setBackgroundDrawableResource(R.drawable.border_result_breathing)

        binding.apply {
            txNormalTimeData.text = normalTime.toString() + "초"
            txUserTimeData.text = userTime.toString() + "초"
            btnClose.setOnClickListener {
                dlg.dismiss()
            }

            val achievementRate = ((userTime.toFloat() / normalTime.toFloat()) * 100).toString()

            when (achievementRate.toDouble().roundToInt()) {

                in 0..40 -> {
                    txFeedbackDetail.text = "\"시작이 반입니다! 꾸준히 연습해서 더 나은 결과를 만들어보세요.\""
                }

                in 41..99 -> {
                    txFeedbackDetail.text = "\"잘하고 있어요! 조금만 더 힘내서 목표에 도달해봐요.\""
                }

                else -> {
                    txFeedbackDetail.text = "\"훌륭합니다! 목표를 성공적으로 달성했어요. 계속 꾸준히 유지하며 건강한 습관을 지켜나가세요.\""
                }

            }

            if (achievementRate.toDouble().roundToInt() > 100) {

                txPercentData.text = "100 %"

            } else {
                val percent = achievementRate.toDouble().roundToInt()
                txPercentData.text = "$percent %"
            }
            startUserProgress(achievementRate.toDouble().roundToInt())

        }

        dlg.show()

    }

    private fun startUserProgress(achievementRate: Int) {
        val progressBar = binding.progressBarAchievement
        progressBar.progress = 0
        progressAnimator?.cancel()
        progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, achievementRate).apply {
            duration = 1300L
            interpolator = LinearInterpolator()
            start()
        }
    }

}