package kr.daejeonuinversity.lungexercise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breath_record")
data class BreathRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    val totalCount: Int,

    val date: String,
    val average: Int,
    val totalTime: Int,
    val clear: Int,

    // 폐기능 평균값 추가
    val avgFvc: Double?,
    val avgFev1: Double?,
    val avgFev1Fvc: Double?,
    val avgExpPressure: Double?
)