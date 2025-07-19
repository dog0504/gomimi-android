package com.example.gomimi.activity

import android.app.Application
import com.orhanobut.hawk.Hawk

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // アプリ起動時にHawkを初期化する
        Hawk.init(this).build()
    }
}