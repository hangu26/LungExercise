package kr.daejeonuinversity.lungexercise.model

data class AirResponse(
    val response: ResponseBody
)

data class ResponseBody(
    val body: AirBody
)

data class AirBody(
    val items: List<AirItem>
)

data class AirItem(
    val lat : Double?,
    val lon : Double?,
    val stationName: String?,
    val pm10Value: String?,   // 미세먼지
    val pm25Value: String?,   // 초미세먼지
    val o3Value: String?,     // 오존
    val no2Value: String?     // 이산화질소
)