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

    fun btnLungExerciseDetail(){
        goToLungExercise.value = true
    }

}