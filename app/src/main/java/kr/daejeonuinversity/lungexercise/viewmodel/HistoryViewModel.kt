package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.data.repository.BreathRepository
import kr.daejeonuinversity.lungexercise.model.CalendarDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class HistoryViewModel(private val repository: BreathRepository, application: Application) :
    AndroidViewModel(application) {

    private val _backClicked = MutableLiveData<Boolean>()
    val backClicked: LiveData<Boolean> = _backClicked

    private val _calendarDays = MutableLiveData<List<CalendarDay>>()
    val calendarDays: LiveData<List<CalendarDay>> = _calendarDays

    private val _currentYearMonth = MutableLiveData<YearMonth>(YearMonth.now())
    val currentYearMonth: LiveData<YearMonth> = _currentYearMonth

    private val _recordedDates = MutableLiveData<Set<String>>()
    val recordedDates: LiveData<Set<String>> get() = _recordedDates

    private val _weeklyBarData = MutableLiveData<Map<String, Int>>()
    val weeklyBarData: LiveData<Map<String, Int>> = _weeklyBarData

    private val _graphVisibility = MutableLiveData<Boolean>(false)
    val graphVisibility = _graphVisibility

    init {
        generateCalendar()
    }

    fun prevMonth() {
        _currentYearMonth.value = _currentYearMonth.value?.minusMonths(1)
        generateCalendar()
    }

    fun nextMonth() {
        _currentYearMonth.value = _currentYearMonth.value?.plusMonths(1)
        generateCalendar()
    }

    /** 그래프를 위한 주간 호흡 연습 횟수 가져오기
     * 특정 날짜 클릭 시, 호출**/
    fun loadWeeklyBreathData(selectedDate: LocalDate) {

        _graphVisibility.value = _graphVisibility.value == false

        viewModelScope.launch {
            val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)
            val datesOfWeek = (0..6).map { startOfWeek.plusDays(it.toLong()) }
            val dateStrings = datesOfWeek.map { it.toString() } // "yyyy-MM-dd"

            val records = repository.getBreathRecordsByDates(dateStrings)
            val map = dateStrings.associateWith { date ->
                records.find { it.date == date }?.totalCount ?: 0
            }
            _weeklyBarData.postValue(map)
        }
    }

    fun fetchBreathData() {
        viewModelScope.launch {
            val dates = repository.getAllRecordedDates()
            _recordedDates.value = dates.toSet()
            generateCalendar() // 🔥 데이터를 받은 후 달력 다시 생성
        }
    }

    fun btnBack() {
        _backClicked.value = true
    }

    private fun generateCalendar() {
        val yearMonth = _currentYearMonth.value ?: YearMonth.now()
        val firstDayOfMonth = yearMonth.atDay(1)
        val daysInMonth = yearMonth.lengthOfMonth()
        val startDayOfWeek = firstDayOfMonth.dayOfWeek.value

        val recorded = _recordedDates.value ?: emptySet()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val days = mutableListOf<CalendarDay>()

        for (i in 1 until startDayOfWeek) {
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