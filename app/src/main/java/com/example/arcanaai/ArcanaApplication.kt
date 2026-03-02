package com.example.arcanaai

import android.app.Application
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ArcanaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // 이제 BuildConfig에서 안전하게 키를 가져온다냥!
        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}
