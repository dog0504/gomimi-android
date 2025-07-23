package com.example.gomimi.activity

import android.os.Bundle
import android.util.Log
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

        viewModel = ViewModelProvider(this)[CalendarViewModel::class.java]

        setupBottomNav(viewBinding.bottomMenu)
        viewBinding.bottomMenu.selectedItemId = R.id.navigation_calendar

        observeBinDays()
        viewModel.fetchBinDays()
    }

    private fun observeBinDays() {
        viewModel.binDays.observe(this) { result ->
            viewBinding.progressBar.visibility =
                if (result is NetworkResult.Loading) View.VISIBLE else View.GONE

            when (result) {
                is NetworkResult.Success -> {
                    val uiData = transformToUiData(result.data)
                    populateCalendar(uiData)
                }

                is NetworkResult.Error -> {
                    Toast.makeText(
                        this,
                        getString(R.string.date_not_found) + " : " + result.message,
                        Toast.LENGTH_LONG
                    ).show()
                    Log.e("CalendarActivity", "API Error: ${result.message}")
                }

                is NetworkResult.Loading -> {
                    // do nothing
                }
            }
        }
    }

    private fun transformToUiData(apiData: List<BinDay>): List<GarbageInfo> {
        return apiData
            .groupBy { convertToShortDay(it.dayOfWeek) }
            .map { (day, items) ->
                GarbageInfo(
                    day,
                    items.map { GarbageItem(it.type, it.time ?: "") })
            }
    }

    private fun populateCalendar(garbageInfoList: List<GarbageInfo>) {
        viewBinding.calendarLayout.removeAllViews()
        val daysOfWeek = listOf(
            "Mon" to getString(R.string.Mon),
            "Tue" to getString(R.string.Tue),
            "Wed" to getString(R.string.Wed),
            "Thu" to getString(R.string.Thu),
            "Fri" to getString(R.string.Fri),
            "Sat" to getString(R.string.Sat),
            "Sun" to getString(R.string.Sun)
        )

        for ((dayKey, label) in daysOfWeek) {
            val view = LayoutInflater.from(this)
                .inflate(R.layout.item_day, viewBinding.calendarLayout, false)
            val title = view.findViewById<TextView>(R.id.dayTitle)
            val iconContainer = view.findViewById<LinearLayout>(R.id.iconContainer)
            val infoContainer = view.findViewById<LinearLayout>(R.id.garbageInfoContainer)

            title.text = label
            val todayInfo = garbageInfoList.find { it.dayOfWeek == dayKey }

            todayInfo?.items?.forEach { item ->
                val icon = ImageView(this)
                icon.setImageResource(getImageResourceForCategory(item.type))
                val iconParams = LinearLayout.LayoutParams(150, 150)
                iconParams.setMargins(10, 0, 10, 0)
                icon.layoutParams = iconParams
                iconContainer.addView(icon)

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

    private fun getImageResourceForCategory(category: String): Int {
        return when (category) {
            getString(R.string.normal_gomi) -> R.drawable.normal_gomi
            getString(R.string.recyclable_gomi) -> R.drawable.recyclable_gomi
            getString(R.string.paper_gomi) -> R.drawable.paper_gomi
            getString(R.string.plastic) -> R.drawable.plastic
            else -> {
                Log.w("CalendarActivity", "Unknown category: $category")
                R.drawable.default_gomi
            }
        }
    }

    private fun convertToShortDay(fullName: String): String {
        return when (fullName) {
            "日曜日" -> "Sun"
            "月曜日" -> "Mon"
            "火曜日" -> "Tue"
            "水曜日" -> "Wed"
            "木曜日" -> "Thu"
            "金曜日" -> "Fri"
            "土曜日" -> "Sat"
            else -> {
                Log.w("CalendarActivity", "Unknown day name: $fullName")
                fullName.take(1)
            }
        }
    }
}
