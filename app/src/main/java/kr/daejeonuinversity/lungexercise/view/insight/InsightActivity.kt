package kr.daejeonuinversity.lungexercise.view.insight

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import kr.daejeonuinversity.lungexercise.R
import kr.daejeonuinversity.lungexercise.databinding.ActivityInsightBinding
import kr.daejeonuinversity.lungexercise.model.AirItem
import kr.daejeonuinversity.lungexercise.util.base.BaseActivity
import kr.daejeonuinversity.lungexercise.util.util.BackPressedCallback
import kr.daejeonuinversity.lungexercise.view.main.MainActivity
import kr.daejeonuinversity.lungexercise.viewmodel.InsightViewModel
import org.koin.android.ext.android.inject
import java.io.IOException
import java.util.Locale

class InsightActivity : BaseActivity<ActivityInsightBinding>(R.layout.activity_insight) {

    private val iViewModel: InsightViewModel by inject()
    private val backPressedCallback = BackPressedCallback(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding.apply {
            activity = this@InsightActivity
            viewmodel = iViewModel
            lifecycleOwner = this@InsightActivity
        }

        observe()
        backPressedCallback.addCallbackActivity(this, MainActivity::class.java)
        fetchAirDataByCurrentLocation()

    }

    private fun observe() = iViewModel.let { vm ->

        vm.btnBackState.observe(this@InsightActivity) {

            if (it) {

                val intent = Intent(this@InsightActivity, MainActivity::class.java)
                startActivityBackAnimation(intent, this@InsightActivity)

            }

        }

        vm.airData.observe(this@InsightActivity) { items ->
            items.forEach {
                Log.d(
                    "AirData",
                    "측정소: ${it.stationName}, PM10: ${it.pm10Value}, PM2.5: ${it.pm25Value}, O3: ${it.o3Value}, NO2: ${it.no2Value}"
                )
            }

        }

        vm.nearestStation.observe(this) { station ->
            binding.tvPm10Value.text = station.pm10Value ?: "데이터 없음"
            binding.tvPm25Value.text = station.pm25Value ?: "데이터 없음"
            binding.tvOzoneValue.text = station.o3Value ?: "데이터 없음"
            binding.tvNo2Value.text = station.no2Value ?: "데이터 없음"
        }

        vm.airGrades.observe(this) { grades ->

            grades["PM10"]?.let { grade ->
                binding.txDustStatePm10.text = grade
                binding.icAirQualityPm10.setImageResource(getGradeIcon(grade))
            }
            grades["PM25"]?.let { grade ->
                binding.txDustStatePm25.text = grade
                binding.icAirQualityPm25.setImageResource(getGradeIcon(grade))
            }
            grades["O3"]?.let { grade ->
                binding.txDustStateO3.text = grade
                binding.icAirQualityO3.setImageResource(getGradeIcon(grade))
            }
            grades["NO2"]?.let { grade ->
                binding.txDustStateNo2.text = grade
                binding.icAirQualityNo2.setImageResource(getGradeIcon(grade))
            }
        }

    }

    private fun getGradeIcon(grade: String): Int {
        return when (grade) {
            "좋음" -> R.drawable.dust_good
            "보통" -> R.drawable.dust_normal
            "나쁨" -> R.drawable.dust_bad
            "매우나쁨" -> R.drawable.dust_worst
            else -> R.drawable.dust_good
        }
    }

    private fun fetchAirDataByCurrentLocation() {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let { loc ->
                val geocoder = Geocoder(this, Locale.KOREA)
                try {
                    val adminArea = geocoder.getFromLocation(loc.latitude, loc.longitude, 1)
                        ?.firstOrNull()?.adminArea
                    val sido = when {
                        adminArea?.contains("서울") == true -> "서울"
                        adminArea?.contains("부산") == true -> "부산"
                        adminArea?.contains("대구") == true -> "대구"
                        adminArea?.contains("인천") == true -> "인천"
                        adminArea?.contains("광주") == true -> "광주"
                        adminArea?.contains("대전") == true -> "대전"
                        adminArea?.contains("울산") == true -> "울산"
                        adminArea?.contains("세종") == true -> "세종"
                        adminArea?.contains("경기") == true -> "경기"
                        adminArea?.contains("강원") == true -> "강원"
                        adminArea?.contains("충북") == true -> "충북"
                        adminArea?.contains("충남") == true -> "충남"
                        adminArea?.contains("전북") == true -> "전북"
                        adminArea?.contains("전남") == true -> "전남"
                        adminArea?.contains("경북") == true -> "경북"
                        adminArea?.contains("경남") == true -> "경남"
                        adminArea?.contains("제주") == true -> "제주"
                        else -> "서울" // 기본값
                    }
                    iViewModel.loadAirDataBySido(sido, loc.latitude, loc.longitude)

                } catch (e: IOException) {
                    Log.e("InsightActivity", "Geocoder error: ${e.message}")
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchAirDataByCurrentLocation()
        }
    }


}