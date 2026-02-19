package kr.daejeonuinversity.lungexercise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fit_result_table")
data class FitResult(
    @PrimaryKey val date: String,   // 날짜를 PK로
    val time: Int,                  // 누적 운동 시간 (분)
    val userDistance: Double,       // 누적 운동 거리 (m)
    val calories: Double,           // 누적 칼로리
    val heartRateWarningCount: Int ,  // 누적 심박수 위험 빈도
    val totalWalkCount : Int

)