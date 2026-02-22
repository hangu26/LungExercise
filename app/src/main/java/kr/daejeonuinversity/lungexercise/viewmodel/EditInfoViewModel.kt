package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.data.local.dao.UserInfoDao
import kr.daejeonuinversity.lungexercise.data.local.entity.UserInfo
import kr.daejeonuinversity.lungexercise.data.repository.InfoRepository

class EditInfoViewModel(private val repository: InfoRepository, application: Application) :
    AndroidViewModel(application) {

    private val _btnBackState = MutableLiveData<Boolean>()
    val btnBackState: LiveData<Boolean> = _btnBackState

    private val _btnSaveState = MutableLiveData<Boolean>()
    val btnSaveState: LiveData<Boolean> = _btnSaveState

    private var _txYear = MutableLiveData<String>()
    val txYear: LiveData<String> = _txYear

    private var _txMonth = MutableLiveData<String>()
    val txMonth: LiveData<String> = _txMonth

    private var _txDay = MutableLiveData<String>()
    val txDay: LiveData<String> = _txDay

    private var _txWeight = MutableLiveData<String>()
    val txWeight: LiveData<String> = _txWeight

    private var _txHeight = MutableLiveData<String>()
    val txHeight: LiveData<String> = _txHeight

    private var _btnMan = MutableLiveData<Boolean>()
    val btnMan: LiveData<Boolean> = _btnMan

    private var _btnWoman = MutableLiveData<Boolean>()
    val btnWoman: LiveData<Boolean> = _btnWoman

    private var _genderState = MutableLiveData<String>()
    val genderState: LiveData<String> = _genderState

    private var _edtDayClickState = MutableLiveData<Boolean>()
    val edtDayClickState: LiveData<Boolean> = _edtDayClickState

    private var _edtMonthClickState = MutableLiveData<Boolean>()
    val edtMonthClickState: LiveData<Boolean> = _edtMonthClickState

    private var _edtYearClickState = MutableLiveData<Boolean>()
    val edtYearClickState: LiveData<Boolean> = _edtYearClickState

    fun edtDayClicked(){

        _edtDayClickState.value = true

    }

    fun edtMonthClicked(){

        _edtMonthClickState.value = true

    }

    fun edtYearClicked(){

        _edtYearClickState.value = true

    }



    fun btnBack() {

        _btnBackState.value = true

    }

    fun btnSave() {
        _btnSaveState.value = true
    }

    fun btnMan() {
        _genderState.value = "man"
    }

    fun btnWoman() {
        _genderState.value = "woman"
    }

    fun saveData(userInfo: UserInfo) {

        viewModelScope.launch {
            repository.insertUserInfo(
                userInfo.birthday,
                userInfo.gender,
                userInfo.height,
                userInfo.weight,
                userInfo.screeningNum,
                userInfo.initial,
                userInfo.visit
            )
        }
    }

    fun fetchUserInfo() {

        viewModelScope.launch {

            val data = repository.getUserDates()

            val birthday = data?.birthday ?: ""
            if (birthday.length == 8) {
                val year = birthday.substring(0, 4)
                val month = birthday.substring(4, 6)
                val day = birthday.substring(6, 8)

                _txYear.value = year
                _txMonth.value = month
                _txDay.value = day
            }

            val gender = data?.gender ?: "woman"

            _genderState.value = gender

            val weight = data?.weight
            val height = data?.height

            _txWeight.value = weight.toString()
            _txHeight.value = height.toString()

        }
    }

}