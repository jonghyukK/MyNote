package com.kjh.mynote

import android.app.Application
import com.kakao.vectormap.KakaoMapSdk
import com.kjh.data.DataConstants
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * Created by kangjonghyuk.
 * Created On 2024. 9. 24..
 * Description:
 */

@HiltAndroidApp
class MyNoteApplication: Application() {

    override fun onCreate() {
        super.onCreate()

        initTimber()
        initKakaoSDK()
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }

    private fun initKakaoSDK() {
        KakaoMapSdk.init(this, DataConstants.KAKAO_NATIVE_APP_KEY)
    }
}