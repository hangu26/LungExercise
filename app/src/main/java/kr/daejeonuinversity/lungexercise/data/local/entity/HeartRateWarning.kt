package kr.daejeonuinversity.lungexercise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "heart_rate_warnings")
data class HeartRateWarning(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: String,
    val maxHeartRate: Float,
    val count: Int
    )