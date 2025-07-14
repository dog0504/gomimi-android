package com.example.gomimi.receiver

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.gomimi.R
import com.example.gomimi.activity.CalendarActivity
import com.example.gomimi.activity.MainActivity

class NotificationReceiver : BroadcastReceiver() {

    // AlarmManagerに起こされたときに、この onReceive メソッドが実行される
    override fun onReceive(context: Context, intent: Intent) {
        sendNotify(context) // 通知を送信する処理を呼び出す
    }

    private fun sendNotify(context: Context) {
        // 通知タップ時にカレンダー画面を開くIntent
        val calendarIntent = Intent(context, CalendarActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context, 0, calendarIntent, PendingIntent.FLAG_IMMUTABLE
        )

        // 通知オブジェクトの作成
        val builder = NotificationCompat.Builder(context, MainActivity.CHANNEL_ID_GARBAGE)
            .setSmallIcon(R.drawable.icon)
            .setContentTitle("ゴミ収集日のお知らせ")
            .setContentText("今日はゴミの収集日です！忘れずにゴミを出しましょう。")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // 権限をチェックしてから通知を発行
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(context)) {
                notify(MainActivity.NOTIFY_ID, builder.build())
            }
        }
    }
}