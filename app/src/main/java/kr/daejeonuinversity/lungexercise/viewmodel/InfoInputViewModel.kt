package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.data.repository.InfoRepository
import kr.daejeonuinversity.lungexercise.util.base.NavigationMenu

class InfoInputViewModel(private val repository : InfoRepository, application: Application) : AndroidViewModel(application) {

    private var _btnCLicked = MutableLiveData<Int>(0)
    val btnClicked = _btnCLicked

    private val _pageLoaded = MutableLiveData<Boolean>()
    val pageLoaded: LiveData<Boolean> = _pageLoaded

    private val _menu: MutableLiveData<NavigationMenu> =
        MutableLiveData(NavigationMenu.BIRTHDAY)
    val menu: LiveData<NavigationMenu> = _menu

    private var _btnNextState = MutableLiveData<Boolean>()
    val btnNextState = _btnNextState

    fun btnNext(){

        if (btnNextState.value == true) _btnCLicked.value = _btnCLicked.value?.plus(1)

    }

    fun saveBirthday(birthdayDate : String, gender : String, stature : Int, weight : Int){
        viewModelScope.launch {

            repository.insertUserInfo(birthdayDate,gender,stature,weight)

        }
    }

    fun changeMenu(menu: NavigationMenu) {
        _menu.value = menu
    }

}