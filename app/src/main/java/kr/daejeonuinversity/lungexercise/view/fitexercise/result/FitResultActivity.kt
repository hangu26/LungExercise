package kr.daejeonuinversity.lungexercise.view.fitexercise.result

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.GridLayoutManager
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityFitResultBinding
import kr.daejeonuinversity.lungexercise.util.adapter.CalendarAdapter
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.fitexercise.FitExerciseActivity
import kr.daejeonuinversity.lungexercise.view.fitplan.FitPlanActivity
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.FitResultViewModel
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.YearMonth
import kotlin.math.roundToInt

class FitResultActivity : BaseActivity<ActivityFitResultBinding>(R.layout.activity_fit_result) {

    private val fViewModel: FitResultViewModel by inject()
    private val backPressedCallback = BackPressedCallback(this)
    private var progressAnimator: ObjectAnimator? = null
    private var isClickedDate: LocalDate? = null

    private val calendarAdapter = CalendarAdapter { calendarDay ->
        val day = calendarDay.day ?: return@CalendarAdapter
        val yearMonth = fViewModel.currentYearMonth.value ?: YearMonth.now()
        val clickedDate = yearMonth.atDay(day)

        isClickedDate = clickedDate

        binding.scrollView.post {
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }

        fViewModel.loadFitExerciseData(clickedDate)
    }

    private lateinit var args: FitResultArgs
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            activity = this@FitResultActivity
            viewmodel = fViewModel
            lifecycleOwner = this@FitResultActivity
        }

        args = FitResultArgs.fromIntent(intent)
        init()
        observe()
        backPressedCallback.intentCallbackActivity(
            this,
            FitExerciseActivity::class.java,
            args
        )
    }

    @SuppressLint("SetTextI18n")
    private fun init() {

        binding.apply {
            riskSeekBar.progress = args.currentWarningCount.coerceIn(0, binding.riskSeekBar.max)
            calendarRecyclerView.layoutManager = GridLayoutManager(this@FitResultActivity, 7)
            calendarRecyclerView.adapter = calendarAdapter
        }

        val fitDistanceKm = args.fitDistance / 1000.0
        val distanceM = args.userDistance.toDouble()
        val distanceKm = distanceM / 1000.0
        val avgHeartRate = args.avgHeartRate
        val avgHeartRateValue = String.format("%.1f", avgHeartRate)


        // 목표 달성률 계산
        val achievementRate = if (fitDistanceKm > 0) (distanceKm / fitDistanceKm) * 100 else 0.0
        val achievementPercent = achievementRate.roundToInt().coerceAtMost(100)

        // 거리 표시
        val fitDistanceText = formatDistanceKm(fitDistanceKm)
        val userDistanceText = if (distanceM < 1000) {
            // 1km 미만 → m 단위
            String.format("%.1f m", distanceM)
        } else {
            // 1km 이상 → km 단위
            String.format("%.1f km", distanceKm)
        }

        val startDistance = if (fitDistanceKm < 1) "0m" else "0km"

        val elapsedSeconds = intent.getIntExtra("elapsedSeconds", 0)

        // UI 업데이트
        binding.apply {
            txAchievementValue.text = "$achievementPercent %"
            txTargetDistance.text = "목표 : $fitDistanceText"
            txStartDistance.text = startDistance
            txRiskPulseCount.text = "${args.currentWarningCount}회"
            txTotalFitTime.text = formatSecondsToTime(elapsedSeconds)
            txCureentDate.text = args.currentDate
            txDistanceValue.text = userDistanceText
            txCalorieValue.text = args.userCalories.toString()
            txStepCount.text = args.userSteps.toString()
            txAverageHeartRate.text = avgHeartRateValue
        }

        startUserProgress(achievementPercent)
    }

    private fun formatSecondsToTime(seconds: Int): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60

        return buildString {
            if (hours > 0) append("${hours}시간 ")
            if (minutes > 0) append("${minutes}분 ")
            if (secs > 0 || (hours == 0 && minutes == 0)) append("${secs}초")
        }.trim()
    }


    private fun observe() = fViewModel.let { vm ->

        vm.calendarDays.observe(this@FitResultActivity) { days ->
            calendarAdapter.submitList(days)
        }

        vm.currentYearMonth.observe(this@FitResultActivity) { yearMonth ->
            val text = "${yearMonth.monthValue}월 ${yearMonth.year}"
            binding.tvCurrentMonth.text = text
        }

        vm.fetchFitExerciseData()

        vm.graphVisibility.observe(this@FitResultActivity) {

            if (it) {

                binding.graphConstraint.visibility = View.VISIBLE

            } else {
                binding.graphConstraint.visibility = View.GONE
            }

        }

        vm.backClicked.observe(this@FitResultActivity) {
            if (it) {
                val intent = Intent(this@FitResultActivity, FitExerciseActivity::class.java).apply {
                    putExtra("userAge", args.age)
                    putExtra("userWeight", args.weight)
                    putExtra("latestDistance", args.latestDistance)
                    putExtra("timer", args.timer)
                    putExtra("fitDistance", args.fitDistance)
                }
                startActivityBackAnimation(intent, this@FitResultActivity)
                finish()
            }
        }

        vm.btnHomeState.observe(this@FitResultActivity){

            if (it){

                val intent = Intent(this@FitResultActivity, MainActivity::class.java)
                startActivityAnimation(intent,this@FitResultActivity)
                finish()

            }
        }

    }

    private fun startUserProgress(achievementRate: Int) {
        val progressBar = binding.progressBarAchievement
        progressBar.progress = 0
        progressAnimator?.cancel()
        progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, achievementRate).apply {
            duration = 1300L
            interpolator = LinearInterpolator()
            start()
        }
    }

}