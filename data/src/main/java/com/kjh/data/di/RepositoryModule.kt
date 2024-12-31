package com.kjh.data.di

import com.example.domain.repository.CategoryRepository
import com.example.domain.repository.KakaoMapRepository
import com.example.domain.repository.PaymentMethodRepository
import com.example.domain.repository.PlaceNoteRepository
import com.example.domain.repository.PurchaseNoteRepository
import com.example.domain.repository.PurchaseNoteWithCategoryRepository
import com.kjh.data.repository.CategoryRepositoryImpl
import com.kjh.data.repository.KakaoMapRepositoryImpl
import com.kjh.data.repository.PaymentMethodRepositoryImpl
import com.kjh.data.repository.PlaceNoteRepositoryImpl
import com.kjh.data.repository.PurchaseNoteRepositoryImpl
import com.kjh.data.repository.PurchaseNoteWithCategoryRepositoryImpl
import dagger.Binds
import dagger.Module
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
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlaceNoteRepository(
        placeNoteRepositoryImpl: PlaceNoteRepositoryImpl
    ): PlaceNoteRepository

    @Binds
    @Singleton
    abstract fun bindKakaoMapRepository(
        kakaoMapRepositoryImpl: KakaoMapRepositoryImpl
    ): KakaoMapRepository

    @Binds
    @Singleton
    abstract fun bindPurchaseNoteRepository(
        purchaseNoteRepositoryImpl: PurchaseNoteRepositoryImpl
    ): PurchaseNoteRepository

    @Binds
    @Singleton
    abstract fun bindCategoryRepository(
        categoryRepositoryImpl: CategoryRepositoryImpl
    ): CategoryRepository

    @Binds
    @Singleton
    abstract fun bindPurchaseNoteWithCategoryRepository(
        purchaseNoteWithCategoryRepositoryImpl: PurchaseNoteWithCategoryRepositoryImpl
    ): PurchaseNoteWithCategoryRepository

    @Binds
    @Singleton
    abstract fun bindPaymentMethodRepository(
        paymentMethodRepositoryImpl: PaymentMethodRepositoryImpl
    ): PaymentMethodRepository
}