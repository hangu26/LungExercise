package kr.daejeonuinversity.lungexercise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.daejeonuinversity.lungexercise.data.local.entity.BreathRecord

@Dao
interface BreathRecordDao {

    @Query("SELECT * FROM breath_record WHERE date IN (:dates)")
    suspend fun getBreathRecordsByDates(dates: List<String>): List<BreathRecord>

    @Query("SELECT DISTINCT date FROM breath_record")
    suspend fun getAllDates(): List<String>

    @Query("DELETE FROM breath_record WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("SELECT EXISTS(SELECT 1 FROM breath_record WHERE date = :date)")
    suspend fun existsByDate(date: String): Boolean

    @Insert
    suspend fun insert(record: BreathRecord)

    @Query("SELECT * FROM breath_record WHERE date = :date LIMIT 1")
    suspend fun getRecordByDate(date: String): BreathRecord?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(record: BreathRecord)

    @Query("SELECT AVG(totalTime) FROM breath_record WHERE date = :date")
    suspend fun getAverageTimeByDate(date: String): Double?

    @Query("SELECT SUM(totalTime) FROM breath_record WHERE date = :date")
    suspend fun getTotalTimeByDate(date: String): Int?

    @Query("SELECT COUNT(*) FROM breath_record WHERE date = :date AND clear = 1")
    suspend fun getClearCountByDate(date: String): Int
}