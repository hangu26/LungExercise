package kr.daejeonuinversity.lungexercise.view.setting

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivitySettingBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.util.util.CustomToastPopup
import kr.daejeonuinversity.lungexercise.view.exercise.LungExerciseActivity
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.SettingViewModel
import org.koin.android.ext.android.inject

class SettingActivity : BaseActivity<ActivitySettingBinding>(R.layout.activity_setting) {

    private val sViewModel: SettingViewModel by inject()
    private val backPressedCallback = BackPressedCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            activity = this@SettingActivity
            viewmodel = sViewModel
            lifecycleOwner = this@SettingActivity
        }

        backPressedCallback.addCallbackActivity(this, MainActivity::class.java)

        observe()
        initButton()
    }

    @SuppressLint("ClickableViewAccessibility", "CommitPrefEdits", "RestrictedApi")
    private fun initButton() {




        binding.constraintMaskPopup.setOnTouchListener { v, event ->
            setTouchAnimation(v, event)

            if (event?.action == MotionEvent.ACTION_UP) {

                    sViewModel.toggleMaskPopup()

            }
            true
        }


    }

    private fun observe() = sViewModel.let { vm ->

        sViewModel.loadMaskPopup()

        vm.maskPopup.observe(this@SettingActivity){ enabled ->

            val toggleOnColor = ContextCompat.getColor(this, R.color.toggle_on)
            val toggleOffColor = ContextCompat.getColor(this, R.color.toggle_off)

            binding.switchMaskPopup.isChecked = enabled
            binding.switchMaskPopup.trackTintList = ColorStateList.valueOf(if (enabled) toggleOnColor else toggleOffColor)

        }

        vm.maskPopupToggled.observe(this) {
            showMaskPopupToast()
        }

        vm.btnBackState.observe(this@SettingActivity) {

            if (it) {

                val intent = Intent(this@SettingActivity, MainActivity::class.java)

                startActivityBackAnimation(intent, this@SettingActivity)

            }

        }

    }

    @SuppressLint("RestrictedApi")
    private fun showMaskPopupToast() {
        val customToast = CustomToastPopup(binding.root, layoutInflater)
        customToast.showMaskPopupToast("동영상 마스크 팝업이 활성화되었습니다")
    }

}