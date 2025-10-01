package kr.daejeonuinversity.lungexercise.data.repository

import kr.daejeonuinversity.lungexercise.data.local.dao.FitResultDao
import kr.daejeonuinversity.lungexercise.data.local.dao.HeartRateWarningDao
import kr.daejeonuinversity.lungexercise.data.local.entity.BreathRecord
import kr.daejeonuinversity.lungexercise.data.local.entity.FitResult
import kr.daejeonuinversity.lungexercise.data.local.entity.HeartRateWarning

class FitExerciseRepository(private val heartRateWarningDao: HeartRateWarningDao, private val fitResultDao: FitResultDao) {

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

    suspend fun insertFitResultData(
        time: Int,
        userDistance: Double,
        calories: Double,
        heartRateWarningCount: Int,
        totalWalkCount : Int,
        date: String
    ) {
        val existing = fitResultDao.getByDate(date)
        if (existing != null) {
            val updated = existing.copy(
                time = existing.time + time,
                userDistance = existing.userDistance + userDistance,
                calories = existing.calories + calories,
                heartRateWarningCount = existing.heartRateWarningCount + heartRateWarningCount,
                totalWalkCount = existing.totalWalkCount + totalWalkCount
            )
            fitResultDao.insert(updated)
        } else {
            val newEntry = FitResult(
                date = date,
                time = time,
                userDistance = userDistance,
                calories = calories,
                heartRateWarningCount = heartRateWarningCount,
                totalWalkCount = totalWalkCount
            )
            fitResultDao.insert(newEntry)
        }
    }

    suspend fun getFitExerciseByDates(dates: List<String>): List<FitResult> {
        return fitResultDao.getFitExerciseByDates(dates)
    }

    suspend fun getAllFitResults(): List<FitResult> {
        return fitResultDao.getAllResults()
    }

    suspend fun getAllRecordedDates(): List<String> {
        return fitResultDao.getAllDates()
    }


    suspend fun deleteAllFitResults() {
        fitResultDao.deleteAll()
    }

}