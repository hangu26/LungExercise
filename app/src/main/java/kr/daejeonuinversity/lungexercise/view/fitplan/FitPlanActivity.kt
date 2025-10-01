package kr.daejeonuinversity.lungexercise.view.fitplan

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
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

        vm.fetchUserInfo()

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

                val age = vm.userAge.value ?: 0
                val weight = vm.userWeight.value?.toDouble() ?: 0.0
                val latestDistance = vm.latestDistance.value ?: 0.0
                val timer = vm.txEdtTime.value?.toString()?.toInt() ?: 0
                val intensity = vm.txEdtIntensity.value?.toString()?.toDouble() ?: 0.0
                var fitDistance = 0.0

                when {
                    latestDistance == 0.0 -> {
                        Toast.makeText(this, "먼저 6분 보행 검사를 완료해주세요.", Toast.LENGTH_SHORT).show()
                    }

                    timer == 0 || intensity == 0.0 -> {
                        Toast.makeText(this, "운동시간과 운동강도를 올바르게 입력해주세요.", Toast.LENGTH_SHORT).show()
                    }

                    else -> {
                        fitDistance = (latestDistance / 6.0) * timer * (intensity / 100.0)
                    }
                }

                Log.e("값 추적", "$age, $latestDistance")

                intent.putExtra("userAge", age)
                intent.putExtra("userWeight", weight)
                intent.putExtra("latestDistance", latestDistance)
                intent.putExtra("timer", timer)
                intent.putExtra("fitDistance", fitDistance)

                startActivityAnimation(intent, this@FitPlanActivity)
                finish()

            }
        }

    }

    fun calculateFitDistance(
        latestDistance: Double,
        timerMinutes: Int,
        intensityPercent: Double
    ): Double {
        // 1분당 거리 × 운동 시간 × 운동 강도(%)
        return (latestDistance / 6.0) * timerMinutes * (intensityPercent / 100.0)
    }


}