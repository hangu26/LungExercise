package kr.daejeonuinversity.lungexercise.data.repository

import kr.daejeonuinversity.lungexercise.data.local.dao.SixMinuteWalkTestDao
import kr.daejeonuinversity.lungexercise.data.local.entity.SixMinuteWalkTest

class SixWalkTestRepository(private val dao: SixMinuteWalkTestDao) {

    suspend fun getSixDataByDate(date: String): List<SixMinuteWalkTest> {
        return dao.getSixDataByDate(date)
    }

    suspend fun getAllRecordedDates(): List<String> {
        return dao.getAllRecord()
    }

    suspend fun getLastRecord(): SixMinuteWalkTest? {
        return dao.getLastRecord()
    }

    suspend fun deleteByDate(date: String) = dao.deleteByDate(date)

    suspend fun insert(record: SixMinuteWalkTest) {
        dao.insert(record)
    }

    suspend fun update(record: SixMinuteWalkTest) {
        dao.update(record)
    }
}