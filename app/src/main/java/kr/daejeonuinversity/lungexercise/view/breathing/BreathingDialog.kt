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
                    txFeedbackDetail.text = context.resources.getString(R.string.tx_feedback_detail_under_41)
                }

                in 41..99 -> {
                    txFeedbackDetail.text = context.resources.getString(R.string.tx_feedback_detail_up_41)

                }

                else -> {
                    txFeedbackDetail.text = context.resources.getString(R.string.tx_feedback_detail)
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