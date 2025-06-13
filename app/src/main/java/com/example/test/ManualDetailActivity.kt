package com.example.test

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import com.example.yourapp.BaseActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class ManualDetailActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.manual_detail)
        val title = intent.getStringExtra("title")
        val description = intent.getStringExtra("description")

        findViewById<TextView>(R.id.textView).text = title
        findViewById<TextView>(R.id.textView2).text = description


        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
        bottomNav.selectedItemId = R.id.navigation_manual
        setupBottomNav(bottomNav)

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish() // 現在のActivityを終了して前の画面に戻る
        }
    }
}