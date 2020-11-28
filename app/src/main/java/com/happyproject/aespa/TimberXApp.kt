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

package com.happyproject.aespa

import android.app.Application
import com.happyproject.aespa.BuildConfig.DEBUG
import com.happyproject.aespa.db.roomModule
import com.happyproject.aespa.logging.FabricTree
import com.happyproject.aespa.network.lastFmModule
import com.happyproject.aespa.network.networkModule
import com.happyproject.aespa.notifications.notificationModule
import com.happyproject.aespa.permissions.permissionsModule
import com.happyproject.aespa.playback.mediaModule
import com.happyproject.aespa.repository.repositoriesModule
import com.happyproject.aespa.ui.viewmodels.viewModelsModule
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
