package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kr.daejeonuinversity.lungexercise.util.util.HeartRateReceiver

class WalkingTestViewModel(application: Application) : AndroidViewModel(application) {

    private val _backClicked = MutableLiveData<Boolean>()
    val backClicked : LiveData<Boolean> = _backClicked

    private val _btnStartState = MutableLiveData<Boolean>()
    val btnStartState : LiveData<Boolean> = _btnStartState

    private val _btnStopState = MutableLiveData<Boolean>()
    val btnStopState : LiveData<Boolean> = _btnStopState

    private val _heartRate = MutableLiveData<Float>()
    val heartRate: LiveData<Float> get() = _heartRate

    private val _stepCount = MutableLiveData<Int>()
    val stepCount: LiveData<Int> get() = _stepCount

    private val _spo2 = MutableLiveData<Float>()
    val spo2: LiveData<Float> get() = _spo2

    val txWalkDistance = MutableLiveData<String>()

    init {
        _stepCount.observeForever { count ->
            val distance = count * 0.7 // 평균 보폭 0.7m
            txWalkDistance.postValue("현재 보행 거리 : ${String.format("%.1f m", distance)}")
        }
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

    private val receiver = HeartRateReceiver(application) { data ->
        when (data["type"]) {
            "heart_rate" -> _heartRate.postValue(data["value"] as Float)
            "step_count" -> _stepCount.postValue(data["value"] as Int)
            "spo2" -> _spo2.postValue(data["value"] as Float)
        }
    }

    fun startReceiving() {
        receiver.register()
    }

    fun stopReceiving() {
        receiver.unregister()
    }

}