package kr.daejeonuinversity.lungexercise.view.walkingtest.result

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityWalkingResultBinding
import kr.daejeonuinversity.lungexercise.util.adapter.CalendarAdapter
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.view.walkingtest.WalkingTestActivity
import kr.daejeonuinversity.lungexercise.viewmodel.WalkingResultViewModel
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.YearMonth

class WalkingResultActivity :
    BaseActivity<ActivityWalkingResultBinding>(R.layout.activity_walking_result) {

    private val wViewModel: WalkingResultViewModel by inject()
    private val backPressedCallback = BackPressedCallback(this)
    private var isClickedDate: LocalDate? = null

    private val calendarAdapter = CalendarAdapter { calendarDay ->
        val day = calendarDay.day ?: return@CalendarAdapter
        val yearMonth = wViewModel.currentYearMonth.value ?: YearMonth.now()
        val clickedDate = yearMonth.atDay(day)

        isClickedDate = clickedDate

        wViewModel.loadWeeklyBreathData(clickedDate)
    }

    private var distance = ""
    private var calories = 0.0
    private var steps = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            activity = this@WalkingResultActivity
            viewmodel = wViewModel
            lifecycleOwner = this@WalkingResultActivity
        }

        init()
        observe()
        backPressedCallback.addCallbackActivity(this, WalkingTestActivity::class.java)

    }

    @SuppressLint("SetTextI18n")
    private fun init(){

        distance = intent.getStringExtra("distance").toString()
        calories = intent.getDoubleExtra("calories", 0.0)
        steps = intent.getIntExtra("steps", 0)

        binding.apply {
            calendarRecyclerView.layoutManager = GridLayoutManager(this@WalkingResultActivity, 7)
            calendarRecyclerView.adapter = calendarAdapter
            txDistanceData.text = distance
            txUserCalorieData.text = "$calories kcal"
            txStepsData.text = "$steps"
        }

    }

    private fun observe() = wViewModel.let { vm ->

        vm.fetchSixWalkData()

        vm.calendarDays.observe(this@WalkingResultActivity) { days ->
            calendarAdapter.submitList(days)
        }

        vm.currentYearMonth.observe(this@WalkingResultActivity) { yearMonth ->
            val text = "${yearMonth.monthValue}월 ${yearMonth.year}"
            binding.tvCurrentMonth.text = text
        }

        vm.backClicked.observe(this@WalkingResultActivity) {
            if (it) {
                val intent = Intent(this@WalkingResultActivity, WalkingTestActivity::class.java)
                startActivityBackAnimation(intent, this@WalkingResultActivity)
                finish()
            }
        }

        vm.graphVisibility.observe(this@WalkingResultActivity) {

            if (it) {

                binding.graphConstraint.visibility = View.VISIBLE

            } else {
                binding.graphConstraint.visibility = View.GONE
            }

        }

    }

}