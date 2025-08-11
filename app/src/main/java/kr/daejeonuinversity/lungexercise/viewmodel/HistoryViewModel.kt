package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.R
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

    private val _btnRemoveClicked = MutableLiveData<Boolean>()
    val btnRemoveClicked: LiveData<Boolean> = _btnRemoveClicked

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

    private val _txAverageTime = MutableLiveData<String>()
    val txAverageTime: LiveData<String> get() = _txAverageTime

    private val _txTotalCount = MutableLiveData<String>()
    val txTotalCount: LiveData<String> get() = _txTotalCount

    private val _txTotalTime = MutableLiveData<String>()
    val txTotalTime: LiveData<String> get() = _txTotalTime

    private val _txClearCount = MutableLiveData<String>()
    val txClearCount: LiveData<String> get() = _txClearCount

    private var lastClickedDate: LocalDate? = null

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

        /** 버튼 클릭 토글 이벤트. **/
        if (selectedDate == lastClickedDate) {
            _graphVisibility.value = !(_graphVisibility.value ?: false)
        } else {
            lastClickedDate = selectedDate
            _graphVisibility.value = true
        }

        /** 데이터 가져오기 **/
        viewModelScope.launch {
            val startOfWeek = selectedDate.with(DayOfWeek.MONDAY)
            val datesOfWeek = (0..6).map { startOfWeek.plusDays(it.toLong()) }
            val dateStrings = datesOfWeek.map { it.toString() } // "yyyy-MM-dd"

            val records = repository.getBreathRecordsByDates(dateStrings)

            /** 주간 데이터 **/
            val map = dateStrings.associateWith { date ->
                records.find { it.date == date }?.totalCount ?: 0
            }

            /** 일간 데이터 **/
            val clickedDateData = records.find { it.date == selectedDate.toString() }

            val res = application.resources
            if (clickedDateData != null) {
                _txTotalCount.value =
                    res.getString(R.string.tx_total_count_format, clickedDateData.totalCount)
                _txAverageTime.value =
                    res.getString(R.string.tx_average_time_format, clickedDateData.average / 1000)
                _txTotalTime.value =
                    res.getString(R.string.tx_total_time_format, clickedDateData.totalTime / 1000)
                _txClearCount.value =
                    res.getString(R.string.tx_clear_count_format) + (clickedDateData.clear) + "회" + "/" + (clickedDateData.totalCount) + "회"
            } else {
                _txTotalCount.value = res.getString(R.string.tx_total_count_empty)
                _txAverageTime.value = res.getString(R.string.tx_average_time_empty)
                _txTotalTime.value = res.getString(R.string.tx_total_time_empty)
                _txClearCount.value = res.getString(R.string.tx_clear_count_empty)
            }

            _weeklyBarData.postValue(map)

        }

    }

    fun fetchBreathData() {
        viewModelScope.launch {
            val dates = repository.getAllRecordedDates()
            _recordedDates.value = dates.toSet()
            generateCalendar()
        }
    }

    fun btnBack() {
        _backClicked.value = true
    }

    fun btnRemove() {

        _btnRemoveClicked.value = true


    }

    fun removeClickedData(isClickedDate: LocalDate) {

        viewModelScope.launch {

            repository.removeClickedData(isClickedDate)

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