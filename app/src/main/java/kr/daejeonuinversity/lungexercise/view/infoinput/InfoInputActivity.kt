package kr.daejeonuinversity.lungexercise.view.infoinput

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityInfoInputBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.base.NavigationMenu
import kr.daejeonuinversity.lungexercise.util.util.UserInfoTempData
import kr.daejeonuinversity.lungexercise.view.infoinput.fragment.BirthdayFragment
import kr.daejeonuinversity.lungexercise.view.infoinput.fragment.BodyFragment
import kr.daejeonuinversity.lungexercise.view.infoinput.fragment.GenderFragment
import kr.daejeonuinversity.lungexercise.view.infoinput.fragment.InitialFragment
import kr.daejeonuinversity.lungexercise.view.infoinput.fragment.ScreeningFragment
import kr.daejeonuinversity.lungexercise.view.infoinput.fragment.VisitFragment
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.BirthdayViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.BodyViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.GenderViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.InfoInputViewModel
import org.koin.android.ext.android.inject

class InfoInputActivity : BaseActivity<ActivityInfoInputBinding>(R.layout.activity_info_input) {

    private val iViewModel: InfoInputViewModel by inject()
    private val bViewModel: BirthdayViewModel by inject()
    private val gViewModel: GenderViewModel by inject()
    private val bodyViewModel: BodyViewModel by inject()

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
                NavigationMenu.Screening -> navigateScreeningNum()
                NavigationMenu.Initial -> navigateInitial()
                NavigationMenu.Visit -> navigateVisit()
            }
        }

        vm.btnClicked.observe(this@InfoInputActivity) {

            when (it) {

                0 -> {
                    vm.changeMenu(NavigationMenu.Initial)
                    vm.btnNextState.postValue(false)
                    binding.frameProgress01.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                }

                1 -> {
                    vm.changeMenu(NavigationMenu.BIRTHDAY)
                    vm.btnNextState.postValue(false)
                    binding.frameProgress01.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress02.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                }

                2 -> {
                    vm.changeMenu(NavigationMenu.GENDER)
                    vm.btnNextState.postValue(false)
                    binding.frameProgress01.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress02.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress03.setBackgroundResource(R.drawable.border_progress_input_info_bar)

                }

                3 -> {
                    vm.changeMenu(NavigationMenu.BODY)
                    vm.btnNextState.postValue(false)
                    binding.frameProgress01.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress02.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress03.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress04.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                }

                4 -> {
                    vm.changeMenu(NavigationMenu.Screening)
                    vm.btnNextState.postValue(false)
                    binding.frameProgress01.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress02.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress03.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress04.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress05.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                }

                5 -> {
                    vm.changeMenu(NavigationMenu.Visit)
                    vm.btnNextState.postValue(false)
                    binding.frameProgress01.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress02.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress03.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress04.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress05.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                    binding.frameProgress06.setBackgroundResource(R.drawable.border_progress_input_info_bar)
                }


                6 -> {

                    val birthday = UserInfoTempData.birthday
                    val gender = UserInfoTempData.gender
                    val height = UserInfoTempData.stature
                    val weight = UserInfoTempData.weight
                    Log.d(
                        "InfoInput",
                        "Birthday: $birthday, Gender: $gender, Height: $height, Weight: $weight"
                    )
                    vm.saveBirthday(birthday, gender, height, weight)

                    getSharedPreferences("tutorial", Context.MODE_PRIVATE).edit()
                        .putInt("isClear", 1).apply()

                    val intent = Intent(this@InfoInputActivity, MainActivity::class.java)
                    startActivity(intent)
                    UserInfoTempData.clear()
                    finish()

                }
            }

        }

        vm.btnNextState.observe(this@InfoInputActivity) {
            if (it) {
                binding.btnNext.background =
                    ContextCompat.getDrawable(
                        this@InfoInputActivity,
                        R.drawable.border_btn_next_able
                    )
                binding.btnNext.isClickable = true
            } else {
                binding.btnNext.background =
                    ContextCompat.getDrawable(
                        this@InfoInputActivity,
                        R.drawable.border_btn_next_unable
                    )
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

    /** 스크리닝 등록번호 프래그먼트 **/
    private fun navigateScreeningNum(bundle: Bundle? = null) {
        changeFragment(ScreeningFragment(), bundle)

    }

    /** 이니셜 프래그먼트 **/
    private fun navigateInitial(bundle: Bundle? = null) {
        changeFragment(InitialFragment(), bundle)

    }

    /** 방문일 프래그먼트 **/
    private fun navigateVisit(bundle: Bundle? = null) {
        changeFragment(VisitFragment(), bundle)

    }

    fun changeFragment(fragment: Fragment, bundle: Bundle? = null) {
        bundle?.let { b -> fragment.apply { arguments = b } }
        supportFragmentManager.beginTransaction().replace(R.id.fl_main, fragment).commit()
    }

}