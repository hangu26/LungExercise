package kr.daejeonuinversity.lungexercise.data.repository

import kr.daejeonuinversity.lungexercise.data.local.dao.StepIntervalDao
import kr.daejeonuinversity.lungexercise.data.local.entity.StepIntervalEntity

class TotalStepRepository(private val dao: StepIntervalDao) {

    suspend fun getAllRecordedDates(): List<String> {
        return dao.getAllDates()
    }

    suspend fun getStepsByDate(selectedDate: String): List<StepIntervalEntity> {

        return dao.getStepsByDate(selectedDate)

    }

    suspend fun getIntervalsByDate(today : String) : List<StepIntervalEntity>{
        return dao.getIntervalsByDate(today)
    }

    suspend fun deleteByIntervalStart(id: Long) {
        dao.deleteByIntervalStart(id)
    }

}
