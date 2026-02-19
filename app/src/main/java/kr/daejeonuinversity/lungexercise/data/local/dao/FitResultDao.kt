package kr.daejeonuinversity.lungexercise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kr.daejeonuinversity.lungexercise.data.local.entity.BreathRecord
import kr.daejeonuinversity.lungexercise.data.local.entity.FitResult

@Dao
interface FitResultDao {

    // 날짜가 이미 있으면 업데이트, 없으면 삽입
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(fitResult: FitResult)

    // 특정 날짜 조회
    @Query("SELECT * FROM fit_result_table WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): FitResult?

    @Query("SELECT * FROM fit_result_table ORDER BY date DESC")
    suspend fun getAllResults(): List<FitResult>

    @Query("SELECT * FROM fit_result_table WHERE date IN (:dates)")
    suspend fun getFitExerciseByDates(dates: List<String>): List<FitResult>

    @Query("SELECT DISTINCT date FROM fit_result_table")
    suspend fun getAllDates(): List<String>

    @Query("DELETE FROM fit_result_table")
    suspend fun deleteAll()
}
