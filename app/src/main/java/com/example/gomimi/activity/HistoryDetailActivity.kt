package com.example.gomimi.activity

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.gomimi.databinding.ActivityHistoryDetailBinding
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.viewModel.HistoryDetailViewModel

// ▼▼▼ クラス名を変更 ▼▼▼
class HistoryDetailActivity : AppCompatActivity() {

    // ★ Bindingクラスの型を変更
    private lateinit var binding: ActivityHistoryDetailBinding
    // ★ ViewModelの型を変更
    private lateinit var viewModel: HistoryDetailViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ★ Bindingクラスのインフレートを変更
        binding = ActivityHistoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ★ ViewModelの取得を変更
        viewModel = ViewModelProvider(this).get(HistoryDetailViewModel::class.java)

        val garbageName = intent.getStringExtra(EXTRA_GARBAGE_NAME)

        if (garbageName == null) {
            Toast.makeText(this, "情報の取得に失敗しました", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel.fetchManualDetail(garbageName)
        observeManualDetail()

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.bottomMenu.selectedItemId = View.NO_ID
    }

    private fun observeManualDetail() {
        viewModel.manualDetail.observe(this) { result ->
            if (result is NetworkResult.Success) {
                val manual = result.data
                binding.textView.text = manual.name
                binding.textView2.text = "カテゴリ: ${manual.category}\n\n${manual.remarks ?: "詳細情報はありません。"}"
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, "詳細情報の取得エラー: ${result.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        const val EXTRA_GARBAGE_NAME = "EXTRA_GARBAGE_NAME"
    }
}