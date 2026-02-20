package kr.daejeonuinversity.lungexercise.view.infoinput.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.FragmentInitialBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseFragment
import kr.daejeonuinversity.lungexercise.view.infoinput.InfoInputActivity
import kr.daejeonuinversity.lungexercise.viewmodel.InitialViewModel
import org.koin.android.ext.android.inject

class InitialFragment : BaseFragment<FragmentInitialBinding>(R.layout.fragment_initial) {

    private val iViewModel : InitialViewModel by inject()

    override fun initView() {

        binding.apply {
            fragment = this@InitialFragment
            viewmodel = iViewModel

        }

        observe()

    }

    private fun observe() = iViewModel.let { vm ->

        vm.isInputValid.observe(viewLifecycleOwner) { isValid ->
            if (isValid) {
                (activity as? InfoInputActivity)?.enableConfirmButton()
            } else {
                (activity as? InfoInputActivity)?.unableConfirmButton()
            }
        }

    }

}