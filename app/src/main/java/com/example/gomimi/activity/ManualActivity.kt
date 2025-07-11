package com.example.gomimi.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gomimi.R
import com.example.gomimi.adapter.ManualAdapter
import com.example.gomimi.databinding.ActivityManualBinding
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.viewModel.ManualViewModel

class ManualActivity : BaseActivity() {
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

        // キーボードの検索ボタンでも検索実行
        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                viewModel.searchByKeyword(binding.searchEditText.text.toString().trim())
                return@setOnEditorActionListener true
            }
            false
        }
    }

    private fun observeViewModel() {
        viewModel.manuals.observe(this) { result ->
            binding.progressBar.visibility = if (result is NetworkResult.Loading) View.VISIBLE else View.GONE

            if (result is NetworkResult.Success) {
                manualAdapter.updateData(result.data)
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, "@string/manual_not_found${result.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
