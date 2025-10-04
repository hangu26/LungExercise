package kr.daejeonuinversity.lungexercise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.daejeonuinversity.lungexercise.data.local.entity.StepIntervalEntity

@Dao
interface StepIntervalDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(interval: StepIntervalEntity)

    @Query("SELECT * FROM step_intervals WHERE date = :date ORDER BY intervalStart")
    suspend fun getIntervalsByDate(date: String): List<StepIntervalEntity>

    @Query("SELECT * FROM step_intervals WHERE date = :date ORDER BY intervalStart ASC")
    suspend fun getStepsByDate(date: String): List<StepIntervalEntity>

    @Query("SELECT DISTINCT date FROM step_intervals")
    suspend fun getAllDates(): List<String>

    @Query("DELETE FROM step_intervals WHERE intervalStart = :intervalStart")
    suspend fun deleteByIntervalStart(intervalStart: Long)

}