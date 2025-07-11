package kr.daejeonuinversity.lungexercise.view.infoinput.fragment

import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.FragmentBodyBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseFragment
import kr.daejeonuinversity.lungexercise.view.infoinput.InfoInputActivity
import kr.daejeonuinversity.lungexercise.viewmodel.BodyViewModel
import org.koin.android.ext.android.inject

class BodyFragment : BaseFragment<FragmentBodyBinding>(R.layout.fragment_body) {

    private val bViewModel: BodyViewModel by inject()

    override fun initView() {

        binding.apply {
            fragment = this@BodyFragment
            viewmodel = bViewModel
        }

        observe()

    }

    private fun observe() = bViewModel.let { vm ->

        vm.isInputValid.observe(viewLifecycleOwner) { isValid ->
            if (isValid) {
                (activity as? InfoInputActivity)?.enableConfirmButton()
            } else {
                (activity as? InfoInputActivity)?.unableConfirmButton()
            }
        }

    }

}