package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData

class FitPlanViewModel(application: Application) : AndroidViewModel(application) {

    private val _backClicked = MutableLiveData<Boolean>(false)
    val backClicked: LiveData<Boolean> get() = _backClicked

    private val _txEdtIntensity = MutableLiveData<String>("")
    val txEdtIntensity = _txEdtIntensity

    private val _txEdtTime = MutableLiveData<String>("")
    val txEdtTime = _txEdtTime

    private val _executeClicked = MutableLiveData<Boolean>(false)
    val executeClicked: LiveData<Boolean> get() = _executeClicked

    fun btnBack() {
        _backClicked.value = true
    }

    // 버튼 활성화 여부
    val isExecuteEnabled = MediatorLiveData<Boolean>().apply {
        addSource(_txEdtIntensity) { checkInputs() }
        addSource(_txEdtTime) { checkInputs() }
    }

    private fun checkInputs() {
        isExecuteEnabled.value =
            !_txEdtIntensity.value.isNullOrEmpty() &&
                    !_txEdtTime.value.isNullOrEmpty()
    }

    fun btnNext() {
        if (isExecuteEnabled.value == true) {
            _executeClicked.value = true
        }
    }

}