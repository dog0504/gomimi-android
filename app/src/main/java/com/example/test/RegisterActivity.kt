package com.example.yourapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.test.databinding.ActivityRegisterBinding
import com.example.test.retrofit.NetworkResult
import com.example.test.retrofit.UserViewModel

class RegisterActivity: BaseActivity() {
    private lateinit var viewBinding:  ActivityRegisterBinding
    private lateinit var userViewModel: UserViewModel // テスト ViewModelを追加

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

//        val register_btn = findViewById<Button>(R.id.register_btn)
//        register_btn.setOnClickListener {
//            startActivity(Intent(this, MainActivity::class.java))
//        }
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // --- 言語スピナーの実装 ---
        val languages = listOf("日本語", "English")
        val languageAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item, // 標準のスピナーアイテムレイアウト
            languages
        )
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) // ドロップダウンレイアウト
        viewBinding.languageSpinner.adapter = languageAdapter

        // --- 住所のスピナー（都道府県、市区町村）にダミーデータを実装 ---
        // 都道府県スピナー
        val prefectures = listOf("選択してください", "東京都", "大阪府", "愛知県", "福岡県", "その他")
        val prefectureAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            prefectures
        )
        prefectureAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.prefectureSpinner.adapter = prefectureAdapter

        // 市区町村スピナー
        val cities = listOf("選択してください", "中央区", "港区", "新宿区", "渋谷区", "その他地域")
        val cityAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            cities
        )
        cityAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        viewBinding.citySpinner.adapter = cityAdapter

        // --- 登録ボタンのクリックリスナー ---
        viewBinding.registerBtn.setOnClickListener {
            Log.d("RegisterActivity", "Register button clicked")
            val email = viewBinding.emailInput.text.toString().trim()
            val password = viewBinding.passwordInput.text.toString().trim()
            val confirmPassword = viewBinding.confirmPasswordInput.text.toString().trim()
            val selectedLanguage = viewBinding.languageSpinner.selectedItem.toString()
            val zipCode = viewBinding.zipCodeInput.text.toString().trim()
            val selectedPrefecture = viewBinding.prefectureSpinner.selectedItem.toString()
            val selectedCity = viewBinding.citySpinner.selectedItem.toString()

            // 入力値の基本的なバリデーション
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
                selectedLanguage == "選択してください" || selectedPrefecture == "選択してください" || selectedCity == "選択してください" ||
                zipCode.isEmpty()) {
                Toast.makeText(this, "全ての項目を入力してください。", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "パスワードが一致しません。", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ViewModelを介して登録処理を実行
            userViewModel.register(email, password, selectedLanguage, zipCode, selectedPrefecture, selectedCity)
        }

        // --- 登録結果の監視 ---
        userViewModel.loginResult.observe(this) { result -> // UserViewModelのloginResultを登録結果にも再利用
            when (result) {
                is NetworkResult.Loading -> {
                    viewBinding.progressBar.visibility = View.VISIBLE
                    viewBinding.registerBtn.isEnabled = false
                    // 他の入力欄も無効化する
                }
                is NetworkResult.Success -> {
                    viewBinding.progressBar.visibility = View.GONE
                    viewBinding.registerBtn.isEnabled = true
                    // 他の入力欄も有効化する
                    Toast.makeText(this, "登録成功！ようこそ ${result.data.name}！", Toast.LENGTH_LONG).show()
                    // 登録成功後の画面遷移（例: メイン画面へ）
                    startActivity(Intent(this, MainActivity::class.java))
                    finish() // 登録画面を閉じる
                }
                is NetworkResult.Error -> {
                    viewBinding.progressBar.visibility = View.GONE
                    viewBinding.registerBtn.isEnabled = true
                    // 他の入力欄も有効化する
                    Toast.makeText(this, "登録エラー: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }


}