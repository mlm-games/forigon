package app.forigon

import android.app.Application
import app.forigon.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LauncherApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@LauncherApp)
            modules(appModule)
        }
    }
}