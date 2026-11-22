package com.goianinho.medicina

import android.app.Application
import com.goianinho.medicina.data.AppContainer

class GoianinhoApp : Application() {
    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer()
    }
}
