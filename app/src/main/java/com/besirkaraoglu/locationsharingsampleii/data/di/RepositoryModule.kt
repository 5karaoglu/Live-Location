package com.besirkaraoglu.locationsharingsampleii.data.di

import com.besirkaraoglu.locationsharingsampleii.data.UsersRepository
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

}