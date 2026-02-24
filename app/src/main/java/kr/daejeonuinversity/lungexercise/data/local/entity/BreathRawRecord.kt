package kr.daejeonuinversity.lungexercise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breath_raw_record")
data class BreathRawRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val date: String,          // yyyy-MM-dd
    val timestamp: Long,       // 측정 시각 (System.currentTimeMillis)

    val exhaleTime: Int,       // userSeconds
    val isClear: Int,

    val fvc: Double?,
    val fev1: Double?,
    val fev1Fvc: Double?,
    val expPressure: Double?
)