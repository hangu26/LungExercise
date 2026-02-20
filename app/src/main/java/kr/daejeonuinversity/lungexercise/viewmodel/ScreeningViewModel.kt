package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import kr.daejeonuinversity.lungexercise.util.util.UserInfoTempData

class ScreeningViewModel(application: Application) : AndroidViewModel(application){

    private val _txEdtScreening = MutableLiveData<String>()
    val txEdtScreening = _txEdtScreening

    private val _isInputValid = MediatorLiveData<Boolean>()
    val isInputValid = _isInputValid

    init {
        _isInputValid.addSource(txEdtScreening) { input ->
            UserInfoTempData.screeningNum = input
            checkInputValidity()
        }

    }

    private fun checkInputValidity() {
        val screeningNum = _txEdtScreening.value
        _isInputValid.value = !screeningNum.isNullOrBlank()
    }

}