package com.example.yourapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
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

        // サンプル資料
        val mockData = listOf(
            GarbageInfo("月", listOf(
                GarbageItem("資源ごみ", "8:30〜10:30"),
                GarbageItem("普通ごみ", "12:30〜14:30")
            )),
            GarbageInfo("木", listOf(
                GarbageItem("古紙衣類", ""),
                GarbageItem("プラスチック資源", "")
            )),
            GarbageInfo("金", listOf(
                GarbageItem("普通ごみ", "12:30〜14:30")
            ))
        )

        populateCalendar(mockData)
    }

    private fun populateCalendar(mockData: List<GarbageInfo>) {
        val days = listOf("日", "月", "火", "水", "木", "金", "土")

        for (day in days) {
            val view = LayoutInflater.from(this).inflate(R.layout.item_day, calendarLayout, false)
            val title = view.findViewById<TextView>(R.id.dayTitle)
            val iconContainer = view.findViewById<LinearLayout>(R.id.iconContainer)
            val infoContainer = view.findViewById<LinearLayout>(R.id.garbageInfoContainer)

            title.text = day

            val todayInfo = mockData.find { it.dayOfWeek == day }
            // 把圖片加到 title 旁邊的 iconContainer
            todayInfo?.items?.forEach {
                val icon = ImageView(this)
                icon.setImageResource(getImageResourceForCategory(it.category))
                val iconParams = LinearLayout.LayoutParams(150, 150)
                iconParams.setMargins(10, 0, 10, 0)
                icon.layoutParams = iconParams
                iconContainer.addView(icon)
            }
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
    private fun getImageResourceForCategory(category: String): Int {
        return when (category) {
            "普通ごみ" -> R.drawable.normal_gomi
            "資源ごみ" -> R.drawable.recyclable_gomi
            "古紙衣類" -> R.drawable.paper_gomi
            "プラスチック資源" -> R.drawable.plastic
            else -> R.drawable.default_gomi
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
