package com.example.gomimi.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.ViewModelProvider
import com.example.gomimi.R
import com.example.gomimi.databinding.SettingsMenuBinding
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.viewModel.SettingsViewModel

class SettingsActivity: BaseActivity() {

    private lateinit var binding: SettingsMenuBinding // ★ View Bindingプロパティ
    private lateinit var viewModel: SettingsViewModel

    private val settingsLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // 子画面から RESULT_OK が返ってきた場合（＝変更が適用された場合）
        if (result.resultCode == Activity.RESULT_OK) {
            // ユーザー情報を再取得してUIを更新する
            viewModel.fetchUserProfile()
            Toast.makeText(this, "@string/settingupdete", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = SettingsMenuBinding.inflate(layoutInflater) // ★ View Bindingでレイアウトをセット
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(SettingsViewModel::class.java)

        // Bottom Navの設定
        setupBottomNav(binding.bottomMenu)
        binding.bottomMenu.selectedItemId = R.id.navigation_settings

        // 各画面への遷移リスナーを設定
        setupNavigation()

        // ユーザー情報の監視を開始
        observeUserProfile()

        // ユーザー情報を取得
        viewModel.fetchUserProfile()
    }

    private fun observeUserProfile() {
        viewModel.userProfile.observe(this) { result ->
            if (result is NetworkResult.Success) {
                // ★ 取得したデータをTextViewに設定
                binding.emailTextView.text = result.data.email
                binding.languageTextView.text = result.data.language.name
                binding.addressTextView.text = result.data.address?.let {
                    "${it.zip}\n${it.city}${it.ward}${it.town ?: ""}${it.chom ?: ""}${it.street ?: ""}${it.inf ?: ""}"
                } ?: "未設定"
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, "@string/user_not_found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupNavigation() {
        // 通知
        binding.buttonImage1.setOnClickListener {
            startActivity(Intent(this, NotifySettingsActivity::class.java))
        }

        // 言語
        binding.buttonImage2.setOnClickListener {
            val intent = Intent(this, LanguageSettingsActivity::class.java)
            settingsLauncher.launch(intent) // startActivityではなく、ランチャーで起動
        }

        // 所在地
        binding.languageBtn.setOnClickListener {
            val intent = Intent(this, LocationSettingsActivity::class.java)
            settingsLauncher.launch(intent) // startActivityではなく、ランチャーで起動
        }

        // ログアウト
        binding.buttonText4.setOnClickListener {
            // TODO: ログアウト処理（トークン削除など）を追加
            startActivity(Intent(this, LoginActivity::class.java))
            finishAffinity() // 全てのアクティビティを終了
        }
    }
}
