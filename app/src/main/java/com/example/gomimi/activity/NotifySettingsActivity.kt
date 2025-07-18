package com.example.gomimi.activity

import android.provider.Settings
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences // ★ SharedPreferencesをインポート
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.gomimi.R
import com.example.gomimi.dataClass.BinDay
import com.example.gomimi.receiver.NotificationReceiver
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.viewModel.CalendarViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime// 現在の時刻取得するインポート
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters


class NotifySettingsActivity: BaseActivity() {

    private lateinit var viewModel: CalendarViewModel

    // ★ 1. SharedPreferencesのプロパティとキーを定義
    private lateinit var prefs: SharedPreferences
    companion object {
        private const val PREFS_NAME = "notification_settings_prefs"
        private const val KEY_SWITCH_STATE = "switch_state"
        private const val KEY_SPINNER_POSITION = "spinner_position"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_settings)

        // ★ 2. SharedPreferencesを初期化
        prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // view初期化
        viewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)

        // UIのセットアップ
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
        bottomNav.selectedItemId = R.id.navigation_settings
        setupBottomNav(bottomNav)

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish() // 現在のActivityを終了して前の画面に戻る
        }

        val spinner: Spinner = findViewById(R.id.timeSetSpinner)
        val notifyOptions = listOf("前日", "1時間前", "30分前", "15分前")

        //  ArrayAdapter で Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, notifyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        //=====================================通知機能設定処理======================================

        // findViewByIdリスト
        val notifySwitch : Switch = findViewById(R.id.notify_set_switch1) // 通知設定のSwitch
        val applyBtn: ImageView = findViewById(R.id.applyBtn) // 通知入力の適用ボタン

        // ★ 3. 画面作成時に保存された設定を読み込んでUIに反映
        loadSettings(notifySwitch, spinner)

        // ボタンのクリック処理
        applyBtn.setOnClickListener {
            // 1. ユーザーの選択内容を取得
            val selectedPosition = spinner.selectedItemPosition // 位置を取得
            val selectedItem = spinner.selectedItem.toString()
            val isNotificationEnabled = notifySwitch.isChecked

            // ★ 4. 設定を適用するタイミングで保存する
            saveSettings(isNotificationEnabled, selectedPosition)

            // 2. スイッチがOFFなら、キャンセル処理をして終了
            if (!isNotificationEnabled) {
                viewModel.binDays.value?.let { result ->
                    if (result is NetworkResult.Success) {
                        result.data.forEach { binDay -> cancelNotification(binDay) }
                    }
                }
                Toast.makeText(this, "すべての通知予約をキャンセルしました", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // --- ここからはスイッチがONの場合の処理 ---

            // 3. ごみ収集日のデータがあるか確認
            val result = viewModel.binDays.value
            if (result !is NetworkResult.Success) {
                Toast.makeText(this, "ごみ収集日データを取得中です。しばらく待ってからもう一度お試しください。", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 4. アラームの権限をチェック
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                // 権限がなければ設定画面へ誘導して終了
                Toast.makeText(this, "通知を予約するには、アラームとリマインダーの権限を許可してください", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM))
                return@setOnClickListener
            }

            // 5. すべてのチェックを通過後、全件の通知を予約
            result.data.forEach { binDay ->
                val nextCollectionDateTime = findNextCollectionDateTime(binDay)
                val triggerDateTime = calculateTriggerTime(nextCollectionDateTime, selectedItem)
                Log.d("NotificationDebug", "予約する日時: $triggerDateTime, ゴミの種類: ${binDay.type}")
                scheduleNotification(triggerDateTime, binDay)
            }

            Toast.makeText(this, "すべての収集日の通知を「${selectedItem}」に予約しました", Toast.LENGTH_LONG).show()
        }
        // --- データ取得の開始 ---
        viewModel.fetchBinDays()
    }

    // ★ 5. 設定を保存/読み込みする関数を追加
    private fun saveSettings(switchState: Boolean, spinnerPosition: Int) {
        with(prefs.edit()) {
            putBoolean(KEY_SWITCH_STATE, switchState)
            putInt(KEY_SPINNER_POSITION, spinnerPosition)
            apply() // 保存を確定
        }
    }

    private fun loadSettings(switch: Switch, spinner: Spinner) {
        val switchState = prefs.getBoolean(KEY_SWITCH_STATE, false) // 保存値がなければfalse
        val spinnerPosition = prefs.getInt(KEY_SPINNER_POSITION, 0) // 保存値がなければ0番目
        switch.isChecked = switchState
        spinner.setSelection(spinnerPosition)
    }


    private fun calculateTriggerTime(baseTime: LocalDateTime, offset: String): LocalDateTime {
        return when (offset) {
            "前日" -> baseTime.minusHours(12)
            "1時間前" -> baseTime.minusHours(1)
            "30分前" -> baseTime.minusMinutes(30)
            "15分前" -> baseTime.minusMinutes(15)
            else -> baseTime
        }
    }

    private fun findNextCollectionDateTime(binDay: BinDay): LocalDateTime {
        val targetDayOfWeek = when (binDay.dayOfWeek) {
            "月曜日" -> DayOfWeek.MONDAY; "火曜日" -> DayOfWeek.TUESDAY; "水曜日" -> DayOfWeek.WEDNESDAY
            "木曜日" -> DayOfWeek.THURSDAY; "金曜日" -> DayOfWeek.FRIDAY; "土曜日" -> DayOfWeek.SATURDAY
            "日曜日" -> DayOfWeek.SUNDAY
            else -> return LocalDateTime.now() // 不正な曜日の場合は現在時刻を返す
        }

        // ★ 1. 時刻の書式（フォーマット）を定義します
        // "H" は時間が1桁でも2桁でもOKという意味
        val timeFormatter = DateTimeFormatter.ofPattern("H:mm")

        val timeString = binDay.time.split('~')[0].trim()

        // ★ 2. 定義した書式を使って、時刻の文字列を解析します
        val collectionTime = LocalTime.parse(timeString, timeFormatter)

        val now = LocalDateTime.now()
        // 次の収集日（今日を含む）の日付を取得
        var nextCollectionDate = now.toLocalDate().with(TemporalAdjusters.nextOrSame(targetDayOfWeek))
        // 日付と時間を組み合わせて、次の収集日時を組み立てる
        var nextCollectionDateTime = nextCollectionDate.atTime(collectionTime)

        // もし計算した収集日時が現在よりも過去なら、来週の同じ曜日に設定する
        if (nextCollectionDateTime.isBefore(now)) {
            nextCollectionDateTime = nextCollectionDateTime.plusWeeks(1)
        }
        return nextCollectionDateTime
    }

    // 通知を予約（スケジュール）する関数
    private fun scheduleNotification(triggerDateTime: LocalDateTime, binDay: BinDay) {
        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("garbage_type", binDay.type)
            putExtra("notification_id", binDay.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, binDay.id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val triggerTimeMillis = triggerDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // --- ★ ここがアラーム権限のチェックとハンドリング ---
        // Android 12 (API 31) 以降か確認
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // 正確なアラームをスケジュールする権限があるか確認
            if (!alarmManager.canScheduleExactAlarms()) {
                // 権限がない場合、ユーザーを権限設定画面に誘導する
                Toast.makeText(this, "通知を予約するには、アラームとリマインダーの権限を許可してください", Toast.LENGTH_LONG).show()
                // OSの権限設定画面を開く
                val settingsIntent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                startActivity(settingsIntent)
                return // アラームをセットせずに処理を中断
            }
        }

        // 権限がある場合のみアラームをセットする
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTimeMillis, pendingIntent)
    }

    private fun cancelNotification(binDay: BinDay) {
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, binDay.id, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_NO_CREATE
        )
        if (pendingIntent != null) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            alarmManager.cancel(pendingIntent)
        }
    }

}

