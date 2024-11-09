package com.hruby.vcelnice

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // Inicializace aplikace (např. logování, nastavení globálních parametrů)
    }
}