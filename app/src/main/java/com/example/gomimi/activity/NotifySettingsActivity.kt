package com.example.gomimi.activity

import android.app.NotificationChannel // 通知チャンネルのインポート
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import com.example.gomimi.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDateTime// 現在の時刻取得するインポート



class NotifySettingsActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_settings)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
        bottomNav.selectedItemId = R.id.navigation_settings
        setupBottomNav(bottomNav)

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish() // 現在のActivityを終了して前の画面に戻る
        }
        val spinner: Spinner = findViewById(R.id.languageSpinner)


        val notifyOptions = listOf("前日", "1時間前", "30分前", "15分前")

        //  ArrayAdapter で Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, notifyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter


        //=====================================通知設定処理======================================

        // findViewByIdリスト
        val notifySwitch : Switch = findViewById(R.id.switch1) // 通知設定のSwitch
        val applyBtn: ImageView = findViewById(R.id.applyBtn) // 通知入力の適用ボタン

        // 変数宣言
        val currentdateTime = LocalDateTime.now()
        val day = currentdateTime.dayOfMonth
        val hour = currentdateTime.hour
        val minute = currentdateTime.minute

        // 通知チャネルの生成


        // 適用ボタンの処理
        applyBtn.setOnClickListener {
            // ここでSpinnerの選択に応じて処理を行う
            val selectedItem = spinner.selectedItem.toString()
            val isNotificationEnabled = notifySwitch.isChecked




        }

        //=====================================================================================


    }
}

