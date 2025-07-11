package com.example.gomimi.activity

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Switch
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
//import com.example.gomimi.Manifest ←　これはなんだろう
import com.example.gomimi.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.time.LocalDateTime// 現在の時刻取得するインポート



class NotifySettingsActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.notification_settings)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
        bottomNav.selectedItemId = R.id.navigation_settings
        setupBottomNav(bottomNav)

        val backButton = findViewById<ImageView>(R.id.back_button)
        backButton.setOnClickListener {
            finish() // 現在のActivityを終了して前の画面に戻る
        }
        val spinner: Spinner = findViewById(R.id.languageSpinner)


        val notifyOptions = listOf("前日", "1時間前", "30分前", "15分前")

        //  ArrayAdapter で Spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, notifyOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter


        //=====================================通知機能設定処理======================================

        // findViewByIdリスト
        val notifySwitch : Switch = findViewById(R.id.switch1) // 通知設定のSwitch
        val applyBtn: ImageView = findViewById(R.id.applyBtn) // 通知入力の適用ボタン

        // 変数宣言
        val now = LocalDateTime.now()
//        val day = now.dayOfMonth
//        val hour = now.hour
//        val minute = now.minute

        applyBtn.setOnClickListener {
            // ここでSpinnerの選択に応じて処理を行う
            val selectedItem = spinner.selectedItem.toString()
            val isNotificationEnabled = notifySwitch.isChecked

            if(isNotificationEnabled){
                if(selectedItem == "前日"){

                }else if(selectedItem == "1時間前"){

                }else if(selectedItem == "30分前"){

                }else if(selectedItem == "15分前"){

                }

            }

        }
    }

//    companion object {}
//    オブジェクトにすると別のクラスでオブジェクトに宣言にものを使える
//

    //通知の作成と送信
    private fun sendNotify(){
        //通知タップ時でアプリを起動する
        //Intent(this,行き先は任意)ここで移動先の画面を指定している
        val intent = Intent(this,CalendarActivity::class.java).apply {
            //指定方法（任意）
            //NEW_TASK or CLEAR_TASK (まっさらな状態で起動する（推奨）)
            //NEW_TASK のみ (既存の履歴が残っている場合、その途中から再開することがある)
            //CLEAR_TASK のみ（効果なし）
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
            // 先ほどの「指示書」(intent)を、すぐに実行するのではなく、
            // 未来のタイミング（通知がタップされたとき）で実行できるように
            // **「予約チケット」**(PendingIntent)に変換しています。
            // このチケットをAndroidシステムに渡すことで、
            // システムがユーザーのタップを監視し、代わりにIntentを実行してくれます。
        val penddingIntent : PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        //通知オブジェクトの作成
        var builder = NotificationCompat.Builder(this, MainActivity.CHANNEL_ID_GARBAGE)
            .setSmallIcon(R.drawable.icon) //通知のアイコン
            .setContentTitle("") //通知のタイトル
            .setContentText("") //通知の本文
            .setPriority(NotificationCompat.PRIORITY_DEFAULT) //通知の優先度を設定するために使用
            .setContentIntent(penddingIntent) //通知タップ時に実行するIntent
            .setAutoCancel(true) //通知をタップしたら、その通知が消える

        //通知の発行
        //android13以降は、通知を送信するにはユーザーの許可が必須
        //朴：権限が許可がされてるのかどうか判定だっけ
        //gemini: そのif文は、まさに「通知を投稿する権限がユーザーに許可されていますか？」と判定するための正しいコードですって
//        コードの解説
//        ActivityCompat.checkSelfPermission(...)
//        指定した権限が現在アプリに与えられているかをチェックするメソッドです。
//
//        Manifest.permission.POST_NOTIFICATIONS
//        チェックしたい権限の名前、つまり「通知を投稿する権限」です。
//
//        == PackageManager.PERMISSION_GRANTED
//        チェックした結果が「許可されている (GRANTED)」状態と等しいかどうかを比較しています
        if(ActivityCompat.checkSelfPermission(this,
                Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED){
            with(NotificationManagerCompat.from(this)){
                notify(MainActivity.NOTIFY_ID, builder.build())
            }
        }

    }

}

