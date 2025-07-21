package kr.daejeonuinversity.lungexercise.util.base

import android.app.Application
import kr.daejeonuinversity.lungexercise.util.util.databaseModule
import kr.daejeonuinversity.lungexercise.util.util.module
import kr.daejeonuinversity.lungexercise.util.util.repositoryModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(
                listOf(
                    databaseModule,
                    module,
                    repositoryModule
                )
            )
        }


    }

}

