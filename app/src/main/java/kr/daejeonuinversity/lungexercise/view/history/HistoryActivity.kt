package kr.daejeonuinversity.lungexercise.view.history

import android.annotation.SuppressLint
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
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.HistoryViewModel
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.YearMonth

/** HistoryRecordActivity로 대체됨 **/
class HistoryActivity : BaseActivity<ActivityHistoryBinding>(R.layout.activity_history) {

    private var isClickedDate: LocalDate? = null
    private val backPressedCallback = BackPressedCallback(this)

    private val hViewModel: HistoryViewModel by inject()
    private val calendarAdapter = CalendarAdapter { calendarDay ->
        val day = calendarDay.day ?: return@CalendarAdapter
        val yearMonth = hViewModel.currentYearMonth.value ?: YearMonth.now()
        val clickedDate = yearMonth.atDay(day)

        isClickedDate = clickedDate

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
        backPressedCallback.addCallbackActivity(this, MainActivity::class.java)

    }

    @SuppressLint("NotifyDataSetChanged")
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

        vm.btnRemoveClicked.observe(this@HistoryActivity) {
            if (it) {
                isClickedDate?.let { date ->
                    vm.removeClickedData(date)
                    vm.loadWeeklyBreathData(date)
                    calendarAdapter.removedDataSet(date)
                }
                calendarAdapter.notifyDataSetChanged()
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

                binding.constraintTxData.visibility = View.VISIBLE
                binding.barChart.visibility = View.VISIBLE

            } else {
                binding.constraintTxData.visibility = View.GONE
                binding.barChart.visibility = View.GONE
            }

        }
    }

    private fun showBarChart(data: Map<String, Int>) {
        val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
        val entries = data.entries.mapIndexed { index, entry ->
            BarEntry(index.toFloat(), entry.value.toFloat())
        }

        // 클릭된 날짜 문자열
        val clickedDateStr = isClickedDate?.toString()

        // 클릭된 날짜 인덱스 찾기
        val clickedIndex = data.keys.indexOf(clickedDateStr)

        // 색상 리스트 생성 (기본 색상과 클릭된 막대 색상 구분)
        val defaultColor = ContextCompat.getColor(this, R.color.appBar_title_01)
        val clickedColor = ContextCompat.getColor(this, R.color.background_clicked_calendar)

        val colors = entries.mapIndexed { index, _ ->
            if (index == clickedIndex) clickedColor else defaultColor
        }

        val dataSet = BarDataSet(entries, "호흡 횟수").apply {
            this.colors = colors
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
                axisMinimum = 0f
                axisMaximum = 10f
                granularity = 1f
                isGranularityEnabled = true
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


}