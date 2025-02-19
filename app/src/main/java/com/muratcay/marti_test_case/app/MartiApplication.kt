package com.muratcay.marti_test_case.app

import android.app.Application
import android.content.pm.ApplicationInfo
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class MartiApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (isDebuggable()) {
            Timber.plant(Timber.DebugTree())
        }
    }

    // Bu kodu yazmak yerine BuildConfig.DEBUG kullanılabilir.
    private fun isDebuggable(): Boolean {
        return (applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
    }
}