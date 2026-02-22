package kr.daejeonuinversity.lungexercise.util.util

import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kr.daejeonuinversity.lungexercise.data.local.BreathDatabase
import org.koin.dsl.module

val databaseModule = module {
    single {
        Room.databaseBuilder(
            get(),
            BreathDatabase::class.java,
            "breath_database"
        )
            .addMigrations(MIGRATION_10_11) // 마이그레이션 추가
            .build()
    }

    single { get<BreathDatabase>().breathRecordDao() }
    single { get<BreathDatabase>().userInfoDao() }
    single { get<BreathDatabase>().sixMinuteWalkTestDao() }
    single { get<BreathDatabase>().heartRateWarning() }
    single { get<BreathDatabase>().fitResult() }
    single { get<BreathDatabase>().stepIntervalDao() }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 새 컬럼 추가, 기존 데이터 보존
        database.execSQL("ALTER TABLE user_info ADD COLUMN screeningNum TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE user_info ADD COLUMN initial TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE user_info ADD COLUMN visit TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS user_info (
                id INTEGER NOT NULL PRIMARY KEY,
                birthday TEXT NOT NULL,
                gender TEXT NOT NULL,
                height INTEGER NOT NULL,
                weight INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }
}

