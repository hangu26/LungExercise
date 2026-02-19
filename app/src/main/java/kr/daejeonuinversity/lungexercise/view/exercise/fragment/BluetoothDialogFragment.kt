package kr.daejeonuinversity.lungexercise.view.exercise.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.DialogBluetoothBinding
import kr.daejeonuinversity.lungexercise.util.adapter.PairedDeviceAdapter
import kr.daejeonuinversity.lungexercise.util.util.CustomToastPopup

class BluetoothFragment : DialogFragment() {

    private var _binding: DialogBluetoothBinding? = null
    private val binding get() = _binding!!

    private lateinit var adapter: PairedDeviceAdapter
    private var pendingDevices: List<BluetoothDevice>? = null
    private var pendingNextVisibility: Boolean? = null
    var onConnectClick: ((BluetoothDevice) -> Unit)? = null
    var onNextClick: Boolean = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogBluetoothBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.window?.apply {
            setLayout(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT
            )
            setBackgroundDrawableResource(R.drawable.border_result_breathing)
        }

        adapter = PairedDeviceAdapter(requireContext()) { device ->
            onConnectClick?.invoke(device)
        }

        binding.linearBluetoothList.adapter = adapter
        binding.linearBluetoothList.layoutManager = LinearLayoutManager(context)

        binding.btnNextDialog.setOnClickListener {
            dialog?.dismiss()
        }

        // Fragment가 준비되면 pendingDevices 처리
        pendingDevices?.let {
            adapter.setDevices(it)
            pendingDevices = null
        }

        pendingNextVisibility?.let {
            updateNextButtonState(it)
            pendingNextVisibility = null
        }

        binding.constraintCancelDialogBluetooth.setOnClickListener { dismiss() }
    }

    fun setDevices(devices: List<BluetoothDevice>) {
        if (::adapter.isInitialized) {
            adapter.setDevices(devices)
        } else {
            pendingDevices = devices
        }
    }

    fun visibilityBtnNext(isAble: Boolean) {
        if (_binding == null) {
            pendingNextVisibility = isAble
            return
        }

        updateNextButtonState(isAble)
    }

    private fun updateNextButtonState(isAble: Boolean) {
        if (isAble) {
            binding.btnNextDialog.visibility = View.VISIBLE
            showMaskPopupToast("연결에 성공했습니다.")
            onNextClick = true
        } else {
            binding.btnNextDialog.visibility = View.INVISIBLE
            showMaskPopupToast("연결에 실패했습니다.")
            onNextClick = false
        }
    }

    @SuppressLint("RestrictedApi")
    private fun showMaskPopupToast(text : String) {
        val customToast = CustomToastPopup(binding.root, layoutInflater)
        customToast.showMaskPopupToast(text)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}




