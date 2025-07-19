package com.example.gomimi.dataClass

// UIが曜日ごとに情報をまとめて表示するためのデータ構造
data class GarbageInfo(
    val dayOfWeek: String,      // 例: "月"
    val items: List<GarbageItem>
)

// UIが表示する各ゴミ収集情報の詳細
data class GarbageItem(
    val type: String,           // 例: "資源ごみ"
    val time: String            // 例: "8:30〜10:30"
)