package com.example.yourapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import com.example.test.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.example.yourapp.BaseActivity


class CalendarActivity : BaseActivity() {

    private lateinit var calendarLayout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarLayout = findViewById(R.id.calendarLayout)

        // BottomNavigationView 設定
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
        bottomNav.selectedItemId = R.id.navigation_calendar
        setupBottomNav(bottomNav)

        // 模擬資料
        val mockData = listOf(
            GarbageInfo("月", listOf(
                GarbageItem("資源ごみ", "8:30〜10:30"),
                GarbageItem("普通ごみ", "12:30〜14:30")
            )),
            GarbageInfo("木", listOf(
                GarbageItem("紙類", ""),
                GarbageItem("ペットボトル", "")
            )),
            GarbageInfo("金", listOf(
                GarbageItem("普通ごみ", "")
            ))
        )

        populateCalendar(mockData)
    }

    private fun populateCalendar(mockData: List<GarbageInfo>) {
        val days = listOf("日", "月", "火", "水", "木", "金", "土")

        for (day in days) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_day, calendarLayout, false)
            val title = view.findViewById<TextView>(R.id.dayTitle)
            val infoContainer = view.findViewById<LinearLayout>(R.id.garbageInfoContainer)

            title.text = day

            val todayInfo = mockData.find { it.dayOfWeek == day }

            todayInfo?.items?.forEach {
                val info = TextView(this)
                info.text = if (it.time.isNotEmpty()) "${it.category}　${it.time}" else it.category
                info.textSize = 16f
                info.setPadding(0, 4, 0, 4)
                infoContainer.addView(info)
            }

            view.setOnClickListener {
                infoContainer.visibility =
                    if (infoContainer.visibility == View.GONE) View.VISIBLE else View.GONE
            }

            calendarLayout.addView(view)
        }
    }
}

// 資料類別
data class GarbageInfo(
    val dayOfWeek: String,
    val items: List<GarbageItem>
)

data class GarbageItem(
    val category: String,
    val time: String
)
