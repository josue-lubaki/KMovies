package ca.josue_lubaki.kmovies.android

import android.app.Application
import ca.josue_lubaki.kmovies.android.di.appModule
import ca.josue_lubaki.kmovies.di.getSharedModule
import org.koin.core.context.startKoin

/**
 * created by Josue Lubaki
 * date : 2023-05-02
 * version : 1.0.0
 */

class MovieApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            modules(appModule + getSharedModule())
        }
    }
}