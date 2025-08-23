package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class InsightViewModel(application: Application) : AndroidViewModel(application) {

    private val _btnBackState = MutableLiveData<Boolean>()
    val btnBackState: LiveData<Boolean> = _btnBackState

    val txPm10Value = MutableLiveData<String>()

    fun btnBack() {
        _btnBackState.value = true
    }

}