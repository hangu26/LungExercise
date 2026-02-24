package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kr.daejeonuinversity.lungexercise.util.util.UserInfoTempData

class SmokingViewModel(application: Application) : AndroidViewModel(application) {

    // 비흡연 버튼 상태
    private val _btnNonSmokerState = MutableLiveData<Boolean>()
    val btnNonSmokerState = _btnNonSmokerState

    // 흡연 버튼 상태
    private val _btnSmokerState = MutableLiveData<Boolean>()
    val btnSmokerState = _btnSmokerState

    fun btnNonSmoker() {
        _btnNonSmokerState.value = true
        _btnSmokerState.value = false
        UserInfoTempData.smoke = "비흡연"
    }

    fun btnSmoker() {
        _btnSmokerState.value = true
        _btnNonSmokerState.value = false
        UserInfoTempData.smoke = "흡연"
    }

//    fun getSmoking(): String = UserInfoTempData.smoking
}