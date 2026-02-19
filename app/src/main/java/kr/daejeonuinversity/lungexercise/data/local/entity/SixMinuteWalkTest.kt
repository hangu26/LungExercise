package kr.daejeonuinversity.lungexercise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "six_minute_walk_test")
data class SixMinuteWalkTest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val totalCount: Int,
    val date: String,                  // yyyy-MM-dd
    val totalDistance: Double,         // 누적 거리 (m)
    val latestDistance: Double,        // 마지막 기록한 거리 (m)
    val totalSteps: Int,                // 누적 걸음 수
    val calories: Double
)