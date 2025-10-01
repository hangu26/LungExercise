package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.data.repository.FitExerciseRepository
import kr.daejeonuinversity.lungexercise.model.CalendarDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class FitResultViewModel(private val repository: FitExerciseRepository, application: Application) :
    AndroidViewModel(application) {

    private val _backClicked = MutableLiveData<Boolean>()
    val backClicked: LiveData<Boolean> = _backClicked

    private val _btnHomeState = MutableLiveData<Boolean>()
    val btnHomeState: LiveData<Boolean> = _btnHomeState

    private val _calendarDays = MutableLiveData<List<CalendarDay>>()
    val calendarDays: LiveData<List<CalendarDay>> = _calendarDays

    private val _currentYearMonth = MutableLiveData<YearMonth>(YearMonth.now())
    val currentYearMonth: LiveData<YearMonth> = _currentYearMonth

    val txWalkDistance = MutableLiveData<String>("0")

    private val _recordedDates = MutableLiveData<Set<String>>()
    val recordedDates: LiveData<Set<String>> get() = _recordedDates

    private val _txTotalTimeValue = MutableLiveData<String>()
    val txTotalTimeValue: LiveData<String> get() = _txTotalTimeValue

    private val _txTotalDistanceValue = MutableLiveData<String>()
    val txTotalDistanceValue: LiveData<String> get() = _txTotalDistanceValue

    private val _graphVisibility = MutableLiveData<Boolean>(false)
    val graphVisibility = _graphVisibility

    private var lastClickedDate: LocalDate? = null

    init {
        generateCalendar()
    }

    fun loadFitExerciseData(selectedDate: LocalDate) {

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

            val records = repository.getFitExerciseByDates(dateStrings)

            /** 일간 데이터 **/
            val clickedDateData = records.find { it.date == selectedDate.toString() }

            val totalTime = clickedDateData?.time ?: 0
            val displayTime = formatTime(totalTime)

            val totalDistance = clickedDateData?.userDistance ?: 0.0
            val displayDistance = formatDistance(totalDistance)

            val res = application.resources
            if (clickedDateData != null) {
                _txTotalTimeValue.value = displayTime
                _txTotalDistanceValue.value = displayDistance
            } else {
                _txTotalTimeValue.value = res.getString(R.string.tx_not_recorded)
                _txTotalDistanceValue.value = res.getString(R.string.tx_not_recorded)
            }

        }

    }

    private fun formatTime(totalMinutes: Int): String {
        return if (totalMinutes < 60) {
            "$totalMinutes 분"
        } else {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            if (minutes == 0) "$hours 시간"
            else "$hours 시간 $minutes 분"
        }
    }

    private fun formatDistance(distanceM: Double): String {
        return if (distanceM < 1000) {
            // 소수점 한 자리까지 표시하고 m 단위
            String.format("%.1f m", distanceM)
        } else {
            val distanceKm = distanceM / 1000.0
            // 소수점 한 자리까지 표시하고 km 단위
            String.format("%.1f km", distanceKm)
        }
    }

    fun btnBack() {
        _backClicked.value = true
    }

    fun btnRetry(){
        _backClicked.value = true
    }

    fun btnHome(){
        _btnHomeState.value = true
    }

    fun fetchFitExerciseData() {
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

    fun prevMonth() {
        _currentYearMonth.value = _currentYearMonth.value?.minusMonths(1)
        generateCalendar()
    }

    fun nextMonth() {
        _currentYearMonth.value = _currentYearMonth.value?.plusMonths(1)
        generateCalendar()
    }

}