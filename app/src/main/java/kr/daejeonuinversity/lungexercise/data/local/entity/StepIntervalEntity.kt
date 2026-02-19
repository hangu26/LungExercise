package kr.daejeonuinversity.lungexercise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "step_intervals")
data class StepIntervalEntity(
    @PrimaryKey val intervalStart: Long, // 30분 단위 timestamp
    val date: String,                    // yyyy-MM-dd
    val steps: Int
)