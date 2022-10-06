package com.besirkaraoglu.locationsharingsampleii.data.di

import com.besirkaraoglu.locationsharingsampleii.data.LoginRepository
import com.besirkaraoglu.locationsharingsampleii.data.UsersRepository
import com.huawei.agconnect.auth.AGConnectAuth
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class RepositoryModule {

    @Provides
    @Singleton
    fun provideUserRepository() = UsersRepository()

    @Provides
    @Singleton
    fun provideLoginRepository(agConnectAuth: AGConnectAuth) = LoginRepository(agConnectAuth)

}