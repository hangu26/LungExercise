package kr.daejeonuinversity.lungexercise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "breath_record")
data class BreathRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val totalCount : Int,
    val date: String,     // ex) "2025-07-20"
    val average: Int,
    val totalTime: Int,
    val clear: Int
)