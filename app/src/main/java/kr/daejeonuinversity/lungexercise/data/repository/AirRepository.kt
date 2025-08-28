package kr.daejeonuinversity.lungexercise.data.repository

import android.util.Log
import kr.daejeonuinversity.lungexercise.data.remote.api.AirKoreaApi
import kr.daejeonuinversity.lungexercise.model.AirItem
import retrofit2.HttpException

class AirRepository(private val api: AirKoreaApi) {

    private val serviceKey = "axl6cFwp5hMFvSX6tB7qOwqjsDmBHUdEqNpSVYAwv+/SfnU2Je+BjZjvI/CkJIGOvOkd4dhI8UmPe3dhGT7BRg=="

    suspend fun getAirData(sidoName: String): List<AirItem> {
        val response = api.getAirData(
            serviceKey = serviceKey,
            sidoName = sidoName
        )
        return response.response.body.items
    }

}
