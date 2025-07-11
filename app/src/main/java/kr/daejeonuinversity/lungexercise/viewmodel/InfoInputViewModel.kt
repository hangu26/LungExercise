package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kr.daejeonuinversity.lungexercise.util.base.NavigationMenu

class InfoInputViewModel(application: Application) : AndroidViewModel(application) {

    private var _btnCLicked = MutableLiveData<Int>(0)
    val btnClicked = _btnCLicked

    private val _pageLoaded = MutableLiveData<Boolean>()
    val pageLoaded: LiveData<Boolean> = _pageLoaded

    private val _menu: MutableLiveData<NavigationMenu> =
        MutableLiveData(NavigationMenu.BIRTHDAY) // 단어 화면으로 초기화
    val menu: LiveData<NavigationMenu> = _menu

    private var _btnNextState = MutableLiveData<Boolean>()
    val btnNextState = _btnNextState

    fun btnNext(){

        if (btnNextState.value == true) _btnCLicked.value = _btnCLicked.value?.plus(1)

    }

    fun changeMenu(menu: NavigationMenu) {
        _menu.value = menu
    }

}