package dev.steelahhh.mapful

import android.app.Application
import dev.steelahhh.mapful.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MapfulApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule)
            androidContext(this@MapfulApp)
        }
    }
}
