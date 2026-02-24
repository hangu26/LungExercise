package kr.daejeonuinversity.lungexercise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kr.daejeonuinversity.lungexercise.data.local.entity.BreathRawRecord

@Dao
interface BreathRawRecordDao {

    @Insert
    suspend fun insert(record: BreathRawRecord)

    @Query("SELECT * FROM breath_raw_record WHERE date = :date")
    suspend fun getRecordsByDate(date: String): List<BreathRawRecord>
}