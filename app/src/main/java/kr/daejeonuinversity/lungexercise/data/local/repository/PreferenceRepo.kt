package kr.daejeonuinversity.lungexercise.data.local.repository

import android.content.Context

class PreferenceRepo(context : Context) {

    private val pref = context.getSharedPreferences("Mask Popup", Context.MODE_PRIVATE)

    fun getMaskPopup(): Boolean{
        return pref.getBoolean("mask popup", false)
    }

    fun setMaskPopup(enabled : Boolean){
        pref.edit().putBoolean("mask popup", enabled).apply()
    }

}