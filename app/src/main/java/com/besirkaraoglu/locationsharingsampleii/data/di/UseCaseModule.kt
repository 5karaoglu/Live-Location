package com.besirkaraoglu.locationsharingsampleii.data.di

import android.content.Context
import com.besirkaraoglu.locationsharingsampleii.data.LoginRepository
import com.besirkaraoglu.locationsharingsampleii.domain.LoginUseCase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    @Provides
    @Singleton
    fun provideLoginUseCase(@ApplicationContext applicationContext: Context,
                            loginRepository: LoginRepository
    ) = LoginUseCase(applicationContext, loginRepository)

}