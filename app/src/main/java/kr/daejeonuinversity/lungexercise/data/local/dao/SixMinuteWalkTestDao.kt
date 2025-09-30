package kr.daejeonuinversity.lungexercise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kr.daejeonuinversity.lungexercise.data.local.entity.SixMinuteWalkTest

@Dao
interface SixMinuteWalkTestDao {
    @Query("SELECT * FROM six_minute_walk_test ORDER BY id DESC LIMIT 1")
    suspend fun getLastRecord(): SixMinuteWalkTest?

    @Query("SELECT DISTINCT date FROM six_minute_walk_test")
    suspend fun getAllRecord(): List<String>

    @Insert
    suspend fun insert(record: SixMinuteWalkTest)

    @Update
    suspend fun update(record: SixMinuteWalkTest)
}