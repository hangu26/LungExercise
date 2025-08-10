package kr.daejeonuinversity.lungexercise.viewmodel

import android.Manifest
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.data.repository.BreathRepository
import kr.daejeonuinversity.lungexercise.model.StopStartState
import kr.daejeonuinversity.lungexercise.util.event.Event
import kr.daejeonuinversity.lungexercise.util.event.ExhaleEvent
import kr.daejeonuinversity.lungexercise.util.event.ResultEvent
import kr.daejeonuinversity.lungexercise.util.util.MaskBluetoothManager
import java.io.IOException
import java.util.UUID

// BreathingViewModel.kt
// BreathingViewModel.kt
class BreathingViewModel(private val repository: BreathRepository, application: Application) :
    AndroidViewModel(application), MaskBluetoothManager.BreathingEventListener {

    private val _btnStartState = MutableLiveData<Boolean>(false)
    val btnStartState: LiveData<Boolean> get() = _btnStartState

    private val _btnStopState = MutableLiveData<Boolean>(false)
    val btnStopState: LiveData<Boolean> get() = _btnStopState

    private val _backClicked = MutableLiveData<Boolean>(false)
    val backClicked: LiveData<Boolean> get() = _backClicked

    private val _userTime = MutableLiveData<String>("0 초")
    val userTime: LiveData<String> get() = _userTime

    private val _isResultAvailable = MutableLiveData(false)
    val isResultAvailable: LiveData<Boolean> get() = _isResultAvailable

    private val _resultEvent = MutableLiveData<Event<ResultEvent>>()
    val resultEvent: LiveData<Event<ResultEvent>> get() = _resultEvent

    private val _exhaleEvent = MutableLiveData<Event<ExhaleEvent>>()
    val exhaleEvent: LiveData<Event<ExhaleEvent>> get() = _exhaleEvent

    private var exhaleCountJob: Job? = null

    private var isStarted = false

    init {
        MaskBluetoothManager.setBreathingEventListener(this)
    }

    fun btnStart() {
        isStarted = true
        _btnStartState.value = true
        _isResultAvailable.value = false
        _userTime.value = "0 초"
    }

    fun btnStop() {
        isStarted = false
        _btnStopState.value = true
        stopExhaleCounting()
    }

    fun btnBack() {
        _backClicked.value = true
    }

    fun btnResult() {
        if (_isResultAvailable.value == true) {
            _resultEvent.value = Event(ResultEvent.ShowResultDialog)
        } else {
            _resultEvent.value = Event(ResultEvent.ShowResultToast)
        }
    }

    override fun onExhaleStart() {
        if (!isStarted) return
        _exhaleEvent.postValue(Event(ExhaleEvent.Start))
        startExhaleCounting()
    }

    override fun onExhaleEnd(durationMs: Long) {
        if (!isStarted) return
        _exhaleEvent.postValue(Event(ExhaleEvent.End(durationMs)))
        stopExhaleCounting()
        _isResultAvailable.postValue(true)
        isStarted = false
    }

    private fun startExhaleCounting() {
        stopExhaleCounting()
        exhaleCountJob = viewModelScope.launch {
            var seconds = 0
            while (true) {
                _userTime.postValue("$seconds 초")
                seconds++
                delay(1000L)
            }
        }
    }

    private fun stopExhaleCounting() {
        exhaleCountJob?.cancel()
        exhaleCountJob = null
    }

    fun saveBreathData(normalSeconds: Int, userSeconds: Int, date: String) = viewModelScope.launch {
        val clearCount = if (normalSeconds <= userSeconds) 1 else 0
        repository.insertOrUpdateBreathRecord(userSeconds, clearCount, date)
    }

    fun disconnect() {
        MaskBluetoothManager.disconnect()
    }

    open class Event<out T>(private val content: T) {

        private var hasBeenHandled = false

        fun getContentIfNotHandled(): T? {
            return if (hasBeenHandled) null else {
                hasBeenHandled = true
                content
            }
        }

        fun peekContent(): T = content
    }
}
