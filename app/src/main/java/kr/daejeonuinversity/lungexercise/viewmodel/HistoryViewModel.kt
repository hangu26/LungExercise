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
import kr.daejeonuinversity.lungexercise.data.repository.InfoRepository
import kr.daejeonuinversity.lungexercise.model.CalendarDay
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

class HistoryViewModel(private val repository: BreathRepository, private val userRepository: InfoRepository, application: Application) :
    AndroidViewModel(application) {

    private val _backClicked = MutableLiveData<Boolean>()
    val backClicked: LiveData<Boolean> = _backClicked

    private val _btnRemoveClicked = MutableLiveData<Boolean>()
    val btnRemoveClicked: LiveData<Boolean> = _btnRemoveClicked

    private val _btnExportClicked = MutableLiveData<Boolean>()
    val btnExportClicked: LiveData<Boolean> = _btnExportClicked

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

    private val _avgFvc = MutableLiveData<Double?>()

    private val _avgFev1 = MutableLiveData<Double?>()

    private val _avgFev1Fvc = MutableLiveData<Double?>()

    private val _avgExpPressure = MutableLiveData<Double?>()

    private val _screeningNum = MutableLiveData<String>()

    private val _initial = MutableLiveData<String>()

    private val _visit = MutableLiveData<String>()

    private val _gender = MutableLiveData<String>()

    private val _birth = MutableLiveData<String>()

    private val _ageRange = MutableLiveData<String>()

    private val _height = MutableLiveData<Int>()

    private val _weight = MutableLiveData<Int>()

    private val _smoke = MutableLiveData<String>()

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
            val userData = userRepository.getUserDates()

            _screeningNum.value = userData?.screeningNum
            _initial.value = userData?.initial
            _gender.value = userData?.gender
            if (userData != null) {
                val age = calculateAge(userData.birthday).toString()
                _birth.value = age
                _ageRange.value = calculateAgeGroup(age.toInt())
            }
            _visit.value = userData?.visit
            _height.value = userData?.height
            _weight.value = userData?.weight
            _smoke.value = userData?.smoke

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
                    res.getString(R.string.tx_clear_count_format) + " ${clickedDateData.clear}" + "회" + " / " + (clickedDateData.totalCount) + "회"
                _avgFvc.value = clickedDateData.avgFvc
                _avgFev1.value = clickedDateData.avgFev1
                _avgFev1Fvc.value = clickedDateData.avgFev1Fvc
                _avgExpPressure.value = clickedDateData.avgExpPressure
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

    fun btnExport() {

        _btnExportClicked.value = true

    }

    fun createExcelContent(): List<List<String>> {

        val headers = listOf(
            //  사용자 정보
            "스크리닝 번호",
            "이니셜",
            "성별",
            "만 나이",
            "연령대",
            "방문일",
            "키(cm)",
            "몸무게(kg)",
            "흡연 여부",

            //  호흡 기록 요약
            "총횟수",
            "평균시간(s)",
            "총시간(s)",
            "클리어/총횟수",
            "평균 FVC",
            "평균 FEV1",
            "평균 FEV1/FVC",
            "평균 호기압력"
        )

        val values = listOf(
            //  사용자 정보
            _screeningNum.value ?: "",
            _initial.value ?: "",
            _gender.value ?: "",
            _birth.value ?: "",        // 이미 String
            _ageRange.value ?: "",
            _visit.value ?: "",
            _height.value?.toString() ?: "",
            _weight.value?.toString() ?: "",
            _smoke.value?.toString() ?: "",

            //  호흡 기록 요약
            _txTotalCount.value ?: "",
            _txAverageTime.value ?: "",
            _txTotalTime.value ?: "",
            _txClearCount.value ?: "",
            _avgFvc.value?.toString() ?: "",
            _avgFev1.value?.toString() ?: "",
            _avgFev1Fvc.value?.toString() ?: "",
            _avgExpPressure.value?.toString() ?: ""
        )

        val data = mutableListOf<List<String>>()
        data.add(headers)

        // 값이 전부 비어있으면 빈 행 추가
        if (values.all { it.isBlank() }) {
            data.add(List(headers.size) { "" })
        } else {
            data.add(values)
        }

        return data
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

    fun calculateAge(birth: String): Int {
        // birth: "20000101"
        val birthYear = birth.substring(0, 4).toInt()
        val birthMonth = birth.substring(4, 6).toInt()
        val birthDay = birth.substring(6, 8).toInt()

        val today = LocalDate.now()

        var age = today.year - birthYear

        // 아직 생일 안 지났으면 -1
        if (today.monthValue < birthMonth ||
            (today.monthValue == birthMonth && today.dayOfMonth < birthDay)
        ) {
            age--
        }

        return age
    }

    fun calculateAgeGroup(age: Int): String {
        return when (age) {
            in 0..9 -> "10대 미만"
            in 10..19 -> "10대"
            in 20..29 -> "20대"
            in 30..39 -> "30대"
            in 40..49 -> "40대"
            in 50..59 -> "50대"
            in 60..69 -> "60대"
            else -> "70대 이상"
        }
    }

}