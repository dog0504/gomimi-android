package com.example.yourapp

import HistoryAdapter
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.R
import com.example.test.HistoryItem


import com.google.android.material.bottomnavigation.BottomNavigationView

class HistoryActivity : BaseActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        // 假資料
        val dummyData = listOf(
            HistoryItem("ペットボトル", "2025/5/23 10:21"),
            HistoryItem("缶", "2025/5/24 14:35"),
            HistoryItem("紙パック", "2025/5/25 09:50"),
            HistoryItem("段ボール", "2025/5/26 11:05"),
            HistoryItem("ガラス瓶", "2025/5/27 13:45")
        )

        // RecyclerView 設定
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HistoryAdapter(dummyData)
        recyclerView.adapter = adapter

        // 底部導覽
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
        bottomNav.selectedItemId = R.id.navigation_history
        setupBottomNav(bottomNav) // 若你在 BaseActivity 定義了該函數
    }
}
