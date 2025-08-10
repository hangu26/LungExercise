package kr.daejeonuinversity.lungexercise.view.infoinput.fragment

import android.app.DatePickerDialog
import android.text.InputType
import androidx.databinding.adapters.TextViewBindingAdapter.setText
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.FragmentBirthdayBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseFragment
import kr.daejeonuinversity.lungexercise.util.util.ShowCustomDatePicker
import kr.daejeonuinversity.lungexercise.util.util.UserInfoTempData
import kr.daejeonuinversity.lungexercise.view.infoinput.InfoInputActivity
import kr.daejeonuinversity.lungexercise.viewmodel.BirthdayViewModel
import org.koin.android.ext.android.inject
import java.util.Calendar

class BirthdayFragment : BaseFragment<FragmentBirthdayBinding>(R.layout.fragment_birthday) {

    private val bViewModel : BirthdayViewModel by inject()

    override fun initView() {
        binding.apply {
            fragment = this@BirthdayFragment
            viewmodel = bViewModel
        }

        observe()

    }

    private fun observe() = bViewModel.let { vm ->

        vm.btnBirthState.observe(viewLifecycleOwner){

            if (it){

                val showCustomDatePicker = ShowCustomDatePicker()

                showCustomDatePicker.showCustomDatePicker(requireContext()) { selectedDate ->
                    binding.edtBirth.setText(selectedDate)
                    UserInfoTempData.birthday = selectedDate
                    (activity as? InfoInputActivity)?.enableConfirmButton()
                }

            }

        }

    }

}