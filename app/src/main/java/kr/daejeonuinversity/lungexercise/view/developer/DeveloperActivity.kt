package kr.daejeonuinversity.lungexercise.view.developer

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
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
import kr.daejeonuinversity.lungexercise.data.local.entity.UserInfo
import kr.daejeonuinversity.lungexercise.databinding.ActivityDeveloperBinding
import kr.daejeonuinversity.lungexercise.util.adapter.CalendarAdapter
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.DeveloperViewModel
import kr.daejeonuinversity.lungexercise.viewmodel.HistoryViewModel
import org.koin.android.ext.android.inject
import java.time.LocalDate
import java.time.YearMonth

class DeveloperActivity : BaseActivity<ActivityDeveloperBinding>(R.layout.activity_developer) {

    private val dViewModel : DeveloperViewModel by inject()
    private var isClickedDate: LocalDate? = null
    private val backPressedCallback = BackPressedCallback(this)

    private val calendarAdapter = CalendarAdapter { calendarDay ->
        val day = calendarDay.day ?: return@CalendarAdapter
        val yearMonth = dViewModel.currentYearMonth.value ?: YearMonth.now()
        val clickedDate = yearMonth.atDay(day)

        isClickedDate = clickedDate

        dViewModel.loadWeeklyBreathData(clickedDate)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            activity = this@DeveloperActivity
            viewmodel = dViewModel
            lifecycleOwner = this@DeveloperActivity
            calendarRecyclerView.layoutManager = GridLayoutManager(this@DeveloperActivity, 7)
            calendarRecyclerView.adapter = calendarAdapter
        }

        observe()

    }

    @SuppressLint("NotifyDataSetChanged")
    private fun observe() = dViewModel.let { vm ->

        vm.fetchUserInfo()

        vm.btnBackState.observe(this@DeveloperActivity) {

            if (it) {

                val intent = Intent(this@DeveloperActivity, MainActivity::class.java)
                startActivityBackAnimation(intent, this@DeveloperActivity)
                finish()

            }
        }

        vm.btnMan.observe(this@DeveloperActivity) {
            if (it) {


            }
        }

        vm.genderState.observe(this@DeveloperActivity) { gender ->
            setGenderBackground(gender == "man")
        }

        vm.btnSaveState.observe(this@DeveloperActivity) {
            if (it) {
                val yearStr = binding.edtYear.text.toString()
                val monthStr = binding.edtMonth.text.toString()
                val dayStr = binding.edtDay.text.toString()
                val genderStr = vm.genderState.value ?: "man"
                val weightStr = binding.edtWeight.text.toString()
                val heightStr = binding.edtHeight.text.toString()

                if (yearStr.isBlank() || monthStr.isBlank() || dayStr.isBlank()) {
                    Toast.makeText(this, "년, 월, 일을 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@observe
                }

                if (weightStr.isBlank() || heightStr.isBlank()) {
                    Toast.makeText(this, "몸무게와 키를 모두 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@observe
                }

                if (!isValidDateInput()) {
                    Toast.makeText(this, "올바른 날짜를 입력해주세요.", Toast.LENGTH_SHORT).show()
                    return@observe
                }

                val userInfo = UserInfo(
                    birthday = yearStr + monthStr + dayStr,
                    gender = genderStr,
                    weight = weightStr.toInt(),
                    height = heightStr.toInt()
                )

                vm.saveData(userInfo)

                val intent = Intent(this@DeveloperActivity, MainActivity::class.java)
                startActivityBackAnimation(intent,this@DeveloperActivity)
                finish()

            }
        }

        vm.calendarDays.observe(this@DeveloperActivity) { days ->
            calendarAdapter.submitList(days)
        }

        vm.backClicked.observe(this@DeveloperActivity) {
            if (it) {

                val intent = Intent(this@DeveloperActivity, MainActivity::class.java)
                startActivityAnimation(intent, this@DeveloperActivity)
                finish()

            }
        }

        vm.btnRemoveClicked.observe(this@DeveloperActivity) {
            if (it) {
                isClickedDate?.let { date ->
                    vm.removeClickedData(date)
                    vm.loadWeeklyBreathData(date)
                    calendarAdapter.removedDataSet(date)
                }
                calendarAdapter.notifyDataSetChanged()
            }
        }

        vm.currentYearMonth.observe(this@DeveloperActivity) { yearMonth ->
            val text = "${yearMonth.year}. ${yearMonth.monthValue}"
            binding.tvCurrentMonth.text = text
        }

        vm.fetchBreathData()

        vm.weeklyBarData.observe(this@DeveloperActivity) { weeklyData ->

            showBarChart(weeklyData)

        }

        vm.graphVisibility.observe(this@DeveloperActivity) {

            if (it) {

                binding.barChart.visibility = View.VISIBLE

            } else {
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

    private fun isValidDateInput(): Boolean {
        val yearStr = binding.edtYear.text.toString()
        val monthStr = binding.edtMonth.text.toString()
        val dayStr = binding.edtDay.text.toString()

        if (yearStr.isBlank() || monthStr.isBlank() || dayStr.isBlank()) {
            return false
        }

        val year = yearStr.toIntOrNull() ?: return false
        val month = monthStr.toIntOrNull() ?: return false
        val day = dayStr.toIntOrNull() ?: return false

        val currentYear = LocalDate.now().year
        if (year < 1800 || year > currentYear) return false
        if (month !in 1..12) return false
        if (day !in 1..31) return false

        val maxDay = when (month) {
            1, 3, 5, 7, 8, 10, 12 -> 31
            4, 6, 9, 11 -> 30
            2 -> if (isLeapYear(year)) 29 else 28
            else -> 31
        }

        if (day > maxDay) return false

        return true
    }

    private fun isLeapYear(year: Int): Boolean {
        return (year % 4 == 0 && year % 100 != 0) || (year % 400 == 0)
    }

    private fun setGenderBackground(isManSelected: Boolean) {
        // 남자 버튼 배경 및 텍스트 색상
        if (isManSelected) {
            binding.constraintMan.background =
                ContextCompat.getDrawable(this, R.drawable.border_constraint_gender)
            binding.btnMan.setTextColor(ContextCompat.getColor(this, R.color.white))
        } else {
            binding.constraintMan.background =
                ContextCompat.getDrawable(this, R.drawable.border_constraint_gender_not)
            binding.btnMan.setTextColor(ContextCompat.getColor(this, R.color.black))
        }

        // 여자 버튼 배경 및 텍스트 색상
        if (isManSelected) {
            binding.constraintWoman.background =
                ContextCompat.getDrawable(this, R.drawable.border_constraint_gender_not)
            binding.btnWoman.setTextColor(ContextCompat.getColor(this, R.color.black))
        } else {
            binding.constraintWoman.background =
                ContextCompat.getDrawable(this, R.drawable.border_constraint_gender)
            binding.btnWoman.setTextColor(ContextCompat.getColor(this, R.color.white))
        }
    }

}