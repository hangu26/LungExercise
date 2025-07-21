package kr.daejeonuinversity.lungexercise.util.util

import androidx.room.Room
import kr.daejeonuinversity.lungexercise.data.local.BreathDatabase
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            BreathDatabase::class.java,
            "breath_database"
        ).build()
    }

    single { get<BreathDatabase>().breathRecordDao() }
}