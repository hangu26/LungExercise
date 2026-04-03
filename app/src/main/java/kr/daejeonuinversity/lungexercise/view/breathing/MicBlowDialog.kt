package kr.daejeonuinversity.lungexercise.view.breathing

import android.app.Dialog
import android.os.CountDownTimer
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kr.daejeonuinversity.lungexercise.databinding.DialogMicBlowBinding

class MicBlowDialog(
    private val context: AppCompatActivity,
    private val onCountdownFinished: () -> Unit
) {

    private lateinit var binding: DialogMicBlowBinding
    private val dlg = Dialog(context)
    private var countDownTimer: CountDownTimer? = null

    fun show() {
        binding = DialogMicBlowBinding.inflate(context.layoutInflater)

        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(binding.root)
        dlg.setCancelable(false)
        dlg.window?.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dlg.window?.setBackgroundDrawableResource(android.R.color.transparent)

        binding.txCountdown.text = "5"
        binding.lottieBlow.playAnimation()

        countDownTimer = object : CountDownTimer(5500L, 1000L) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = ((millisUntilFinished + 500L) / 1000L).toInt().coerceIn(1, 5)
                context.runOnUiThread {
                    binding.txCountdown.text = seconds.toString()
                }
            }

            override fun onFinish() {
                context.runOnUiThread {
                    dlg.dismiss()
                    onCountdownFinished()
                }
            }
        }.start()

        dlg.show()
    }

    fun dismiss() {
        countDownTimer?.cancel()
        if (dlg.isShowing) dlg.dismiss()
    }
}

