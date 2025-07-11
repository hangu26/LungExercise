package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class LungExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val _backClicked = MutableLiveData<Boolean>()
    val backClicked : LiveData<Boolean> = _backClicked

    fun btnBack(){
        _backClicked.value = true
    }

}