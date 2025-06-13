package com.example.yourapp

import ManualAdapter
import android.os.Bundle
import android.widget.EditText
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.test.ManualItem
import com.example.test.R
import com.google.android.material.bottomnavigation.BottomNavigationView

class ManualActivity : BaseActivity() {

    private lateinit var adapter: ManualAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manual)

        val manualData = listOf(
            ManualItem("ペットボトル", "キャップとラベルを外して捨ててください。"),
            ManualItem("缶", "中をすすいでから捨ててください。"),
            ManualItem("ガラス瓶", "透明・茶・その他で分別してください。"),
            ManualItem("段ボール", "折りたたんでひもで縛ってください。"),
            ManualItem("電池", "回収ボックスへ入れてください。")
        )

        searchEditText = findViewById(R.id.search_edit_text)
        recyclerView = findViewById(R.id.manual_recycler)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = ManualAdapter(this, manualData)
        recyclerView.adapter = adapter


        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
        bottomNav.selectedItemId = R.id.navigation_manual
        setupBottomNav(bottomNav)
    }
}
