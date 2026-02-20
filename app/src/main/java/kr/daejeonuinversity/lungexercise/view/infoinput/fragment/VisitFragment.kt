package kr.daejeonuinversity.lungexercise.view.infoinput.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.FragmentVisitBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseFragment
import kr.daejeonuinversity.lungexercise.util.util.ShowCustomDatePicker
import kr.daejeonuinversity.lungexercise.util.util.UserInfoTempData
import kr.daejeonuinversity.lungexercise.view.infoinput.InfoInputActivity
import kr.daejeonuinversity.lungexercise.viewmodel.VisitViewModel
import org.koin.android.ext.android.inject

class VisitFragment : BaseFragment<FragmentVisitBinding>(R.layout.fragment_visit) {

    private val vViewModel : VisitViewModel by inject()

    override fun initView() {

        binding.apply {
            fragment = this@VisitFragment
            viewmodel = vViewModel
        }

        observe()

    }

    private fun observe() = vViewModel.let { vm ->

        vm.btnVisitState.observe(viewLifecycleOwner){

            if (it){

                val showCustomDatePicker = ShowCustomDatePicker()

                showCustomDatePicker.showCustomDatePicker(requireContext()) { selectedDate ->
                    binding.edtVisit.setText(selectedDate)
                    UserInfoTempData.visit = selectedDate
                    (activity as? InfoInputActivity)?.enableConfirmButton()
                }

            }

        }

    }

}