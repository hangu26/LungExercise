package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class GenderViewModel(application: Application) : AndroidViewModel(application) {

    private var _btnManState = MutableLiveData<Boolean>()
    val btnManState = _btnManState

    private var _btnWomanState = MutableLiveData<Boolean>()
    val btnWomanState = _btnWomanState

    fun btnWoman(){
        _btnWomanState.value = true
    }

    fun btnMan(){
        _btnManState.value = true
    }

}