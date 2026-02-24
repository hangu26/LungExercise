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
            .addMigrations(
                MIGRATION_10_11,
                MIGRATION_11_12,
                MIGRATION_12_13,
                MIGRATION_13_14
            )
            .build()
    }

    single { get<BreathDatabase>().breathRecordDao() }
    single { get<BreathDatabase>().userInfoDao() }
    single { get<BreathDatabase>().sixMinuteWalkTestDao() }
    single { get<BreathDatabase>().heartRateWarning() }
    single { get<BreathDatabase>().fitResult() }
    single { get<BreathDatabase>().stepIntervalDao() }
    single { get<BreathDatabase>().breathRawRecordDao() }
}

val MIGRATION_10_11 = object : Migration(10, 11) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // 새 컬럼 추가, 기존 데이터 보존
        database.execSQL("ALTER TABLE user_info ADD COLUMN screeningNum TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE user_info ADD COLUMN initial TEXT NOT NULL DEFAULT ''")
        database.execSQL("ALTER TABLE user_info ADD COLUMN visit TEXT NOT NULL DEFAULT ''")
    }
}

val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(database: SupportSQLiteDatabase) {

        database.execSQL(
            "ALTER TABLE breath_record ADD COLUMN avgFvc REAL"
        )
        database.execSQL(
            "ALTER TABLE breath_record ADD COLUMN avgFev1 REAL"
        )
        database.execSQL(
            "ALTER TABLE breath_record ADD COLUMN avgFev1Fvc REAL"
        )
        database.execSQL(
            "ALTER TABLE breath_record ADD COLUMN avgExpPressure REAL"
        )
    }
}

val MIGRATION_12_13 = object : Migration(12, 13) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            """
            CREATE TABLE IF NOT EXISTS breath_raw_record (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                date TEXT NOT NULL,
                timestamp INTEGER NOT NULL,
                exhaleTime INTEGER NOT NULL,
                isClear INTEGER NOT NULL,
                fvc REAL,
                fev1 REAL,
                fev1Fvc REAL,
                expPressure REAL
            )
            """.trimIndent()
        )
    }
}

val MIGRATION_13_14 = object : Migration(13, 14) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL(
            "ALTER TABLE user_info ADD COLUMN smoke TEXT NOT NULL DEFAULT ''"
        )
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

