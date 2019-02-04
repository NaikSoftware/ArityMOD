// Copyright (C) 2009 Mihai Preda, (C) 2019 Nickolay Savchenko

package ua.naiksoftware.aritymod

import android.app.Application

import timber.log.Timber
import ua.naiksoftware.aritymod.service.ServicesModule

class ArityApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        Injector.appComponent = DaggerAppComponent.builder()
                .servicesModule(ServicesModule(this))
                .build()
    }
}

class Injector {

    companion object {
        lateinit var appComponent: AppComponent
    }
}
