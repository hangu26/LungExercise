package kr.daejeonuinversity.lungexercise.view.infoinput.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.FragmentScreeningBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseFragment
import kr.daejeonuinversity.lungexercise.view.infoinput.InfoInputActivity
import kr.daejeonuinversity.lungexercise.viewmodel.ScreeningViewModel
import org.koin.android.ext.android.inject

class ScreeningFragment : BaseFragment<FragmentScreeningBinding>(R.layout.fragment_screening) {

    private val sViewModel: ScreeningViewModel by inject()

    override fun initView() {

        binding.apply {
            fragment = this@ScreeningFragment
            viewmodel = sViewModel
        }

        observe()

    }

    private fun observe() = sViewModel.let { vm ->

        vm.isInputValid.observe(viewLifecycleOwner) { isValid ->
            if (isValid) {
                (activity as? InfoInputActivity)?.enableConfirmButton()
            } else {
                (activity as? InfoInputActivity)?.unableConfirmButton()
            }
        }

    }

}