package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import kr.daejeonuinversity.lungexercise.util.util.UserInfoTempData

class GenderViewModel(application: Application) : AndroidViewModel(application) {

    private var _btnManState = MutableLiveData<Boolean>()
    val btnManState = _btnManState

    private var _btnWomanState = MutableLiveData<Boolean>()
    val btnWomanState = _btnWomanState

    fun btnWoman() {
        _btnWomanState.value = true
        _btnManState.value = false
        UserInfoTempData.gender = "woman"
    }

    fun btnMan() {
        _btnManState.value = true
        _btnWomanState.value = false
        UserInfoTempData.gender = "man"
    }

    fun getGender(): String = UserInfoTempData.gender
}