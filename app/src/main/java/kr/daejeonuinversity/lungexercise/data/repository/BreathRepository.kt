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

    suspend fun insertOrUpdateBreathRecord(time: Int, isClear: Int, date: String) {
        val existingRecord = dao.getRecordByDate(date)

        if (existingRecord != null) {
            // 기존 데이터 업데이트
            val newTotalCount = existingRecord.totalCount + 1
            val newTotalTime = existingRecord.totalTime + time
            val newAverage = newTotalTime / newTotalCount
            val newClear = existingRecord.clear + isClear

            val updatedRecord = existingRecord.copy(
                totalCount = newTotalCount,
                totalTime = newTotalTime,
                average = newAverage,
                clear = newClear
            )

            dao.insertOrUpdate(updatedRecord)

        } else {
            // 새 데이터 생성
            val newRecord = BreathRecord(
                date = date,
                totalCount = 1,
                totalTime = time,
                average = time,
                clear = isClear
            )
            dao.insertOrUpdate(newRecord)
        }
    }
}