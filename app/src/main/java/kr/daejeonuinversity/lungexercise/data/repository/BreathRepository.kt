package kr.daejeonuinversity.lungexercise.data.repository

import kr.daejeonuinversity.lungexercise.data.local.dao.BreathRecordDao
import kr.daejeonuinversity.lungexercise.data.local.entity.BreathRecord
import java.time.LocalDate

class BreathRepository(private val dao: BreathRecordDao) {

    suspend fun getAllRecordedDates(): List<String> {
        return dao.getAllDates()
    }

    suspend fun getBreathRecordsByDates(dates: List<String>): List<BreathRecord> {
        return dao.getBreathRecordsByDates(dates)
    }

    suspend fun removeClickedData(date: LocalDate) {

        val dateString = date.toString()
        val exists = dao.existsByDate(dateString)
        if (exists) {
            dao.deleteByDate(dateString)
        }

    }

    suspend fun insertOrUpdateBreathRecord(
        time: Int,
        isClear: Int,
        date: String,
        fvc: Double?,
        fev1: Double?,
        fev1Fvc: Double?,
        expPressure: Double?
    ) {
        val existingRecord = dao.getRecordByDate(date)

        if (existingRecord != null) {
            val newTotalCount = existingRecord.totalCount + 1
            val newTotalTime = existingRecord.totalTime + time
            val newAverage = newTotalTime / newTotalCount
            val newClear = existingRecord.clear + isClear

            val updatedRecord = existingRecord.copy(
                totalCount = newTotalCount,
                totalTime = newTotalTime,
                average = newAverage,
                clear = newClear,

                // 🔽 폐기능 평균값 갱신
                avgFvc = mergeAvg(existingRecord.avgFvc, fvc),
                avgFev1 = mergeAvg(existingRecord.avgFev1, fev1),
                avgFev1Fvc = mergeAvg(existingRecord.avgFev1Fvc, fev1Fvc),
                avgExpPressure = mergeAvg(existingRecord.avgExpPressure, expPressure)
            )

            dao.insertOrUpdate(updatedRecord)

        } else {
            val newRecord = BreathRecord(
                date = date,
                totalCount = 1,
                totalTime = time,
                average = time,
                clear = isClear,

                avgFvc = fvc,
                avgFev1 = fev1,
                avgFev1Fvc = fev1Fvc,
                avgExpPressure = expPressure
            )

            dao.insertOrUpdate(newRecord)
        }
    }

    private fun mergeAvg(old: Double?, new: Double?): Double? =
        when {
            new == null -> old
            old == null -> new
            else -> (old + new) / 2
        }

}