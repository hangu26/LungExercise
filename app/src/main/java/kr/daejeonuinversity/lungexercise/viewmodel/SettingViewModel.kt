package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kr.daejeonuinversity.lungexercise.data.local.repository.PreferenceRepo

class SettingViewModel(application: Application, private val preferenceRepo: PreferenceRepo): AndroidViewModel(application) {

    private val _btnBackState = MutableLiveData<Boolean>()
    val btnBackState : LiveData<Boolean> = _btnBackState

    private val _maskPopup = MutableLiveData<Boolean>()
    val maskPopup : LiveData<Boolean> get() = _maskPopup

    private val _maskPopupToggled = MutableLiveData<Unit>()
    val maskPopupToggled: LiveData<Unit> = _maskPopupToggled

    fun loadMaskPopup(){
        _maskPopup.value = preferenceRepo.getMaskPopup()
    }

    fun toggleMaskPopup() {
        val newValue = !(_maskPopup.value ?: false)
        _maskPopup.value = newValue
        preferenceRepo.setMaskPopup(newValue)

        if (newValue) {
            _maskPopupToggled.value = Unit // true로 바뀔 때만 이벤트 발생
        }

    }

    fun btnBack(){

        _btnBackState.value = true

    }

}