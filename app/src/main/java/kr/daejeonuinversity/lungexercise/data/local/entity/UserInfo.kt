package kr.daejeonuinversity.lungexercise.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_info")
data class UserInfo(
    @PrimaryKey val id: Int = 0,  // 항상 한 명의 사용자라면 id = 0 고정도 가능
    val birthday : String,
    val gender: String,
    val height: Int,
    val weight: Int,
    val screeningNum : String = "",
    val initial : String = "",
    val visit : String = ""
)