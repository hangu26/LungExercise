package kr.daejeonuinversity.lungexercise.view.fitplan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityFitPlanBinding
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.exercise.LungExerciseActivity
import kr.daejeonuinversity.lungexercise.view.fitexercise.FitExerciseActivity
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.FitPlanViewModel
import org.koin.android.ext.android.inject

class FitPlanActivity : BaseActivity<ActivityFitPlanBinding>(R.layout.activity_fit_plan) {

    private val backPressedCallback = BackPressedCallback(this)
    private val fViewModel: FitPlanViewModel by inject()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@FitPlanActivity
            lifecycleOwner = this@FitPlanActivity
            viewmodel = fViewModel
        }

        backPressedCallback.addCallbackActivity(this, MainActivity::class.java)

        observe()

    }

    private fun observe() = fViewModel.let { vm ->

        vm.backClicked.observe(this@FitPlanActivity) {
            if (it) {
                val intent = Intent(this@FitPlanActivity, MainActivity::class.java)
                startActivityBackAnimation(intent, this@FitPlanActivity)
                finish()
            }
        }

        vm.isExecuteEnabled.observe(this@FitPlanActivity) { enabled ->
            if (enabled) {
                binding.clExecuteExercise.background = ContextCompat.getDrawable(
                    this@FitPlanActivity,
                    R.drawable.border_btn_next_able
                )
                binding.clExecuteExercise.isClickable = true
            } else {
                binding.clExecuteExercise.background = ContextCompat.getDrawable(
                    this@FitPlanActivity,
                    R.drawable.border_btn_next_unable
                )
                binding.clExecuteExercise.isClickable = false
            }
        }

        // 버튼 클릭 이벤트
        vm.executeClicked.observe(this@FitPlanActivity) {
            if (it) {

                val intent = Intent(this@FitPlanActivity, FitExerciseActivity::class.java)
                startActivityAnimation(intent, this@FitPlanActivity)
                finish()

            }
        }

    }


}