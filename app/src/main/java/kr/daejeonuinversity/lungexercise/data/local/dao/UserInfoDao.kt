package kr.daejeonuinversity.lungexercise.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kr.daejeonuinversity.lungexercise.data.local.entity.UserInfo

@Dao
interface UserInfoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(userInfo: UserInfo)

    @Query("SELECT * FROM user_info LIMIT 1")
    suspend fun getUserInfo(): UserInfo?

    @Update
    suspend fun updateUserInfo(userInfo: UserInfo)


}
