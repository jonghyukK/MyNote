package com.kjh.data.di

import com.kjh.data.source.remote.KakaoApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */

@Module
@InstallIn(SingletonComponent::class)
object ApiModule {

    @Singleton
    @Provides
    fun provideKakaoApiService(retrofit: Retrofit): KakaoApiService {
        return retrofit.create(KakaoApiService::class.java)
    }
}