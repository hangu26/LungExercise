package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.data.local.entity.SixMinuteWalkTest
import kr.daejeonuinversity.lungexercise.data.repository.InfoRepository
import kr.daejeonuinversity.lungexercise.data.repository.SixWalkTestRepository
import kr.daejeonuinversity.lungexercise.util.util.HeartRateReceiver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WalkingTestViewModel(private val repository: InfoRepository, private val wRepository : SixWalkTestRepository, application: Application) : AndroidViewModel(application) {

    private val _backClicked = MutableLiveData<Boolean>()
    val backClicked : LiveData<Boolean> = _backClicked

    private val _btnStartState = MutableLiveData<Boolean>()
    val btnStartState : LiveData<Boolean> = _btnStartState

    private val _btnStopState = MutableLiveData<Boolean>()
    val btnStopState : LiveData<Boolean> = _btnStopState

    private val _btnResetState = MutableLiveData<Boolean>()
    val btnResetState : LiveData<Boolean> = _btnResetState

    private val _btnResultState = MutableLiveData<Boolean>()
    val btnResultState : LiveData<Boolean> = _btnResultState

    private val _isEndedState = MutableLiveData<Boolean>()
    val isEndedState : LiveData<Boolean> = _isEndedState

    private val _heartRate = MutableLiveData<Float>()
    val heartRate: LiveData<Float> get() = _heartRate

    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int> get() = _stepCount

    private val _spo2 = MutableLiveData<Float>()
    val spo2: LiveData<Float> get() = _spo2

    val txWalkDistance = MutableLiveData<String>("0.0m")

    private var userWeight: Double? = null

    private val _calories = MutableLiveData<Double>()
    val calories: LiveData<Double> = _calories

    private var initialStepCount: Int? = null

    init {
        _stepCount.observeForever { count ->
            val distance = count * 0.7 // 평균 보폭 0.7m
            txWalkDistance.postValue(String.format("%.1f m", distance))
            val weight = userWeight ?: return@observeForever
            calculateCalories(weight, distance, 6) // 6분 테스트 기준

        }
    }

    private fun calculateCalories(weightKg: Double, distanceM: Double, i: Int) {
        val distanceKm = distanceM / 1000.0
        val minutes = 6.0
        val hours = minutes / 60.0 // 6분 = 0.1시간

        // 6분 동안 평균 속도 km/h
        val speedKmh = if (hours > 0) distanceKm / hours else 0.0

        // 속도 기반 METs 추정
        val mets = when {
            speedKmh < 2 -> 2.0   // 천천히 걷기
            speedKmh < 3 -> 2.8
            speedKmh < 4 -> 3.5
            speedKmh < 5 -> 4.3
            speedKmh < 6 -> 5.0
            speedKmh < 7 -> 6.0
            else -> 7.0
        }

        // 칼로리 계산: kcal = METs * 체중(kg) * 시간(h)
        val kcal = mets * weightKg * hours
        _calories.postValue(kcal)
    }

    fun btnBack(){
        _backClicked.value = true
    }

    fun btnStart(){

        _btnStartState.value = true

    }

    fun btnStop(){

        _btnStopState.value = true

    }

    fun btnReset(){

        _btnResetState.value = true

    }

    fun btnResult(){

        _btnResultState.value = true

    }

    fun isEnded(){

        _isEndedState.value = true

    }

    fun isReset(){

        _isEndedState.value = false
        _stepCount.postValue(0)
        initialStepCount = null
    }



    private val receiver = HeartRateReceiver(application) { data ->
        when (data["type"]) {
            "heart_rate" -> _heartRate.postValue(data["value"] as Float)
            "step_count" -> _stepCount.postValue(data["value"] as Int)

//            "step_count" -> {
//                val rawCount = data["value"] as Int
//                if (initialStepCount == null) {
//                    // 처음 들어온 값 저장
//                    initialStepCount = rawCount
//                }
//                // 현재 값 - 초기 값 = 세션 걸음 수
//                val sessionCount = rawCount - (initialStepCount ?: 0)
//                _stepCount.postValue(sessionCount)
//            }

            "spo2" -> _spo2.postValue(data["value"] as Float)

        }

    }

    fun startReceiving() {
        receiver.register()
    }

    fun stopReceiving() {
        receiver.unregister()
    }

    fun fetchUserInfo() {

        viewModelScope.launch {

            val data = repository.getUserDates()

            userWeight = data?.weight?.toDouble()
        }
    }

    fun saveData() {
        val distance = txWalkDistance.value?.replace(" m", "")?.toDoubleOrNull() ?: 0.0
        val steps = stepCount.value?.toInt() ?: 0
        val calorie = _calories.value ?: 0.0
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

        viewModelScope.launch {
            val recordsToday = wRepository.getSixDataByDate(currentDate) // 오늘 날짜 데이터
            if (recordsToday.isEmpty()) {
                // 오늘 처음 기록
                val record = SixMinuteWalkTest(
                    totalCount = 1,
                    date = currentDate,
                    totalDistance = distance,
                    latestDistance = distance,
                    totalSteps = steps,
                    calories = calorie
                )
                wRepository.insert(record)
            } else {
                // 오늘 이미 기록이 있으면 누적
                val lastRecord = recordsToday.last()
                val updatedRecord = lastRecord.copy(
                    totalCount = lastRecord.totalCount + 1,
                    totalDistance = lastRecord.totalDistance + distance,
                    latestDistance = distance,
                    totalSteps = lastRecord.totalSteps + steps,
                    calories = lastRecord.calories + calorie
                )
                wRepository.update(updatedRecord)
            }
        }

    }


}