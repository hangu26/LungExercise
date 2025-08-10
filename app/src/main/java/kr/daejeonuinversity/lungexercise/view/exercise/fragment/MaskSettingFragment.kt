package kr.daejeonuinversity.lungexercise.view.exercise.fragment

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.fragment.app.DialogFragment
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.DialogMaskSettingBinding

class MaskSettingFragment(
    private val onClearClick: (Boolean) -> Unit
) : DialogFragment() {

    private var _binding: DialogMaskSettingBinding? = null
    private val binding get() = _binding!!

    private lateinit var progressAnimator: ValueAnimator

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogMaskSettingBinding.inflate(inflater, container, false)
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

        // 초기 세팅
        binding.progressBar.progress = 0
        binding.txCompleteSetting.visibility = View.GONE
        binding.btnCompleteSettingMask.apply {
            isClickable = false
            isFocusable = false
        }

        startProgressAnimation()
    }

    private fun startProgressAnimation() {
        progressAnimator = ValueAnimator.ofInt(0, 100).apply {
            duration = 10_000L // 10초
            interpolator = LinearInterpolator()
            addUpdateListener { animation ->
                val progress = animation.animatedValue as Int
                binding.progressBar.progress = progress
            }
            addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    binding.txCompleteSetting.visibility = View.VISIBLE
                    binding.btnCompleteSettingMask.apply {
                        isClickable = true
                        isFocusable = true
                        setOnClickListener {
                            dismiss()
                        }
                    }
                }
            })
        }
        progressAnimator.start()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        progressAnimator.cancel()
        _binding = null
    }
}
