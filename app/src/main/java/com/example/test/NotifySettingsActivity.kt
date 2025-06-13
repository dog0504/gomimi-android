package com.example.yourapp

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import com.example.test.R
import com.google.android.material.bottomnavigation.BottomNavigationView

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
        val spinner: Spinner = findViewById(R.id.spinner2)


        val notifyOptions = listOf("前日", "1時間前", "30分前", "15分前")

//  ArrayAdapter で Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, notifyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

    }
}
