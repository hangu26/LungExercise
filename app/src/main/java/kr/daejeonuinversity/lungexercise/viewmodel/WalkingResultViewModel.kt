package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.data.repository.SixWalkTestRepository
import kr.daejeonuinversity.lungexercise.model.CalendarDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class WalkingResultViewModel(
    private val repository: SixWalkTestRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _currentYearMonth = MutableLiveData<YearMonth>(YearMonth.now())
    val currentYearMonth: LiveData<YearMonth> = _currentYearMonth

    private val _recordedDates = MutableLiveData<Set<String>>()

    private val _calendarDays = MutableLiveData<List<CalendarDay>>()
    val calendarDays: LiveData<List<CalendarDay>> = _calendarDays

    private val _backClicked = MutableLiveData<Boolean>()
    val backClicked: LiveData<Boolean> = _backClicked

    private var lastClickedDate: LocalDate? = null

    private val _graphVisibility = MutableLiveData<Boolean>(false)
    val graphVisibility = _graphVisibility

    val txSelectedTotalDistance: LiveData<String> get() = _txSelectedTotalDistance
    private val _txSelectedTotalDistance = MutableLiveData<String>()

    val txSelectedTotalSteps: LiveData<String> get() = _txSelectedTotalSteps
    private val _txSelectedTotalSteps = MutableLiveData<String>()

    val txSelectedTotalCalories: LiveData<String> get() = _txSelectedTotalCalories
    private val _txSelectedTotalCalories = MutableLiveData<String>()

    init {
        generateCalendar()
    }

    fun loadWeeklyBreathData(selectedDate: LocalDate) {

        /** 버튼 클릭 토글 이벤트 **/
        if (selectedDate == lastClickedDate) {
            _graphVisibility.value = !(_graphVisibility.value ?: false)
        } else {
            lastClickedDate = selectedDate
            _graphVisibility.value = true
        }

        // DB에서 해당 날짜 데이터 불러오기
        viewModelScope.launch {
            val records = repository.getSixDataByDate(selectedDate.toString())

            /** 일간 데이터 **/
            val clickedDateData = records.find { it.date == selectedDate.toString() }

            val totalDistance = clickedDateData?.totalDistance ?: 0.0
            val totalSteps = clickedDateData?.totalSteps
            val calories = clickedDateData?.calories

            val distanceStr = if (totalDistance >= 1000) {
                String.format("%.1f km", totalDistance / 1000)
            } else {
                String.format("%.1f m", totalDistance)
            }

            val caloriesStr = String.format("%.1f kcal", calories)

            val res = application.resources
            if (clickedDateData != null) {
                _txSelectedTotalDistance.postValue(distanceStr)
                _txSelectedTotalSteps.postValue("$totalSteps 걸음")
                _txSelectedTotalCalories.postValue(caloriesStr)
            } else {
                _txSelectedTotalDistance.value = (res.getString(R.string.tx_not_recorded))
                _txSelectedTotalSteps.value = (res.getString(R.string.tx_not_recorded))
                _txSelectedTotalCalories.value = (res.getString(R.string.tx_not_recorded))

            }

        }


    }

    fun btnBack() {
        _backClicked.value = true
    }

    fun btnClose() {

        _backClicked.value = true

    }

    fun prevMonth() {
        _currentYearMonth.value = _currentYearMonth.value?.minusMonths(1)
        generateCalendar()
    }

    fun nextMonth() {
        _currentYearMonth.value = _currentYearMonth.value?.plusMonths(1)
        generateCalendar()
    }

    fun fetchSixWalkData() {
        viewModelScope.launch {
            val dates = repository.getAllRecordedDates()
            _recordedDates.value = dates.toSet()
            generateCalendar()
        }
    }

    private fun generateCalendar() {
        val yearMonth = _currentYearMonth.value ?: YearMonth.now()
        val firstDayOfMonth = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7  // 일요일 = 0

        val recorded = _recordedDates.value ?: emptySet()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val days = mutableListOf<CalendarDay>()

        // 앞에 빈 칸 채우기
        for (i in 0 until startDayOfWeek) {
            days.add(CalendarDay(null))
        }

        for (day in 1..daysInMonth) {
            val dateStr = yearMonth.atDay(day).format(formatter)
            val isRecorded = recorded.contains(dateStr)
            days.add(CalendarDay(day = day, isRecorded = isRecorded))
        }

        _calendarDays.value = days
    }

}