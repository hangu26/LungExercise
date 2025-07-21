package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import android.view.MotionEvent
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kr.daejeonuinversity.lungexercise.util.util.HeartRateReceiver

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _goToLungExercise = MutableLiveData<Boolean>()
    val goToLungExercise = _goToLungExercise

    private val _btnHistoryClicked = MutableLiveData<Boolean>()
    val btnHistoryClicked = _btnHistoryClicked


    fun btnLungExerciseDetail(){
        goToLungExercise.value = true
    }

    fun btnHistory(){
        _btnHistoryClicked.value = true
    }

}