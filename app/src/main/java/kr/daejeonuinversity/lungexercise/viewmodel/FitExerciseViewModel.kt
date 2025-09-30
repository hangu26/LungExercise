package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class FitExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val _backClicked = MutableLiveData<Boolean>(false)
    val backClicked: LiveData<Boolean> get() = _backClicked

    private val _txWalkDistance = MutableLiveData<String>()
    val txWalkDistance : LiveData<String> = _txWalkDistance

    fun btnBack() {
        _backClicked.value = true
    }

    fun btnStart(){

    }

    fun btnStop(){

    }

    fun btnReset(){


    }

}