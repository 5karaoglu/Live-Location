package com.besirkaraoglu.locationsharingsampleii

import android.app.Application
import com.huawei.hms.maps.MapsInitializer

class LSSApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        MapsInitializer.setApiKey("DAEDAOKMFvXBACtts+kTjEtZQlpsNteIiTCEqxF6NGNYoOzF7Qw+47f9tA1ixeMe3EAfS27uV2XdZkkZkuzCdI3fBUd7eRjUMFWVkg==")
        MapsInitializer.initialize(this)

    }
}