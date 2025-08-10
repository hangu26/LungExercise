package kr.daejeonuinversity.lungexercise.view.exercise.fragment

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.DialogBluetoothBinding

class BluetoothFragment(
    private val onConnectClick: (Boolean) -> Unit,
    private val onNextClick: () -> Unit
) : DialogFragment() {

    private var _binding: DialogBluetoothBinding? = null
    private val binding get() = _binding!!

    // 상태 저장용 변수
    private var currentStatusText: String? = null
    private var bluetoothListVisible: Boolean = false
    private var nextDialogVisible: Boolean = false

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

        // 초기 UI 상태 반영
        currentStatusText?.let {
            binding.txDeviceName.text = it
        }
        binding.linearBluetoothList.visibility =
            if (bluetoothListVisible) View.VISIBLE else View.GONE

        binding.btnNextDialog.visibility =
            if (nextDialogVisible) View.VISIBLE else View.INVISIBLE

        binding.apply {
            constraintConnectDevice.setOnClickListener {
                onConnectClick(true)
            }
            btnRefresh.setOnClickListener {
                onConnectClick(false)
            }
            btnNextDialog.setOnClickListener {
                onNextClick()
            }
            constraintCancelDialogBluetooth.setOnClickListener {
                dismiss()
            }
        }

        // 콜백 호출 초기화
        onConnectClick(false)
    }

    fun setStatusText(text: String) {
        currentStatusText = text
        if (isAdded && _binding != null) {
            binding.txDeviceName.text = text
            binding.linearBluetoothList.visibility =
                if (text.startsWith("연결할 디바이스 발견")) View.VISIBLE else View.GONE
        }
    }

    fun setBluetoothListVisible(visible: Boolean) {
        bluetoothListVisible = visible
        if (isAdded && _binding != null) {
            binding.linearBluetoothList.visibility = if (visible) View.VISIBLE else View.GONE
        }
    }

    fun setNextDialogVisible(visible: Boolean) {

        nextDialogVisible = visible
        if (isAdded && _binding != null) {
            binding.btnNextDialog.visibility = if (visible) View.VISIBLE else View.INVISIBLE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

