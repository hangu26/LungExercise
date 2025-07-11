package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class BirthdayViewModel(application: Application) : AndroidViewModel(application) {

    private var _btnBirthState = MutableLiveData<Boolean>()
    val btnBirthState = _btnBirthState

    fun btnBirthClicked(){
        _btnBirthState.value = true
    }

}