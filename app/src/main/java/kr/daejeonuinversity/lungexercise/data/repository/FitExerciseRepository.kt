package kr.daejeonuinversity.lungexercise.data.repository

import kr.daejeonuinversity.lungexercise.data.local.dao.HeartRateWarningDao
import kr.daejeonuinversity.lungexercise.data.local.entity.HeartRateWarning

class FitExerciseRepository(private val heartRateWarningDao: HeartRateWarningDao) {

    suspend fun insertOrUpdateWarning(date: String, currentHR: Float) {
        val existing = heartRateWarningDao.getWarningByDate(date)

        if (existing == null) {
            // 새 날짜 → 초기값 저장
            val newWarning = HeartRateWarning(
                date = date,
                maxHeartRate = currentHR,
                count = 1
            )
            heartRateWarningDao.insertWarning(newWarning)
        } else {
            // 기존 날짜 → count 증가 + 최대 심박수 갱신
            val updatedWarning = existing.copy(
                maxHeartRate = maxOf(existing.maxHeartRate, currentHR),
                count = existing.count + 1
            )
            heartRateWarningDao.insertWarning(updatedWarning) // REPLACE
        }
    }

}