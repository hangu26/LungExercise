package kr.daejeonuinversity.lungexercise.view.infoinput.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.FragmentSmokingBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseFragment
import kr.daejeonuinversity.lungexercise.view.infoinput.InfoInputActivity
import kr.daejeonuinversity.lungexercise.viewmodel.SmokingViewModel
import org.koin.android.ext.android.inject


class SmokingFragment :
    BaseFragment<FragmentSmokingBinding>(R.layout.fragment_smoking) {

    private val sViewModel: SmokingViewModel by inject()

    override fun initView() {

        binding.apply {
            fragment = this@SmokingFragment
            viewmodel = sViewModel
        }

        observe()
    }

    private fun observe() = sViewModel.let { vm ->

        vm.btnNonSmokerState.observe(viewLifecycleOwner) { isSelected ->
            if (isSelected) {
                binding.iconNonSmoker.setImageResource(R.drawable.ic_non_smoke_clicked)
                binding.txNonSmoker.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )
                binding.constraintNonSmoker.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.border_constraint_gender_clicked
                    )
                binding.txNonSmokerDetail.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )

                vm.btnSmokerState.postValue(false)
                (activity as? InfoInputActivity)?.enableConfirmButton()

            } else {
                binding.iconNonSmoker.setImageResource(R.drawable.ic_no_smoke)
                binding.txNonSmoker.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.black)
                )
                binding.constraintNonSmoker.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.border_constraint_man
                    )
                binding.txNonSmokerDetail.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.black)
                )
            }
        }

        vm.btnSmokerState.observe(viewLifecycleOwner) { isSelected ->
            if (isSelected) {
                binding.iconSmoker.setImageResource(R.drawable.ic_smoke_clicked)
                binding.txSmoker.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )
                binding.constraintSmoker.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.border_constraint_gender_clicked
                    )
                binding.txSmokerDetail.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.white)
                )

                vm.btnNonSmokerState.postValue(false)
                (activity as? InfoInputActivity)?.enableConfirmButton()

            } else {
                binding.iconSmoker.setImageResource(R.drawable.ic_smoke)
                binding.txSmoker.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.black)
                )
                binding.constraintSmoker.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.border_constraint_man
                    )
                binding.txSmokerDetail.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.black)
                )
            }
        }
    }
}