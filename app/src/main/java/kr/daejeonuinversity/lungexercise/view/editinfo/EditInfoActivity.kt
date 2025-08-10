package kr.daejeonuinversity.lungexercise.view.editinfo

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.data.local.entity.UserInfo
import kr.daejeonuinversity.lungexercise.databinding.ActivityEditInfoBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.EditInfoViewModel
import org.koin.android.ext.android.inject
import java.time.LocalDate

class EditInfoActivity : BaseActivity<ActivityEditInfoBinding>(R.layout.activity_edit_info) {

    private val eViewModel: EditInfoViewModel by inject()
    private val backPressedCallback = BackPressedCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@EditInfoActivity
            viewmodel = eViewModel
            lifecycleOwner = this@EditInfoActivity
        }

        backPressedCallback.addCallbackActivity(this, MainActivity::class.java)
        observe()

    }

    private fun observe() = eViewModel.let { vm ->

        vm.fetchUserInfo()

        vm.btnBackState.observe(this@EditInfoActivity) {

            if (it) {

                val intent = Intent(this@EditInfoActivity, MainActivity::class.java)
                startActivityBackAnimation(intent, this@EditInfoActivity)
                finish()

            }
        }

        vm.btnMan.observe(this@EditInfoActivity) {
            if (it) {


            }
        }

        vm.genderState.observe(this@EditInfoActivity) { gender ->
            setGenderBackground(gender == "man")
        }

        vm.btnSaveState.observe(this@EditInfoActivity) {
            if (it) {
                val yearStr = binding.edtYear.text.toString()
                val monthStr = binding.edtMonth.text.toString()
                val dayStr = binding.edtDay.text.toString()
                val genderStr = vm.genderState.value ?: "man"
                val weightStr = binding.edtWeight.text.toString()
                val heightStr = binding.edtHeight.text.toString()

                if (yearStr.isBlank() || monthStr.isBlank() || dayStr.isBlank()) {
                    Toast.makeText(this, "년, 월, 일을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@observe
                }

                if (weightStr.isBlank() || heightStr.isBlank()) {
                    Toast.makeText(this, "몸무게와 키를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@observe
                }

                if (!isValidDateInput()) {
                    Toast.makeText(this, "올바른 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@observe
                }

                val userInfo = UserInfo(
                    birthday = yearStr + monthStr + dayStr,
                    gender = genderStr,
                    weight = weightStr.toInt(),
                    height = heightStr.toInt()
                )

                vm.saveData(userInfo)

                val intent = Intent(this@EditInfoActivity, MainActivity::class.java)
                startActivityBackAnimation(intent,this@EditInfoActivity)
                finish()

            }
        }
    }

    private fun isValidDateInput(): Boolean {
        val yearStr = binding.edtYear.text.toString()
        val monthStr = binding.edtMonth.text.toString()
        val dayStr = binding.edtDay.text.toString()

        if (yearStr.isBlank() || monthStr.isBlank() || dayStr.isBlank()) {
            return false
        }

        val year = yearStr.toIntOrNull() ?: return false
        val month = monthStr.toIntOrNull() ?: return false
        val day = dayStr.toIntOrNull() ?: return false

        val currentYear = LocalDate.now().year
        if (year < 1800 || year > currentYear) return false
        if (month !in 1..12) return false
        if (day !in 1..31) return false

        val maxDay = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 31
        }

        if (day > maxDay) return false

        return true
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    private fun setGenderBackground(isManSelected: Boolean) {
        // 남자 버튼 배경 및 텍스트 색상
        if (isManSelected) {
            binding.constraintMan.background =
                ContextCompat.getDrawable(this, R.drawable.border_constraint_gender)
            binding.btnMan.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.constraintMan.background =
                ContextCompat.getDrawable(this, R.drawable.border_constraint_gender_not)
            binding.btnMan.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        // 여자 버튼 배경 및 텍스트 색상
        if (isManSelected) {
            binding.constraintWoman.background =
                ContextCompat.getDrawable(this, R.drawable.border_constraint_gender_not)
            binding.btnWoman.setTextColor(ContextCompat.getColor(this, R.color.black))
        } else {
            binding.constraintWoman.background =
                ContextCompat.getDrawable(this, R.drawable.border_constraint_gender)
            binding.btnWoman.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
    }

}