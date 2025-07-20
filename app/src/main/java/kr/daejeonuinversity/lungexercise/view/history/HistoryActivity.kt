package kr.daejeonuinversity.lungexercise.view.history

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityHistoryBinding
import kr.daejeonuinversity.lungexercise.util.adapter.CalendarAdapter
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.HistoryViewModel
import org.koin.android.ext.android.inject
import java.time.YearMonth

class HistoryActivity : BaseActivity<ActivityHistoryBinding>(R.layout.activity_history) {

    private val hViewModel: HistoryViewModel by inject()
    private val calendarAdapter = CalendarAdapter { calendarDay ->
        val day = calendarDay.day ?: return@CalendarAdapter
        val yearMonth = hViewModel.currentYearMonth.value ?: YearMonth.now()
        val clickedDate = yearMonth.atDay(day)

        hViewModel.loadWeeklyBreathData(clickedDate)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@HistoryActivity
            viewmodel = hViewModel
            lifecycleOwner = this@HistoryActivity
            calendarRecyclerView.layoutManager = GridLayoutManager(this@HistoryActivity, 7)
            calendarRecyclerView.adapter = calendarAdapter
        }

        observe()
    }

    private fun observe() = hViewModel.let { vm ->
        vm.calendarDays.observe(this@HistoryActivity) { days ->
            calendarAdapter.submitList(days)
        }

        vm.backClicked.observe(this@HistoryActivity) {
            if (it) {

                val intent = Intent(this@HistoryActivity, MainActivity::class.java)
                startActivityAnimation(intent, this@HistoryActivity)
                finish()

            }
        }

        vm.currentYearMonth.observe(this@HistoryActivity) { yearMonth ->
            val text = "${yearMonth.year}. ${yearMonth.monthValue}"
            binding.tvCurrentMonth.text = text
        }

        vm.fetchBreathData()

        vm.weeklyBarData.observe(this@HistoryActivity) { weeklyData ->

            showBarChart(weeklyData)

        }

        vm.graphVisibility.observe(this@HistoryActivity) {

            if (it) {

                binding.barChart.visibility = View.VISIBLE

            } else {
                binding.barChart.visibility = View.GONE
            }

        }
    }

    private fun showBarChart(data: Map<String, Int>) {
        val dayLabels = listOf("일", "월", "화", "수", "목", "금", "토")

        val entries = data.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        val dataSet = BarDataSet(entries, "호흡 횟수").apply {
            color = ContextCompat.getColor(this@HistoryActivity, R.color.appBar_title_01)
            valueTextSize = 12f
        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.9f
        }

        binding.barChart.apply {
            this.data = barData
            setFitBars(true)
            description.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                isGranularityEnabled = true
                valueFormatter = IndexAxisValueFormatter(dayLabels)
                setDrawGridLines(false)
            }

            axisRight.isEnabled = false

            axisLeft.apply {
                axisMinimum = 0f               // Y축 최소값을 1로 설정
                axisMaximum = 10f
                granularity = 1f              // 눈금 간격 1
                isGranularityEnabled = true
                valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return value.toInt().toString()   // 정수만 표시
                    }
                }
            }

            setScaleEnabled(false)
            animateY(800)
            invalidate()
        }
    }


}