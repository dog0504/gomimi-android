package com.example.gomimi.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.gomimi.R
import com.example.gomimi.dataClass.BinDay
import com.example.gomimi.dataClass.GarbageInfo
import com.example.gomimi.dataClass.GarbageItem
import com.example.gomimi.databinding.ActivityCalendarBinding
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.viewModel.CalendarViewModel


class CalendarActivity : BaseActivity() {
    private lateinit var viewBinding: ActivityCalendarBinding
    private lateinit var viewModel: CalendarViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // ViewModelを初期化
        viewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)

        // BottomNavigationView 設定 (binding経由でアクセス)
        setupBottomNav(viewBinding.bottomMenu)
        viewBinding.bottomMenu.selectedItemId = R.id.navigation_calendar

        // LiveDataの監視を開始
        observeBinDays()

        // ViewModel経由でデータを取得
        viewModel.fetchBinDays()

    }

    private fun observeBinDays() {
        viewModel.binDays.observe(this) { result ->
            // ★ ローディング表示をbinding経由で制御
            viewBinding.progressBar.visibility =
                if (result is NetworkResult.Loading) View.VISIBLE else View.GONE

            when (result) {
                is NetworkResult.Success -> {
                    // APIのデータをUI表示用データに変換
                    val uiData = transformToUiData(result.data)
                    // UIを更新
                    populateCalendar(uiData)
                }

                is NetworkResult.Error -> {
                    Toast.makeText(this, "@string/date_not_found ${result.message}", Toast.LENGTH_LONG)
                        .show()
                }

                is NetworkResult.Loading -> {
                    // ローディング表示中は何もしない
                }
            }
        }
    }

    // APIデータ(List<BinDay>)からUI表示用データ(List<GarbageInfo>)へ変換
    private fun transformToUiData(apiData: List<BinDay>): List<GarbageInfo> {
        return apiData
            .groupBy { convertToShortDay(it.dayOfWeek) } // 曜日でグループ化
            .map { (day, items) ->
                GarbageInfo(
                    day,
                    items.map { GarbageItem(it.type, it.time) })
            }
    }

    // UIを組み立てる
    private fun populateCalendar(garbageInfoList: List<GarbageInfo>) {
        // ★ calendarLayoutへのアクセスをbinding経由に変更
        viewBinding.calendarLayout.removeAllViews() // 表示をリセット
        val daysOfWeek = listOf("@string/Mon", "@string/Tue", "@string/Wed", "@string/Thu", "@string/Fri", "@string/Sat", "@string/Sun")

        for (day in daysOfWeek) {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_day, viewBinding.calendarLayout, false)
            val title = view.findViewById<TextView>(R.id.dayTitle)
            val iconContainer = view.findViewById<LinearLayout>(R.id.iconContainer)
            val infoContainer = view.findViewById<LinearLayout>(R.id.garbageInfoContainer)

            title.text = day
            val todayInfo = garbageInfoList.find { it.dayOfWeek == day }

            todayInfo?.items?.forEach { item ->
                // アイコンの追加
                val icon = ImageView(this)
                icon.setImageResource(getImageResourceForCategory(item.type))
                val iconParams = LinearLayout.LayoutParams(150, 150)
                iconParams.setMargins(10, 0, 10, 0)
                icon.layoutParams = iconParams
                iconContainer.addView(icon)

                // 詳細テキストの追加
                val infoText = TextView(this)
                infoText.text =
                    if (item.time.isNotEmpty()) "${item.type}　${item.time}" else item.type
                infoText.textSize = 16f
                infoText.setPadding(0, 4, 0, 4)
                infoContainer.addView(infoText)
            }

            view.setOnClickListener {
                infoContainer.visibility =
                    if (infoContainer.visibility == View.GONE) View.VISIBLE else View.GONE
            }

            viewBinding.calendarLayout.addView(view)
        }
    }

    // カテゴリ名から画像リソースIDを取得
    private fun getImageResourceForCategory(category: String): Int {
        return when (category) {
            "@string/normal_gomi" -> R.drawable.normal_gomi
            "@string/recyclable_gomi" -> R.drawable.recyclable_gomi
            "@string/paper_gomi" -> R.drawable.paper_gomi
            "@string/plastic" -> R.drawable.plastic
            else -> R.drawable.default_gomi
        }
    }

    // "月曜日" -> "月" のように変換
    private fun convertToShortDay(fullName: String): String {
        return when (fullName) {
            "日曜日" -> "@string/Sun"; "月曜日" -> "@string/Mon"; "火曜日" -> "@string/Tue"; "水曜日" -> "@string/Wed";
            "木曜日" -> "@string/Thu"; "金曜日" -> "@string/Fri"; "土曜日" -> "@string/Sat"
            else -> fullName.take(1)
        }
    }
}
