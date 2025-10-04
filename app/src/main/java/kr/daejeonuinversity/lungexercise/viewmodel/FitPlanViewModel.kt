package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import android.mtp.MtpConstants
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.data.repository.InfoRepository
import kr.daejeonuinversity.lungexercise.data.repository.SixWalkTestRepository
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class FitPlanViewModel(private val repository: InfoRepository, private val wRepository : SixWalkTestRepository, application: Application) :
    AndroidViewModel(application) {

    private val _backClicked = MutableLiveData<Boolean>(false)
    val backClicked: LiveData<Boolean> get() = _backClicked

    private val _txEdtIntensity = MutableLiveData<String>("")
    val txEdtIntensity = _txEdtIntensity

    private val _txEdtTime = MutableLiveData<String>("")
    val txEdtTime = _txEdtTime

    private val _executeClicked = MutableLiveData<Boolean>(false)
    val executeClicked: LiveData<Boolean> get() = _executeClicked

    private val _userAge = MutableLiveData<Int>()
    val userAge = _userAge

    private val _userWeight = MutableLiveData<Int>()
    val userWeight = _userWeight

    private val _userHeight = MutableLiveData<Int>()
    val userHeight = _userHeight

    private val _latestDistance = MutableLiveData<Double>()
    val latestDistance: LiveData<Double> = _latestDistance

    private var userBirth: String? = null

    fun btnBack() {
        _backClicked.value = true
    }

    fun deleteRecordByDate(date: String) {
        viewModelScope.launch(Dispatchers.IO) {
            wRepository.deleteByDate(date)
        }
    }

    fun fetchUserInfo() {
        viewModelScope.launch {
            val data = repository.getUserDates()
            userBirth = data?.birthday?.toString()

            val weight = data?.weight?.toInt()
            _userWeight.postValue(weight)

            val height = data?.height?.toInt()
            _userHeight.postValue(height)

            val age = calculateUserAge()
            _userAge.postValue(age ?: 0) // null일 경우 0 처리

            val record = wRepository.getLastRecord()
            _latestDistance.postValue(record?.latestDistance ?: 0.0)

        }
    }

    private fun calculateUserAge(): Int? {
        return try {
            userBirth?.let { birth ->
                val formatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                val birthDate = LocalDate.parse(birth, formatter)
                val today = LocalDate.now()
                Period.between(birthDate, today).years
            }
        } catch (e: Exception) {
            null
        }
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