package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kr.daejeonuinversity.lungexercise.data.model.StopStartState

class BreathingViewModel(application: Application) : AndroidViewModel(application) {

    private val _breathingState = MutableLiveData<StopStartState?>(null)
    val breathingState: LiveData<StopStartState?> = _breathingState

    private val _backClicked = MutableLiveData<Boolean>()
    val backClicked: LiveData<Boolean> = _backClicked



    private val _btnResultClicked = MutableLiveData<Boolean>()
    val btnResultClicked: LiveData<Boolean> = _btnResultClicked

    fun btnBack() {
        _backClicked.value = true
    }

    fun btnStart() {
        _breathingState.value = when (_breathingState.value) {
            StopStartState.STOP -> StopStartState.START
            StopStartState.START -> StopStartState.STOP
            null -> StopStartState.STOP
        }

    }

    fun btnResult() {
        _btnResultClicked.value = true
    }

}