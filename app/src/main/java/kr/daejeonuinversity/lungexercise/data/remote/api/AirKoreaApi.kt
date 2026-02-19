package kr.daejeonuinversity.lungexercise.data.remote.api

import com.google.gson.GsonBuilder
import kr.daejeonuinversity.lungexercise.model.AirResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface AirKoreaApi {
    @GET("getCtprvnRltmMesureDnsty")
    suspend fun getAirData(
        @Query("serviceKey") serviceKey: String,
        @Query("sidoName") sidoName: String,   // 시/도 이름 (예: 서울)
        @Query("returnType") returnType: String = "json",
        @Query("numOfRows") numOfRows: Int = 100,
        @Query("pageNo") pageNo: Int = 1,
        @Query("ver") ver: String = "1.0"
    ): AirResponse
}

// Retrofit Client
object RetrofitClient {
    private const val BASE_URL = "http://apis.data.go.kr/B552584/ArpltnInforInqireSvc/"

    val api: AirKoreaApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AirKoreaApi::class.java)
    }
}
