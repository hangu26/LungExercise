package kr.daejeonuinversity.lungexercise.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import kr.daejeonuinversity.lungexercise.data.local.dao.BreathRecordDao
import kr.daejeonuinversity.lungexercise.data.local.dao.UserInfoDao
import kr.daejeonuinversity.lungexercise.data.local.entity.BreathRecord
import kr.daejeonuinversity.lungexercise.data.local.entity.UserInfo

@Database(entities = [BreathRecord::class, UserInfo::class], version = 3)
abstract class BreathDatabase : RoomDatabase(){
    abstract fun breathRecordDao() : BreathRecordDao
    abstract fun userInfoDao(): UserInfoDao

    companion object{

        @Volatile
        private var INSTANCE : BreathDatabase? = null

    }

}
