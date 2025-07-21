package kr.daejeonuinversity.lungexercise.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kr.daejeonuinversity.lungexercise.data.local.dao.BreathRecordDao
import kr.daejeonuinversity.lungexercise.data.local.entity.BreathRecord

@Database(entities = [BreathRecord::class], version = 2)
abstract class BreathDatabase : RoomDatabase(){
    abstract fun breathRecordDao() : BreathRecordDao

    companion object{

        @Volatile
        private var INSTANCE : BreathDatabase? = null

    }

}