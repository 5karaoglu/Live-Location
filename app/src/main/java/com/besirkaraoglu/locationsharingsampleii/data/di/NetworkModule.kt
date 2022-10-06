package com.besirkaraoglu.locationsharingsampleii.data.di

import com.huawei.agconnect.auth.AGConnectAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class NetworkModule {

    @Provides
    @Singleton
    fun provideAgConnectAuth(): AGConnectAuth = AGConnectAuth.getInstance()

}