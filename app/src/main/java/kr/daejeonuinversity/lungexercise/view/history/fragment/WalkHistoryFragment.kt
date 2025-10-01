package kr.daejeonuinversity.lungexercise.view.history.fragment

import android.animation.ObjectAnimator
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.data.local.entity.StepIntervalEntity
import kr.daejeonuinversity.lungexercise.databinding.FragmentWalkHistoryBinding
import kr.daejeonuinversity.lungexercise.util.adapter.CalendarAdapter
import kr.daejeonuinversity.lungexercise.util.base.BaseFragment
import kr.daejeonuinversity.lungexercise.viewmodel.WalkHistoryViewModel
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.YearMonth
import java.util.Calendar

class WalkHistoryFragment :
    BaseFragment<FragmentWalkHistoryBinding>(R.layout.fragment_walk_history) {

    private val wViewModel: WalkHistoryViewModel by inject()
    private var isClickedDate: LocalDate? = null
    private var progressAnimator: ObjectAnimator? = null
    private val calendarAdapter = CalendarAdapter { calendarDay ->
        val day = calendarDay.day ?: return@CalendarAdapter
        val yearMonth = wViewModel.currentYearMonth.value ?: YearMonth.now()
        val clickedDate = yearMonth.atDay(day)

        isClickedDate = clickedDate

//        wViewModel.startReceiving()
        wViewModel.loadWeeklyBreathData(clickedDate)
    }

    override fun initView() {

        binding.apply {
            fragment = this@WalkHistoryFragment
            viewmodel = wViewModel
            calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
            calendarRecyclerView.adapter = calendarAdapter
        }

        observe()

    }

    private fun observe() = wViewModel.let { vm ->

        vm.fetchUserInfo()

        vm.getTodayTotalSteps()

        vm.fetchActivityData()

        vm.calendarDays.observe(viewLifecycleOwner) { days ->
            calendarAdapter.submitList(days)
        }

        vm.currentYearMonth.observe(viewLifecycleOwner) { yearMonth ->
            val text = "${yearMonth.monthValue}월 ${yearMonth.year}"
            binding.tvCurrentMonth.text = text
        }

        vm.graphVisibility.observe(viewLifecycleOwner) {

            if (it) {

                binding.graphConstraint.visibility = View.VISIBLE

            } else {
                binding.graphConstraint.visibility = View.GONE
            }

        }

        vm.stepIntervals.observe(viewLifecycleOwner) { intervals ->
            showStepBarChart(intervals)
        }

        vm.progressSetup.observe(viewLifecycleOwner) { progressSetup ->

            startUserProgress(progressSetup.toInt())

        }

    }

    private fun showStepBarChart(intervals: List<StepIntervalEntity>) {
        // 0~23시 전체 시간 단위로 데이터 합산
        val entries = (0..23).map { hour ->
            val stepsSum = intervals.filter { entity ->
                val cal = Calendar.getInstance().apply { timeInMillis = entity.intervalStart }
                val hourOfDay = cal.get(Calendar.HOUR_OF_DAY)
                hourOfDay == hour
            }.sumOf { it.steps }
            BarEntry(hour.toFloat(), stepsSum.toFloat())
        }

        // X축 라벨: 2시간 간격만 표시, 나머지는 빈 문자열
        val xAxisLabels = (0..23).map { hour ->
            if (hour % 2 == 0) "${hour}시" else ""
        }

        val dataSet = BarDataSet(entries, "걸음 수").apply {
            color = ContextCompat.getColor(requireContext(), R.color.color_barchart_default)
            valueTextSize = 12f
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        val barData = BarData(dataSet).apply { barWidth = 0.9f }
        val maxDataValue = barData.yMax

        binding.barChart.apply {
            data = barData
            setFitBars(true)
            description.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                valueFormatter = IndexAxisValueFormatter(xAxisLabels)
                setDrawGridLines(false)
                setDrawAxisLine(false)
            }

            axisRight.isEnabled = false

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = (maxDataValue * 1.1f).coerceAtLeast(10f)
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false)
                setDrawAxisLine(false)
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()
                    }
                }
            }

            setScaleEnabled(false)
            animateY(800)
            invalidate()
        }
    }

    private fun startUserProgress(achievementRate: Int) {
        val progressBar = binding.progressBarAchievement
        progressBar.progress = 0
        progressAnimator?.cancel()

        val duration = (1300L * (achievementRate / 100f)).coerceAtLeast(500F)

        progressAnimator = ObjectAnimator.ofInt(progressBar, "progress", 0, achievementRate).apply {
            this.duration = duration.toLong()
            interpolator = LinearInterpolator()
            start()
        }
    }

}