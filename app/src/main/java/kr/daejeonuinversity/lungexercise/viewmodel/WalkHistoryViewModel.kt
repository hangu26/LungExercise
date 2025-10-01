package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.data.local.BreathDatabase
import kr.daejeonuinversity.lungexercise.data.local.dao.StepIntervalDao
import kr.daejeonuinversity.lungexercise.data.local.entity.StepIntervalEntity
import kr.daejeonuinversity.lungexercise.data.repository.InfoRepository
import kr.daejeonuinversity.lungexercise.data.repository.TotalStepRepository
import kr.daejeonuinversity.lungexercise.model.CalendarDay
import kr.daejeonuinversity.lungexercise.util.util.StepReceiver
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class WalkHistoryViewModel(
    private val dao: StepIntervalDao,
    private val infoRepo: InfoRepository,
    private val repository: TotalStepRepository,
    application: Application
) :
    AndroidViewModel(application) {

    private val _calendarDays = MutableLiveData<List<CalendarDay>>()
    val calendarDays: LiveData<List<CalendarDay>> = _calendarDays

    private val _currentYearMonth = MutableLiveData<YearMonth>(YearMonth.now())
    val currentYearMonth: LiveData<YearMonth> = _currentYearMonth

    val txSelectedTotalDistance: LiveData<String> get() = _txSelectedTotalDistance
    private val _txSelectedTotalDistance = MutableLiveData<String>()

    val txSelectedTotalCalories: LiveData<String> get() = _txSelectedTotalCalories
    private val _txSelectedTotalCalories = MutableLiveData<String>()

    val txRemainSteps: LiveData<String> get() = _txRemainSteps
    private val _txRemainSteps = MutableLiveData<String>()

    val txProgressStep: LiveData<String> get() = _txProgressStep
    private val _txProgressStep = MutableLiveData<String>()

    val txTargetDistance: LiveData<String> get() = _txTargetDistance
    private val _txTargetDistance = MutableLiveData<String>()

    val txSelectedTotalSteps: LiveData<String> get() = _txSelectedTotalSteps
    private val _txSelectedTotalSteps = MutableLiveData<String>()

    val txCurrentSteps: LiveData<String> get() = _txCurrentSteps
    private val _txCurrentSteps = MutableLiveData<String>()

    private val _progressSetup = MutableLiveData<Int>()
    val progressSetup : LiveData<Int> get() = _progressSetup

    private val _recordedDates = MutableLiveData<Set<String>>()

    private var lastClickedDate: LocalDate? = null

    private val _graphVisibility = MutableLiveData<Boolean>(false)
    val graphVisibility = _graphVisibility

    private val TARGET_STEPS = 6000

    private val _stepIntervals = MutableLiveData<List<StepIntervalEntity>>()
    val stepIntervals: LiveData<List<StepIntervalEntity>> get() = _stepIntervals

    // 유저 정보 변수
    private val _userWeight = MutableLiveData<Int>()
    val userWeight: LiveData<Int> get() = _userWeight

    private val _userHeight = MutableLiveData<Int>() // 필요시
    val userHeight: LiveData<Int> get() = _userHeight

    private val _userAge = MutableLiveData<Int>()
    val userAge: LiveData<Int> get() = _userAge

    init {
        generateCalendar()
        _txTargetDistance.postValue("$TARGET_STEPS")
    }

    fun getTodayTotalSteps() {
        viewModelScope.launch(Dispatchers.IO) {
            val today = SimpleDateFormat(
                "yyyy-MM-dd",
                Locale.getDefault()
            ).format(System.currentTimeMillis())
            val intervals = repository.getIntervalsByDate(today)
            val totalSteps = intervals.sumOf { it.steps }
            val achievementRate =
                ((totalSteps.toDouble() / TARGET_STEPS) * 100).toInt().coerceAtMost(100)
            val remaining = (TARGET_STEPS - totalSteps).coerceAtLeast(0)

            _progressSetup.postValue(achievementRate)
            _txCurrentSteps.postValue(totalSteps.toString())
            _txProgressStep.postValue("$achievementRate %")
            _txRemainSteps.postValue("$remaining 걸음 남음")

        }
    }

    fun fetchActivityData() {
        viewModelScope.launch {
            val dates = repository.getAllRecordedDates()
            _recordedDates.value = dates.toSet()
            generateCalendar()
        }
    }

    /** 유저 정보 가져오는 함수 **/
    fun fetchUserInfo() {
        viewModelScope.launch {
            val data = infoRepo.getUserDates()

            val weight = data?.weight?.toInt()
            val height = data?.height?.toInt()
            _userWeight.postValue(weight)
            _userHeight.postValue(height)

        }
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
            val result = repository.getStepsByDate(selectedDate.toString()) // yyyy-MM-dd
            _stepIntervals.postValue(result)

            val totalSteps = result.sumOf { it.steps }
            _txSelectedTotalSteps.postValue("$totalSteps 걸음")

            // 총 이동거리 계산 (평균 보폭 = 키 * 0.415)
            val height = _userHeight.value ?: 170 // 기본값
            val stepLengthCm = height * 0.415
            val totalDistanceKm = (totalSteps * stepLengthCm) / 100000.0f // cm → km

            val totalDistanceStr = if (totalDistanceKm < 1) {
                val totalDistanceM = totalDistanceKm * 1000
                String.format(Locale.getDefault(), "%.1f m", totalDistanceM)
            } else {
                String.format(Locale.getDefault(), "%.1f km", totalDistanceKm)
            }

            _txSelectedTotalDistance.postValue(totalDistanceStr)

            val totalCalories = totalSteps * 0.05f
            val totalCaloriesStr = String.format(Locale.getDefault(), "%.1f kcal", totalCalories)
            _txSelectedTotalCalories.postValue(totalCaloriesStr)

        }
    }


    fun prevMonth() {
        _currentYearMonth.value = _currentYearMonth.value?.minusMonths(1)
        generateCalendar()
    }

    fun nextMonth() {
        _currentYearMonth.value = _currentYearMonth.value?.plusMonths(1)
        generateCalendar()
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