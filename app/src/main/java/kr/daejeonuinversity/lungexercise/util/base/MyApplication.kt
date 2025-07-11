package kr.daejeonuinversity.lungexercise.util.base

import android.app.Application
import kr.daejeonuinversity.lungexercise.util.util.module
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MyApplication)
            modules(module)
        }


    }

}

