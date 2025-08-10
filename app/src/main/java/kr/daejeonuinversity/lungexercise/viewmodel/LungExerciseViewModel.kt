package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

class LungExerciseViewModel(application: Application) : AndroidViewModel(application) {

    private val _backClicked = MutableLiveData<Boolean>()
    val backClicked : LiveData<Boolean> = _backClicked

    /** 블루투스 연결에 따라 버튼 색상, 텍스트 변경 결정 변수 **/
    private val _isConnected = MutableLiveData<Boolean>()
    val isConnected : LiveData<Boolean> = _isConnected

    private val _btnConnectState = MutableLiveData<Boolean>()
    val btnConnectState : LiveData<Boolean> = _btnConnectState

    private val _btnDisconnectState = MutableLiveData<Boolean>()
    val btnDisconnectState : LiveData<Boolean> = _btnDisconnectState

    fun setIsConnected(value: Boolean) {
        _isConnected.value = value
    }

    fun btnBack(){
        _backClicked.value = true
    }

    fun btnConnect(){
        _btnConnectState.value = true
    }

    fun btnDisconnect(){

        _btnDisconnectState.value = true

    }

}