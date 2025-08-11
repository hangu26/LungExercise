package kr.daejeonuinversity.lungexercise.util.base

import android.app.Application
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import kr.daejeonuinversity.lungexercise.util.util.MaskBluetoothManager
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

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {

            override fun onStop(owner: LifecycleOwner) {
                // 앱이 백그라운드로 진입할 때
                MaskBluetoothManager.disconnect()

            }

            override fun onStart(owner: LifecycleOwner) {

                val deviceName = "MASK7"
                MaskBluetoothManager.connectToDevice(applicationContext, deviceName, true)

            }
        })


    }

}

