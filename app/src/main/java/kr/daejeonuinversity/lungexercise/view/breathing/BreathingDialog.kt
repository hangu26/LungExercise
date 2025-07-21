package kr.daejeonuinversity.lungexercise.view.breathing

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.DialogResultBreathingBinding

class BreathingDialog(private val context: AppCompatActivity) {

    private lateinit var binding: DialogResultBreathingBinding

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
            txPercentData.text = ((userTime.toFloat() / normalTime.toFloat()) * 100).toString()
        }

        dlg.show()

    }

}