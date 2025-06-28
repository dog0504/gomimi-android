package com.example.yourapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.test.GarbageInfo
import com.example.test.GarbageItem
import com.example.test.R
import com.example.test.databinding.ActivityCalendarBinding
import com.example.test.retrofit.BinDay
import com.example.test.retrofit.CalendarViewModel
import com.example.test.retrofit.NetworkResult


class CalendarActivity : BaseActivity() {

    private lateinit var viewBinding: ActivityCalendarBinding

    //    private lateinit var calendarLayout: LinearLayout
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

//        calendarLayout = findViewById(R.id.calendarLayout)

        // BottomNavigationView 設定
//        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
//        bottomNav.selectedItemId = R.id.navigation_calendar
//        setupBottomNav(bottomNav)
//
//        // APIからデータ取得（非同期）
//        lifecycleScope.launch {
//            try {
//                // 1. APIからデータを取得する。この時点でのデータ型はAPIレスポンス用の `List<BinDay>`
//                val response = ApiClient.service.getBinDays()
//
//                // 2. ここでAPI用のデータ構造から「UI表示用のデータ構造」へと変換を行う。
//                //    この変換処理があるおかげで、APIの仕様変更がUIコードに直接影響するのを防げる。
//                val grouped = response.groupBy { convertToShortDay(it.dayOfWeek) }
//                    .map { (day, items) ->
//                        // UI表示用の `GarbageInfo` と `GarbageItem` を生成している
//                        GarbageInfo(day, items.map { GarbageItem(it.type, it.time)})
//                    }
//
//                // 3. UIを更新するメソッドには、整形済みの「UI表示用データ」だけを渡す。
//                populateCalendar(grouped)
//            }catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

//        // サンプル資料
//        val mockData = listOf(
//            GarbageInfo("月", listOf(
//                GarbageItem("資源ごみ", "8:30〜10:30"),
//                GarbageItem("普通ごみ", "12:30〜14:30")
//            )),
//            GarbageInfo("木", listOf(
//                GarbageItem("古紙衣類", ""),
//                GarbageItem("プラスチック資源", "")
//            )),
//            GarbageInfo("金", listOf(
//                GarbageItem("普通ごみ", "12:30〜14:30")
//            ))
//        )

//        populateCalendar(mockData)

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
                    Toast.makeText(this, "データ取得エラー: ${result.message}", Toast.LENGTH_LONG)
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
                GarbageInfo(day, items.map { GarbageItem(it.type, it.time) })
            }
    }

    // UIを組み立てる
    private fun populateCalendar(garbageInfoList: List<GarbageInfo>) {
        // ★ calendarLayoutへのアクセスをbinding経由に変更
        viewBinding.calendarLayout.removeAllViews() // 表示をリセット
        val daysOfWeek = listOf("日", "月", "火", "水", "木", "金", "土")

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
            "普通ごみ" -> R.drawable.normal_gomi
            "資源ごみ" -> R.drawable.recyclable_gomi
            "古紙衣類" -> R.drawable.paper_gomi
            "プラスチック資源" -> R.drawable.plastic
            else -> R.drawable.default_gomi
        }
    }

    // "月曜日" -> "月" のように変換
    private fun convertToShortDay(fullName: String): String {
        return when (fullName) {
            "日曜日" -> "日"; "月曜日" -> "月"; "火曜日" -> "火"; "水曜日" -> "水";
            "木曜日" -> "木"; "金曜日" -> "金"; "土曜日" -> "土"
            else -> fullName.take(1)
        }
    }

//    // UI表示用のデータ(List<GarbageInfo>)を受け取り、画面の要素を実際に組み立てるメソッド。
//    // このメソッドはUI表示用のデータ構造にのみ依存しているため、APIの仕様を知る必要がない。
//    private fun populateCalendar(mockData: List<GarbageInfo>) {
//        val days = listOf("日", "月", "火", "水", "木", "金", "土")
//
//        for (day in days) {
//            val view = LayoutInflater.from(this).inflate(R.layout.item_day, calendarLayout, false)
//            val title = view.findViewById<TextView>(R.id.dayTitle)
//            val iconContainer = view.findViewById<LinearLayout>(R.id.iconContainer)
//            val infoContainer = view.findViewById<LinearLayout>(R.id.garbageInfoContainer)
//
//            title.text = day
//
//            // UI表示用データ `data` から今日の情報を探す
//            val todayInfo = mockData.find { it.dayOfWeek == day }
//
//            // 把圖片加到 title 旁邊的 iconContainer
//            // UI表示用データ `GarbageItem` を使ってアイコンやテキストを配置する
//            todayInfo?.items?.forEach {
//                val icon = ImageView(this)
//                // it.category ではなく it.type を使用（GarbageItemのプロパティ名に合わせる）
//                icon.setImageResource(getImageResourceForCategory(it.type))
//                val iconParams = LinearLayout.LayoutParams(150, 150)
//                iconParams.setMargins(10, 0, 10, 0)
//                icon.layoutParams = iconParams
//                iconContainer.addView(icon)
//            }
//            todayInfo?.items?.forEach {
//                val info = TextView(this)
//                // it.category ではなく it.type を使用（GarbageItemのプロパティ名に合わせる）
//                info.text = if (it.time.isNotEmpty()) "${it.type}　${it.time}" else it.type
//                info.textSize = 16f
//                info.setPadding(0, 4, 0, 4)
//                infoContainer.addView(info)
//            }
//
//            view.setOnClickListener {
//                infoContainer.visibility =
//                    if (infoContainer.visibility == View.GONE) View.VISIBLE else View.GONE
//            }
//
//            calendarLayout.addView(view)
//        }
//    }
//    private fun getImageResourceForCategory(category: String): Int {
//        return when (category) {
//            "普通ごみ" -> R.drawable.normal_gomi
//            "資源ごみ" -> R.drawable.recyclable_gomi
//            "古紙衣類" -> R.drawable.paper_gomi
//            "プラスチック資源" -> R.drawable.plastic
//            else -> R.drawable.default_gomi
//        }
//    }
//
//    private fun convertToShortDay(full: String): String {
//        return when (full) {
//            "日曜日" -> "日"
//            "月曜日" -> "月"
//            "火曜日" -> "火"
//            "水曜日" -> "水"
//            "木曜日" -> "木"
//            "金曜日" -> "金"
//            "土曜日" -> "土"
//            else -> full.take(1)
//        }
//    }
//}
//
//// ▼▼▼ UI表示用のデータクラス ▼▼▼
//// --------------------------------------------------------------------------------
//// UIが「画面に何を表示するか」に特化して定義したクラス群。
//// APIのデータ構造とは独立しているため、UIの都合だけで自由にプロパティを追加・変更できる。
//
//// UIが「曜日ごと」に情報をまとめて表示するために定義した、UI専用のデータ構造。
//
//
//// 資料類別
//data class GarbageInfo(
//    val dayOfWeek: String,
//    val items: List<GarbageItem>
//)
//
//data class GarbageItem(
//    val type: String,
//    val time: String
//)
//// --------------------------------------------------------------------------------
//// ▲▲▲ UI表示用のデータクラス ▲▲▲
//
//
//// ▼▼▼ APIレスポンス用のデータクラス ▼▼▼
//// --------------------------------------------------------------------------------
//// APIから返されるJSONの構造と完全に一致させる必要があるクラス。
//// このクラスの役割は「APIとの正しい通信」のみ。UIロジックで直接は使わない。
//data class BinDay(
//    val id: Int,
//    val type: String,
//    val dayOfWeek: String,
//    val time: String
//)
//
//// --------------------------------------------------------------------------------
//// ▲▲▲ APIレスポンス用のデータクラス ▲▲▲
//
//// APIエンドポイントを定義する
//interface ApiService {
//    @GET("/user/me/bin-days")
//    suspend fun getBinDays(): List<BinDay>
//
//}
//
//// Retrofitクライアント生成
//// Retrofit本体を初期化し、APIサービスを使えるようにするため
//object ApiClient {
//    private val retrofit = Retrofit.Builder()
//        // TODO: 実際の接続先に修正してください
//        .baseUrl("接続先のホスト")
//        .addConverterFactory(GsonConverterFactory.create())
//        .build()
//
//    val service: ApiService = retrofit.create(ApiService::class.java)
//}
}
