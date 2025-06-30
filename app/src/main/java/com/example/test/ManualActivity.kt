package com.example.yourapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.test.HistoryDetailActivity
import com.example.test.ManualAdapter
import com.example.test.R
import com.example.test.databinding.ActivityManualBinding
import com.example.test.retrofit.NetworkResult
import com.example.test.viewModel.ManualViewModel

class ManualActivity : BaseActivity() {

//    private lateinit var adapter: ManualAdapter
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var searchEditText: EditText

    private lateinit var binding: ActivityManualBinding
    private lateinit var viewModel: ManualViewModel
    private lateinit var manualAdapter: ManualAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityManualBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(ManualViewModel::class.java)

        setupUI()
        observeViewModel()

        // 画面起動時に全マニュアルを取得
        viewModel.fetchAllManuals()

//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_manual)
//
//        val manualData = listOf(
//            ManualItem("ペットボトル", "キャップとラベルを外して捨ててください。"),
//            ManualItem("缶", "中をすすいでから捨ててください。"),
//            ManualItem("ガラス瓶", "透明・茶・その他で分別してください。"),
//            ManualItem("段ボール", "折りたたんでひもで縛ってください。"),
//            ManualItem("電池", "回収ボックスへ入れてください。")
//        )
//
//        searchEditText = findViewById(R.id.search_edit_text)
//        recyclerView = findViewById(R.id.manual_recycler)
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        adapter = ManualAdapter(this, manualData)
//        recyclerView.adapter = adapter
//
//
//        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomMenu)
//        bottomNav.selectedItemId = R.id.navigation_manual
//        setupBottomNav(bottomNav)
    }

    private fun setupUI() {
        // BottomNav
        setupBottomNav(binding.bottomMenu)
        binding.bottomMenu.selectedItemId = R.id.navigation_manual

        // RecyclerViewとAdapter
        manualAdapter = ManualAdapter(emptyList()) { manualInfo ->
            // アイテムクリックで詳細画面へ
            val intent = Intent(this, HistoryDetailActivity::class.java).apply {
                putExtra(HistoryDetailActivity.EXTRA_GARBAGE_NAME, manualInfo.name)
            }
            startActivity(intent)
        }
        binding.manualRecycler.apply {
            // ★★★ この1行を追加 ★★★
            layoutManager = LinearLayoutManager(this@ManualActivity) // 縦スクロールのリスト形式に設定
            adapter = manualAdapter
        }

        // キーワード検索ボタン
//        binding.searchButton.setOnClickListener {
//            viewModel.searchByKeyword(binding.searchEditText.text.toString().trim())
//        }
        // キーボードの検索ボタンでも検索実行
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.searchByKeyword(binding.searchEditText.text.toString().trim())
                return@setOnEditorActionListener true
            }
            false
        }

        // 頭文字ボタンを動的に生成
//        val initials = listOf("あ", "か", "さ", "た", "な", "は", "ま", "や", "ら", "わ")
//        initials.forEach { initial ->
//            val button = Button(this).apply {
//                text = initial
//                setOnClickListener {
//                    viewModel.searchByInitial(initial)
//                }
//            }
//            binding.initialsContainer.addView(button)
//        }
    }

//    private fun performKeywordSearch() {
//        val keyword = binding.searchEditText.text.toString().trim()
//        viewModel.searchByKeyword(keyword)
//    }

    private fun observeViewModel() {
        viewModel.manuals.observe(this) { result ->
            binding.progressBar.visibility = if (result is NetworkResult.Loading) View.VISIBLE else View.GONE

            if (result is NetworkResult.Success) {
                manualAdapter.updateData(result.data)
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, "マニュアルの取得エラー: ${result.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
