package com.besirkaraoglu.locationsharingsampleii.core

import android.app.Application
import com.besirkaraoglu.locationsharingsampleii.util.API_KEY
import com.huawei.hms.maps.MapsInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class LSSApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MapsInitializer.setApiKey(API_KEY)
        MapsInitializer.initialize(this)

    }
}