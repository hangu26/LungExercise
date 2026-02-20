package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kr.daejeonuinversity.lungexercise.util.util.UserInfoTempData

class InitialViewModel(application: Application) : AndroidViewModel(application)  {

    private val _txEdtInitial = MutableLiveData<String>()
    val txEdtInitial = _txEdtInitial

    private val _isInputValid = MediatorLiveData<Boolean>()
    val isInputValid = _isInputValid

    init {
        _isInputValid.addSource(txEdtInitial) { input ->
            UserInfoTempData.initial = input
            checkInputValidity()
        }

    }

    private fun checkInputValidity() {
        val initial = _txEdtInitial.value
        _isInputValid.value = !initial.isNullOrBlank()
    }

}