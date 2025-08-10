package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kr.daejeonuinversity.lungexercise.util.util.UserInfoTempData

class BodyViewModel(application: Application) : AndroidViewModel(application) {

    private val _txEdtStature = MutableLiveData<String>()
    val txEdtStature = _txEdtStature

    private val _txEdtWeight = MutableLiveData<String>()
    val txEdtWeight = _txEdtWeight

    private val _isInputValid = MediatorLiveData<Boolean>()
    val isInputValid = _isInputValid

    init {
        _isInputValid.addSource(txEdtStature) { input ->
            UserInfoTempData.stature = input.toIntOrNull() ?: 0
            checkInputValidity()
        }

        _isInputValid.addSource(txEdtWeight) { input ->
            UserInfoTempData.weight = input.toIntOrNull() ?: 0
            checkInputValidity()
        }
    }

    private fun checkInputValidity() {
        val stature = _txEdtStature.value
        val weight = _txEdtWeight.value
        _isInputValid.value = !stature.isNullOrBlank() && !weight.isNullOrBlank()
    }
}