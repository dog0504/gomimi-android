package com.example.yourapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.test.R

class CalendarActivity : AppCompatActivity() {

    private lateinit var calendarLayout: LinearLayout  // カレンダーを表示するレイアウト

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calendar)

        calendarLayout = findViewById(R.id.calendarLayout)

        // 🔧 モックデータ（仮のゴミ収集情報）
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

        // カレンダーに情報を表示
        populateCalendar(mockData)
    }

    /**
     * 各曜日のゴミ収集情報をカレンダーに追加する関数
     */
    private fun populateCalendar(mockData: List<GarbageInfo>) {
        val days = listOf("日", "月", "火", "水", "木", "金", "土")  // 曜日リスト

        for (day in days) {
            // カスタムレイアウト（item_day.xml）を読み込む
            val view = LayoutInflater.from(this).inflate(R.layout.item_day, calendarLayout, false)
            val title = view.findViewById<TextView>(R.id.dayTitle) // 曜日タイトル
            val infoContainer = view.findViewById<LinearLayout>(R.id.garbageInfoContainer) // ゴミ情報表示エリア

            title.text = day // 曜日名を設定

            // 現在の曜日のゴミ情報を取得
            val todayInfo = mockData.find { it.dayOfWeek == day }

            // ゴミ情報を動的に追加
            todayInfo?.items?.forEach {
                val info = TextView(this)
                info.text = if (it.time.isNotEmpty()) "${it.category}　${it.time}" else it.category
                info.textSize = 16f
                info.setPadding(0, 4, 0, 4)
                infoContainer.addView(info)
            }

            // タップするとゴミ情報を展開・非表示切り替え
            view.setOnClickListener {
                infoContainer.visibility =
                    if (infoContainer.visibility == View.GONE) View.VISIBLE else View.GONE
            }

            // カレンダーのレイアウトに追加
            calendarLayout.addView(view)
        }
    }
}

// ゴミ情報（曜日ごと）
data class GarbageInfo(
    val dayOfWeek: String,      // "月" など
    val items: List<GarbageItem> // 各曜日に複数の収集項目
)

// 各ゴミ収集項目のデータ
data class GarbageItem(
    val category: String, // 例: "資源ごみ"
    val time: String      // 例: "8:30〜10:30"
)
