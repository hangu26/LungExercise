package kr.daejeonuinversity.lungexercise.view.history.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.FragmentBreathHistoryBinding
import kr.daejeonuinversity.lungexercise.util.adapter.CalendarAdapter
import kr.daejeonuinversity.lungexercise.util.base.BaseFragment
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.HistoryViewModel
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.YearMonth

class BreathHistoryFragment :
    BaseFragment<FragmentBreathHistoryBinding>(R.layout.fragment_breath_history) {

    private val bViewModel: HistoryViewModel by inject()
    private var isClickedDate: LocalDate? = null
    private val calendarAdapter = CalendarAdapter { calendarDay ->
        val day = calendarDay.day ?: return@CalendarAdapter
        val yearMonth = bViewModel.currentYearMonth.value ?: YearMonth.now()
        val clickedDate = yearMonth.atDay(day)

        isClickedDate = clickedDate

        bViewModel.loadWeeklyBreathData(clickedDate)
    }

    override fun initView() {

        binding.apply {
            fragment = this@BreathHistoryFragment
            viewmodel = bViewModel
            calendarRecyclerView.layoutManager = GridLayoutManager(requireContext(), 7)
            calendarRecyclerView.adapter = calendarAdapter
        }

        observe()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observe() = bViewModel.let { vm ->
        vm.calendarDays.observe(viewLifecycleOwner) { days ->
            calendarAdapter.submitList(days)
        }

        vm.btnRemoveClicked.observe(viewLifecycleOwner) {
            if (it) {
                isClickedDate?.let { date ->
                    vm.removeClickedData(date)
                    vm.loadWeeklyBreathData(date)
                    calendarAdapter.removedDataSet(date)
                }
                calendarAdapter.notifyDataSetChanged()
            }
        }

        vm.currentYearMonth.observe(viewLifecycleOwner) { yearMonth ->
            val text = "${yearMonth.monthValue}월 ${yearMonth.year}"
            binding.tvCurrentMonth.text = text
        }

        vm.fetchBreathData()

        vm.weeklyBarData.observe(viewLifecycleOwner) { weeklyData ->

            showBarChart(weeklyData)

        }

        vm.graphVisibility.observe(viewLifecycleOwner) {

            if (it) {

                binding.graphConstraint.visibility = View.VISIBLE

            } else {
                binding.graphConstraint.visibility = View.GONE
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
        val defaultColor = ContextCompat.getColor(requireContext(), R.color.color_barchart_default)
        val clickedColor =
            ContextCompat.getColor(requireContext(), R.color.background_clicked_calendar)

        val colors = entries.mapIndexed { index, _ ->
            if (index == clickedIndex) clickedColor else defaultColor
        }

        val dataSet = BarDataSet(entries, "호흡 횟수").apply {
            this.colors = colors
            valueTextSize = 12f

            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }

        }

        val barData = BarData(dataSet).apply {
            barWidth = 0.9f
        }

        /** y축 데이터중 가장 큰 값 **/
        val maxDataValue = barData.yMax

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
                setDrawAxisLine(false)
            }

            axisRight.isEnabled = false

            axisLeft.apply {
                axisMinimum = 0f
                axisMaximum = (maxDataValue * 1.1f).coerceAtLeast(10f)
                granularity = 1f
                isGranularityEnabled = true
                setDrawGridLines(false)         // Y축 그리드 제거
                setDrawAxisLine(false)          // Y축 라인 제거
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