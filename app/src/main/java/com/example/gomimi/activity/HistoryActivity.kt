package com.example.gomimi.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gomimi.R
import com.example.gomimi.adapter.HistoryAdapter
import com.example.gomimi.databinding.ActivityHistoryBinding
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.viewModel.HistoryViewModel

class HistoryActivity : BaseActivity() {

    private lateinit var binding: ActivityHistoryBinding
    private lateinit var viewModel: HistoryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ViewModelを初期化
        viewModel = ViewModelProvider(this).get(HistoryViewModel::class.java)

        // AdapterとRecyclerViewを初期化
        setupRecyclerView()

        // LiveDataの監視を開始
        observeHistories()

        // データを取得開始
        viewModel.fetchHistories()

        // Bottom Navigationの設定
        setupBottomNav(binding.bottomMenu)
        binding.bottomMenu.selectedItemId = R.id.navigation_history
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(emptyList()) { historyItem ->
            val intent = Intent(this, HistoryDetailActivity::class.java).apply {
                putExtra(HistoryDetailActivity.EXTRA_GARBAGE_NAME, historyItem.name)
            }
            startActivity(intent)
        }
        binding.recyclerView.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(this@HistoryActivity)
        }
    }

    private fun observeHistories() {
        viewModel.histories.observe(this) { result ->
            // ローディング表示を制御
            binding.progressBar.visibility = if (result is NetworkResult.Loading) View.VISIBLE else View.GONE
            when (result) {
                is NetworkResult.Success -> {
                    // 成功したらAdapterのデータを更新
                    historyAdapter.updateData(result.data)
                }
                is NetworkResult.Error -> {
                    // エラーメッセージを表示
                    Toast.makeText(this, "履歴の取得に失敗: ${result.message}", Toast.LENGTH_LONG).show()
                }
                else -> {} // Loading
            }
        }
    }
}
