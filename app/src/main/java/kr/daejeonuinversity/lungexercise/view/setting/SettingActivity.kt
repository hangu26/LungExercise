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
        val snackbar = Snackbar.make(binding.root, "", Snackbar.LENGTH_SHORT)
        val customLayout = layoutInflater.inflate(R.layout.custom_mask_popup_toast, null)
        customLayout.findViewById<TextView>(R.id.toast_text_icon_mask_popup).text =
            "동영상 마스크 팝업이 활성화되었습니다"

        val snackbarLayout = snackbar.view as Snackbar.SnackbarLayout
        snackbarLayout.apply {
            background = null
            elevation = 0f
            setPadding(0, 0, 0, 0)
            layoutParams = (layoutParams as FrameLayout.LayoutParams).apply {
                width = FrameLayout.LayoutParams.WRAP_CONTENT
                gravity = Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM
                bottomMargin = 100
            }
            addView(customLayout, 0)
        }
        snackbar.duration = 350
        snackbar.show()
    }

}