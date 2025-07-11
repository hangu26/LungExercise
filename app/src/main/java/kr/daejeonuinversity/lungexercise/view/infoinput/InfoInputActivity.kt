package kr.daejeonuinversity.lungexercise.view.infoinput

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityInfoInputBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.base.NavigationMenu
import kr.daejeonuinversity.lungexercise.view.infoinput.fragment.BirthdayFragment
import kr.daejeonuinversity.lungexercise.view.infoinput.fragment.BodyFragment
import kr.daejeonuinversity.lungexercise.view.infoinput.fragment.GenderFragment
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.InfoInputViewModel
import org.koin.android.ext.android.inject

class InfoInputActivity : BaseActivity<ActivityInfoInputBinding>(R.layout.activity_info_input) {

    private val iViewModel: InfoInputViewModel by inject()
    private var btnNextClicked = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@InfoInputActivity
            viewmodel = iViewModel
            lifecycleOwner = this@InfoInputActivity
        }

        observe()

    }

    @SuppressLint("ResourceAsColor")
    private fun observe() = iViewModel.let { vm ->

        vm.menu.observe(this@InfoInputActivity) { menu ->
            menu ?: return@observe // menu가 null이면 이벤트 무시 후, 리턴
            when (menu) {
                NavigationMenu.BIRTHDAY -> navigateBirthDay()
                NavigationMenu.GENDER -> navigateGender()
                NavigationMenu.BODY -> navigateBody()
            }
        }

        vm.btnClicked.observe(this@InfoInputActivity) {

            when (it) {
                0 -> {
                    iViewModel.changeMenu(NavigationMenu.BIRTHDAY)
                    binding.frameProgress01.setBackgroundResource(R.color.appBar_title_01)
                }

                1 -> {
                    iViewModel.changeMenu(NavigationMenu.GENDER)
                    iViewModel.btnNextState.postValue(false)
                    binding.frameProgress01.setBackgroundResource(R.color.appBar_title_01)
                    binding.frameProgress02.setBackgroundResource(R.color.appBar_title_01)
                }

                2 -> {
                    iViewModel.changeMenu(NavigationMenu.BODY)
                    iViewModel.btnNextState.postValue(false)
                    binding.frameProgress01.setBackgroundResource(R.color.appBar_title_01)
                    binding.frameProgress02.setBackgroundResource(R.color.appBar_title_01)
                    binding.frameProgress03.setBackgroundResource(R.color.appBar_title_01)
                }

                3 -> {
                    val intent = Intent(this@InfoInputActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }

        }

        vm.btnNextState.observe(this@InfoInputActivity){
            if (it){
                binding.btnNext.background =
                    ContextCompat.getDrawable(this@InfoInputActivity, R.drawable.border_btn_next_able)
                binding.btnNext.isClickable = true
            }else{
                binding.btnNext.background =
                    ContextCompat.getDrawable(this@InfoInputActivity, R.drawable.border_btn_next_unable)
                binding.btnNext.isClickable = false
            }
        }

    }

    fun enableConfirmButton() {
        iViewModel.btnNextState.postValue(true)
    }

    fun unableConfirmButton() {
        iViewModel.btnNextState.postValue(false)
    }

    private fun navigateBirthDay(bundle: Bundle? = null) {
        changeFragment(BirthdayFragment(), bundle)

    }

    private fun navigateGender(bundle: Bundle? = null) {
        changeFragment(GenderFragment(), bundle)

    }

    private fun navigateBody(bundle: Bundle? = null) {
        changeFragment(BodyFragment(), bundle)

    }

    fun changeFragment(fragment: Fragment, bundle: Bundle? = null) {
        bundle?.let { b -> fragment.apply { arguments = b } }
        supportFragmentManager.beginTransaction().replace(R.id.fl_main, fragment).commit()
    }

}