package kr.daejeonuinversity.lungexercise.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import kr.daejeonuinversity.lungexercise.data.local.dao.BreathRawRecordDao
import kr.daejeonuinversity.lungexercise.data.local.dao.BreathRecordDao
import kr.daejeonuinversity.lungexercise.data.local.dao.FitResultDao
import kr.daejeonuinversity.lungexercise.data.local.dao.HeartRateWarningDao
import kr.daejeonuinversity.lungexercise.data.local.dao.SixMinuteWalkTestDao
import kr.daejeonuinversity.lungexercise.data.local.dao.StepIntervalDao
import kr.daejeonuinversity.lungexercise.data.local.dao.UserInfoDao
import kr.daejeonuinversity.lungexercise.data.local.entity.BreathRawRecord
import kr.daejeonuinversity.lungexercise.data.local.entity.BreathRecord
import kr.daejeonuinversity.lungexercise.data.local.entity.FitResult
import kr.daejeonuinversity.lungexercise.data.local.entity.HeartRateWarning
import kr.daejeonuinversity.lungexercise.data.local.entity.SixMinuteWalkTest
import kr.daejeonuinversity.lungexercise.data.local.entity.StepIntervalEntity
import kr.daejeonuinversity.lungexercise.data.local.entity.UserInfo

@Database(
    entities = [BreathRecord::class, UserInfo::class, SixMinuteWalkTest::class, HeartRateWarning::class, FitResult::class, StepIntervalEntity::class, BreathRawRecord::class],
    version = 14
)
abstract class BreathDatabase : RoomDatabase() {
    abstract fun breathRecordDao(): BreathRecordDao

    abstract fun breathRawRecordDao(): BreathRawRecordDao
    abstract fun userInfoDao(): UserInfoDao
    abstract fun sixMinuteWalkTestDao(): SixMinuteWalkTestDao
    abstract fun heartRateWarning(): HeartRateWarningDao
    abstract fun fitResult(): FitResultDao
    abstract fun stepIntervalDao(): StepIntervalDao

    companion object {

        @Volatile
        private var INSTANCE: BreathDatabase? = null

    }

}
