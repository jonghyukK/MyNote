package com.kjh.data.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.JsonSyntaxException
import com.kjh.data.DataConstants.KAKAO_API_HEADER
import com.kjh.data.DataConstants.KAKAO_REST_API_KEY
import com.kjh.data.DataConstants.KAKAO_API_URL
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 4..
 * Description:
 */

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder().create()

    @Singleton
    @Provides
    fun provideRetrofit(gson: Gson): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            if (!message.startsWith("{") && !message.startsWith("[")) {
                Timber.tag(TAG).d(message)
                return@HttpLoggingInterceptor
            }

            try {
                Timber.tag(TAG).d(GsonBuilder().setPrettyPrinting().create().toJson(JsonParser().parse(message)))
            }
            catch (m: JsonSyntaxException) {
                Timber.tag(TAG).d(message)
            }
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val okHttpClient = OkHttpClient.Builder()
            .callTimeout(1, TimeUnit.MINUTES)
            .connectTimeout(1, TimeUnit.MINUTES)
            .readTimeout(1, TimeUnit.MINUTES)
            .writeTimeout(1, TimeUnit.MINUTES)
            .addInterceptor {
                val requestBuilder = it.request().newBuilder()
                requestBuilder.addHeader(KAKAO_API_HEADER, KAKAO_REST_API_KEY)
                it.proceed(requestBuilder.build())
            }
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl(KAKAO_API_URL)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    private const val TAG = "OKHTTP"
}