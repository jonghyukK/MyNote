package com.kjh.data.di

import com.example.domain.model.PlaceNote
import com.example.domain.repository.KakaoMapRepository
import com.example.domain.repository.PlaceNoteRepository
import com.kjh.data.repository.KakaoMapRepositoryImpl
import com.kjh.data.repository.PlaceNoteRepositoryImpl
import com.kjh.data.source.local.PlaceNoteDao
import com.kjh.data.source.remote.KakaoApiService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 29..
 * Description:
 */

@Module
@InstallIn(SingletonComponent::class)
interface RepositoryModule {

    @Binds
    @Singleton
    fun bindPlaceNoteRepository(
        placeNoteRepositoryImpl: PlaceNoteRepositoryImpl
    ): PlaceNoteRepository

    @Binds
    @Singleton
    fun bindKakaoMapRepository(
        kakaoMapRepositoryImpl: KakaoMapRepositoryImpl
    ): KakaoMapRepository
}