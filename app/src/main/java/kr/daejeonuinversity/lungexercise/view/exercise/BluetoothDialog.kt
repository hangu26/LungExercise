package kr.daejeonuinversity.lungexercise.view.exercise

import android.annotation.SuppressLint
import android.app.Dialog
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.DialogBluetoothBinding

/**
class BluetoothDialog(
    private val context: AppCompatActivity,
    private val onConnectClick: (Boolean) -> Unit
) {

    private lateinit var binding: DialogBluetoothBinding
    private val dlg = Dialog(context)

    fun show() {
        binding = DialogBluetoothBinding.inflate(context.layoutInflater)
        dlg.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dlg.setContentView(binding.root)
        dlg.setCancelable(false)
        dlg.window!!.setLayout(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dlg.window!!.setBackgroundDrawableResource(R.drawable.border_result_breathing)
        onConnectClick(false)

        binding.apply {
            constraintConnectDevice.setOnClickListener {
                onConnectClick(true)
            }

            btnRefresh.setOnClickListener {
                onConnectClick(false)
            }

            btnNextDialog.setOnClickListener {
                dlg.dismiss()
            }

            constraintCancelDialogBluetooth.setOnClickListener {
                dlg.dismiss()
            }
        }
        dlg.show()
    }

    fun setStatusText(text: String) {
        if (::binding.isInitialized) {
            binding.txDeviceName.text = text
        }

        if (text.startsWith("연결할 디바이스 발견")) {
            binding.linearBluetoothList.visibility = View.VISIBLE
        } else {
            binding.linearBluetoothList.visibility = View.GONE
        }

    }

    fun setBluetoothListVisible(visible: Boolean) {
        if (::binding.isInitialized) {
            binding.linearBluetoothList.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

}
**/