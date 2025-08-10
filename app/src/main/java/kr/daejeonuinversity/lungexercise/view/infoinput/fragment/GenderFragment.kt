package kr.daejeonuinversity.lungexercise.view.infoinput.fragment

import android.graphics.PorterDuff
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.FragmentGenderBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseFragment
import kr.daejeonuinversity.lungexercise.view.infoinput.InfoInputActivity
import kr.daejeonuinversity.lungexercise.viewmodel.GenderViewModel
import org.koin.android.ext.android.inject

class GenderFragment : BaseFragment<FragmentGenderBinding>(R.layout.fragment_gender) {

    private val gViewModel: GenderViewModel by inject()

    override fun initView() {

        binding.apply {
            fragment = this@GenderFragment
            viewmodel = gViewModel
        }

        observe()

    }

    private fun observe() = gViewModel.let { vm ->

        vm.btnManState.observe(viewLifecycleOwner) {
            if (it) {
                binding.iconMan.setImageResource(R.drawable.icon_man_clicked)
                binding.txMan.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                binding.constraintMan.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.border_constraint_gender_clicked
                    )
                binding.txManDetail.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                vm.btnWomanState.postValue(false)
                (activity as? InfoInputActivity)?.enableConfirmButton()
            } else {
                binding.iconMan.setImageResource(R.drawable.icon_man)
                binding.txMan.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                binding.constraintMan.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.border_constraint_man)
                binding.txManDetail.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
        }

        vm.btnWomanState.observe(viewLifecycleOwner) {
            if (it) {
                binding.iconWoman.setImageResource(R.drawable.icon_woman_clicked)
                binding.txWoman.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                binding.constraintWoman.background =
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.border_constraint_gender_clicked
                    )
                binding.txWomanDetail.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
                vm.btnManState.postValue(false)
                (activity as? InfoInputActivity)?.enableConfirmButton()
            } else {
                binding.iconWoman.setImageResource(R.drawable.icon_woman)
                binding.txWoman.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
                binding.constraintWoman.background =
                    ContextCompat.getDrawable(requireContext(), R.drawable.border_constraint_man)
                binding.txWomanDetail.setTextColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.black
                    )
                )
            }
        }

    }

}