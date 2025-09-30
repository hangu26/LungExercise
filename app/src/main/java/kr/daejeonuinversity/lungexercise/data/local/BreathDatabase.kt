package kr.daejeonuinversity.lungexercise.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import kr.daejeonuinversity.lungexercise.data.local.dao.BreathRecordDao
import kr.daejeonuinversity.lungexercise.data.local.dao.SixMinuteWalkTestDao
import kr.daejeonuinversity.lungexercise.data.local.dao.UserInfoDao
import kr.daejeonuinversity.lungexercise.data.local.entity.BreathRecord
import kr.daejeonuinversity.lungexercise.data.local.entity.SixMinuteWalkTest
import kr.daejeonuinversity.lungexercise.data.local.entity.UserInfo

@Database(entities = [BreathRecord::class, UserInfo::class, SixMinuteWalkTest::class], version = 6)
abstract class BreathDatabase : RoomDatabase(){
    abstract fun breathRecordDao() : BreathRecordDao
    abstract fun userInfoDao(): UserInfoDao
    abstract fun sixMinuteWalkTestDao(): SixMinuteWalkTestDao

    companion object{

        @Volatile
        private var INSTANCE : BreathDatabase? = null

    }

}
