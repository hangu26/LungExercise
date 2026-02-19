package kr.daejeonuinversity.lungexercise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.daejeonuinversity.lungexercise.data.local.entity.HeartRateWarning

@Dao
interface HeartRateWarningDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWarning(warning: HeartRateWarning)

    @Query("SELECT * FROM heart_rate_warnings WHERE date = :date LIMIT 1")
    suspend fun getWarningByDate(date: String): HeartRateWarning?
}