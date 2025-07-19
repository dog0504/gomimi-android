package com.example.gomimi.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.gomimi.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.gomimi.utils.LocaleHelper

open class BaseActivity : AppCompatActivity() {

    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val langCode = prefs.getString("lang_code", "ja") ?: "ja" //日文
        val context = LocaleHelper.setAppLocale(newBase, langCode)
        super.attachBaseContext(context)
    }

    protected fun setupBottomNav(bottomNav: BottomNavigationView) {
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_manual -> {
                    if (this !is ManualActivity) {
                        startActivity(Intent(this, ManualActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.navigation_calendar -> {
                    if (this !is CalendarActivity) {
                        startActivity(Intent(this, CalendarActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.navigation_camera -> {
                    if (this !is MainActivity) {
                        startActivity(Intent(this, MainActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.navigation_history -> {
                    if (this !is HistoryActivity) {
                        startActivity(Intent(this, HistoryActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                R.id.navigation_settings -> {
                    if (this !is SettingsActivity) {
                        startActivity(Intent(this, SettingsActivity::class.java))
                        overridePendingTransition(0, 0)
                    }
                    true
                }
                else -> false
            }
        }
    }

}
