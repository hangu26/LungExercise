package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData

class VisitViewModel(application: Application) : AndroidViewModel(application) {

    private var _btnVisitState = MutableLiveData<Boolean>()
    val btnVisitState = _btnVisitState

    fun btnVisitClicked(){
        _btnVisitState.value = true
    }

}