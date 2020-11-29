/*
 * Copyright (c) 2019 Naman Dwivedi.
 *
 * Licensed under the GNU General Public License v3
 *
 * This is free software: you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 */
@file:Suppress("unused")

package com.happyproject.secretnumberplay

import android.app.Application
import com.happyproject.secretnumberplay.BuildConfig.DEBUG
import com.happyproject.secretnumberplay.db.roomModule
import com.happyproject.secretnumberplay.logging.FabricTree
import com.happyproject.secretnumberplay.network.lastFmModule
import com.happyproject.secretnumberplay.network.networkModule
import com.happyproject.secretnumberplay.notifications.notificationModule
import com.happyproject.secretnumberplay.permissions.permissionsModule
import com.happyproject.secretnumberplay.playback.mediaModule
import com.happyproject.secretnumberplay.repository.repositoriesModule
import com.happyproject.secretnumberplay.ui.viewmodels.viewModelsModule
import org.koin.android.ext.android.startKoin
import timber.log.Timber

class TimberXApp : Application() {

    override fun onCreate() {
        super.onCreate()

        if (DEBUG) {
            Timber.plant(Timber.DebugTree())
        } else {
            Timber.plant(FabricTree())
        }

        val modules = listOf(
                mainModule,
                permissionsModule,
                mediaModule,
                prefsModule,
                networkModule,
                roomModule,
                notificationModule,
                repositoriesModule,
                viewModelsModule,
                lastFmModule
        )
        startKoin(
                androidContext = this,
                modules = modules
        )
    }
}
