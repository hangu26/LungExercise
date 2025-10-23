package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.data.repository.FitExerciseRepository
import kr.daejeonuinversity.lungexercise.util.util.HeartRateReceiver
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FitExerciseViewModel(
    private val repository: FitExerciseRepository,
    application: Application
) : AndroidViewModel(application) {

    private val _backClicked = MutableLiveData<Boolean>(false)
    val backClicked: LiveData<Boolean> get() = _backClicked

    val txWalkDistance = MutableLiveData<String>("0")

    private val _btnStartState = MutableLiveData<Boolean>()
    val btnStartState: LiveData<Boolean> = _btnStartState

    private val _btnStopState = MutableLiveData<Boolean>()
    val btnStopState: LiveData<Boolean> = _btnStopState

    private val _btnResetState = MutableLiveData<Boolean>()
    val btnResetState: LiveData<Boolean> = _btnResetState

    private val _heartRate = MutableLiveData<Float>()
    val heartRate: LiveData<Float> get() = _heartRate

    private val _heartRateWarning = MutableLiveData<Boolean>()
    val heartRateWarning: LiveData<Boolean> get() = _heartRateWarning

    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int> get() = _stepCount

    private val _spo2 = MutableLiveData<Float>()

    private val _currentWarningCount = MutableLiveData(0)
    val currentWarningCount: LiveData<Int> get() = _currentWarningCount

    private val heartRateList = mutableListOf<Float>()

    private var userWeight: Double? = null
    private var testDurationMinutes: Int = 0
    private var lastElapsedHours = 0.0 // 이전 tick 기준 시간
    private val WARNING_COOLDOWN = 5000L // 5초

    val spo2: LiveData<Float> get() = _spo2

    private var userAge = 0

    private val _calories = MutableLiveData<Double>()
    val calories: LiveData<Double> = _calories

    private val _isEndedState = MutableLiveData<Boolean>()
    val isEndedState: LiveData<Boolean> = _isEndedState

    private val _btnResultState = MutableLiveData<Boolean>()
    val btnResultState: LiveData<Boolean> = _btnResultState

    private var initialStepCount: Int? = null
    private var totalDurationMinutes: Int = 0
    private var elapsedTimeSeconds: Int = 0
    private var accumulatedCalories = 0.0
    private var warningCount = 0
    private val WARNING_THRESHOLD = 2  // 2번 연속 기준
    private var lastWarningTime = 0L

    private val _elapsedTime = MutableLiveData<Int>()
    val elapsedTime: LiveData<Int> get() = _elapsedTime

    init {
        _stepCount.observeForever { steps ->
            val distanceM = steps * 0.7 // 평균 보폭 0.7m
            txWalkDistance.postValue(String.format("%.1f m", distanceM))

            if (userWeight!! > 0 && totalDurationMinutes > 0) {
                calculateCaloriesRealtime(distanceM)
            }
        }
    }

    fun saveFitResultData(time : Int, userDistance : Double, calorie : Double, heartWaringCount : Int, totalWalkCount : Int, date : String) = viewModelScope.launch {

        repository.insertFitResultData(time, userDistance, calorie, heartWaringCount, totalWalkCount, date)

    }

    fun setUserInfo(weight: Double, durationMinutes: Int, age: Int) {
        userWeight = weight
        testDurationMinutes = durationMinutes
        userAge = age
    }

    fun updateElapsedTime(seconds: Int) {
        _elapsedTime.value = seconds
        elapsedTimeSeconds = seconds
        _stepCount.value?.let { steps ->
            val distanceM = steps * 0.7
            calculateCaloriesRealtime(distanceM)
        }
    }

    private fun calculateCaloriesRealtime(distanceM: Double) {
        val weight = userWeight ?: return
        val elapsedHours = elapsedTimeSeconds / 3600.0
        if (elapsedHours <= 0) return

        val distanceKm = distanceM / 1000.0
        val speedKmh = distanceKm / elapsedHours

        val mets = when {
            speedKmh < 2 -> 2.0
            speedKmh < 3 -> 2.8
            speedKmh < 4 -> 3.5
            speedKmh < 5 -> 4.3
            speedKmh < 6 -> 5.0
            speedKmh < 7 -> 6.0
            else -> 7.0
        }

        val kcal = mets * weight * (elapsedHours - lastElapsedHours)
        accumulatedCalories += kcal
        lastElapsedHours = elapsedHours

        _calories.postValue(String.format("%.2f", accumulatedCalories).toDouble())
    }


    fun btnBack() {
        _backClicked.value = true
    }

    fun btnStart() {

        _btnStartState.value = true

    }

    fun btnStop() {

        _btnStopState.value = true

    }

    fun btnReset() {

        _btnResetState.value = true

    }

    fun btnResult() {

        _btnResultState.value = true
        _isEndedState.value = false

    }

    fun isEnded() {

        _isEndedState.value = true

    }

    fun isReset() {

        _isEndedState.value = false
        elapsedTimeSeconds = 0
        _stepCount.postValue(0)
        _calories.postValue(0.0)
        initialStepCount = null
    }

    private val receiver = HeartRateReceiver(application) { data ->
        when (data["type"]) {
            "heart_rate" -> {
                val hr = data["value"] as Float
                _heartRate.postValue(hr)
                addHeartRate(hr)
                checkHeartRateWarning(hr)
            }

            "step_count" -> _stepCount.postValue(data["value"] as Int)
            "spo2" -> _spo2.postValue(data["value"] as Float)

        }

    }

    fun addHeartRate(hr: Float) {
        heartRateList.add(hr)
    }

    fun getAverageHeartRate(durationSeconds: Int): Double {
        // durationSeconds 만큼 최근 값만 사용
        val recent = if (heartRateList.size >= durationSeconds) {
            heartRateList.takeLast(durationSeconds)
        } else {
            heartRateList.toList()
        }
        return if (recent.isNotEmpty()) recent.average() else 0.0
    }

    fun startReceiving() {
        receiver.register()
    }

    fun stopReceiving() {
        receiver.unregister()
    }

    /** 심박수 위험도 알림 **/
    private fun checkHeartRateWarning(currentHR: Float) {
//        val maxHR = 220 - userAge
        val maxHR = 220 - userAge
        if (currentHR >= maxHR * 0.85) {
            warningCount++

            if (warningCount >= WARNING_THRESHOLD) {
                val now = System.currentTimeMillis()
                if (now - lastWarningTime >= WARNING_COOLDOWN) {

                    _currentWarningCount.value = (_currentWarningCount.value ?: 0) + 1

                    _heartRateWarning.postValue(true)

                    val timeStamp = getCurrentDate()

                    viewModelScope.launch {
                        try {
                            repository.insertOrUpdateWarning(timeStamp, currentHR)
                            lastWarningTime = now // 저장한 시각 갱신
                        } catch (e: Exception) {
                            Log.e("FitExerciseViewModel", "DB insert error", e)
                        }
                    }
                }
            }
        } else {
            warningCount = 0
            _heartRateWarning.postValue(false)
        }
    }

    private fun getCurrentDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date())
    }

}