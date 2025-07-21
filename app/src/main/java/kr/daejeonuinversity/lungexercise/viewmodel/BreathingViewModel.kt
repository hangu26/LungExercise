package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.data.repository.BreathRepository
import kr.daejeonuinversity.lungexercise.model.StopStartState
import kr.daejeonuinversity.lungexercise.util.event.Event
import kr.daejeonuinversity.lungexercise.util.event.ResultEvent

class BreathingViewModel(private val repository: BreathRepository, application: Application) :
    AndroidViewModel(application) {

    private val _breathingState = MutableLiveData<StopStartState?>(null)
    val breathingState: LiveData<StopStartState?> = _breathingState

    private val _backClicked = MutableLiveData<Boolean>()
    val backClicked: LiveData<Boolean> = _backClicked

    private val _normalTime = MutableLiveData<String>()
    val normalTime: LiveData<String> get() = _normalTime

    private val _userTime = MutableLiveData<String>()
    val userTime: LiveData<String> get() = _userTime


    private val _btnResultClicked = MutableLiveData<Boolean>()
    val btnResultClicked: LiveData<Boolean> = _btnResultClicked

    private val _isResultAvailable = MutableLiveData<Boolean>(false)
    val isResultAvailable = _isResultAvailable

    private val _resultEvent = MutableLiveData<Event<ResultEvent>>()
    val resultEvent: LiveData<Event<ResultEvent>> = _resultEvent

    private var countJob: Job? = null
    private var userCountJob: Job? = null

    fun saveBreathData(normalSeconds : Int,userSeconds: Int, date: String) = viewModelScope.launch {
        val clearCount = if (normalSeconds <= userSeconds) 1 else 0
        repository.insertOrUpdateBreathRecord(userSeconds, clearCount, date)
    }

    fun startCounting(totalSeconds: Int) {
        countJob?.cancel()
        countJob = viewModelScope.launch {
            for (i in 0 until totalSeconds + 1) {
                /** 테스트로 인해 _normalTime 잠시 주석 **/
//                _normalTime.postValue(" $i 초")
                delay(1000L)
            }
            _isResultAvailable.value = true
//            _breathingState.value = StopStartState.END
        }
    }

    fun startUserCounting(userSeconds: Int) {
        userCountJob?.cancel()
        userCountJob = viewModelScope.launch {
            for (i in 0 until userSeconds + 1) {
                _userTime.postValue(" $i 초")
                delay(1000L)
            }

        }
    }

    fun stopCounting() {
        countJob?.cancel()
        _normalTime.postValue(" 정지됨")
        _userTime.postValue(" 정지됨")
        _isResultAvailable.value = false
    }

    fun btnBack() {
        _backClicked.value = true
    }

    fun btnStart() {
        _breathingState.value = when (_breathingState.value) {
            StopStartState.STOP -> StopStartState.START
            StopStartState.START -> StopStartState.STOP
            StopStartState.END -> StopStartState.END
            null -> StopStartState.STOP
        }

    }

    /** 결과 확인 버튼 **/
    fun btnResult() {

        if (_isResultAvailable.value == true) {

            _resultEvent.value = Event(ResultEvent.ShowResultDialog)

        } else {

            _resultEvent.value = Event(ResultEvent.ShowResultToast)

        }
    }

}