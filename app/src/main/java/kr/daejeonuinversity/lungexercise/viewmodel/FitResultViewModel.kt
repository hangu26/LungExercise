package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FitResultViewModel(application: Application) : AndroidViewModel(application) {

    private val _backClicked = MutableLiveData<Boolean>()
    val backClicked : LiveData<Boolean> = _backClicked

    val txWalkDistance = MutableLiveData<String>("0")


    fun btnBack(){
        _backClicked.value = true
    }

}