package kr.daejeonuinversity.lungexercise.viewmodel

import android.app.Application
import android.location.Geocoder
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.daejeonuinversity.lungexercise.data.remote.api.RetrofitClient
import kr.daejeonuinversity.lungexercise.data.repository.AirRepository
import kr.daejeonuinversity.lungexercise.model.AirItem
import retrofit2.HttpException
import java.util.Locale

class InsightViewModel(application: Application, private val airRepository: AirRepository) :
    AndroidViewModel(application) {

    private val _btnBackState = MutableLiveData<Boolean>()
    val btnBackState: LiveData<Boolean> = _btnBackState

    val txPm10Value = MutableLiveData<String>("0")

    private val _airData = MutableLiveData<List<AirItem>>()
    val airData: LiveData<List<AirItem>> get() = _airData

    private val _nearestStation = MutableLiveData<AirItem>()
    val nearestStation: LiveData<AirItem> get() = _nearestStation

    private val _airGrades = MutableLiveData<Map<String, String>>()
    val airGrades: LiveData<Map<String, String>> = _airGrades

    fun loadAirDataBySido(sido: String, lat: Double, lon: Double) {
        viewModelScope.launch {
            try {
                val stations = airRepository.getAirData(sido)
                _airData.postValue(stations)

                val nearest = stations
                    .filter {
                        !listOf(
                            it.pm10Value,
                            it.pm25Value,
                            it.o3Value,
                            it.no2Value
                        ).all { v -> v.isNullOrEmpty() }
                    }
                    .minByOrNull { s ->
                        val dLat = (s.lat ?: 0.0) - lat
                        val dLon = (s.lon ?: 0.0) - lon
                        dLat * dLat + dLon * dLon
                    } ?: stations.firstOrNull { it.stationName == "서울" }

                _nearestStation.postValue(nearest)

                // ✅ 항목별 상태를 Map으로 저장
                nearest?.let {
                    val grades = mapOf(
                        "PM10" to getAirGrade(it.pm10Value, "PM10"),
                        "PM25" to getAirGrade(it.pm25Value, "PM25"),
                        "O3" to getAirGrade(it.o3Value, "O3"),
                        "NO2" to getAirGrade(it.no2Value, "NO2")
                    )
                    _airGrades.postValue(grades)
                }

            } catch (e: Exception) {
                Log.e("InsightVM", "Exception: ${e.message}")
            }
        }
    }

    private fun getAirGrade(value: String?, type: String): String {
        val v = value?.toDoubleOrNull() ?: return "데이터 없음"
        return when (type) {
            "PM10" -> when {
                v <= 30 -> "좋음"
                v <= 80 -> "보통"
                v <= 150 -> "나쁨"
                else -> "매우나쁨"
            }

            "PM25" -> when {
                v <= 15 -> "좋음"
                v <= 35 -> "보통"
                v <= 75 -> "나쁨"
                else -> "매우나쁨"
            }

            "O3" -> when {
                v <= 0.030 -> "좋음"
                v <= 0.090 -> "보통"
                v <= 0.150 -> "나쁨"
                else -> "매우나쁨"
            }

            "NO2" -> when {
                v <= 0.030 -> "좋음"
                v <= 0.060 -> "보통"
                v <= 0.200 -> "나쁨"
                else -> "매우나쁨"
            }

            else -> "데이터 없음"
        }
    }

    fun btnBack() {
        _btnBackState.value = true
    }

}