package com.example.gomimi.activity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.gomimi.R
import com.example.gomimi.dataClass.Language
import com.example.gomimi.databinding.LanguageSettingsBinding
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.utils.LocaleHelper
import com.example.gomimi.viewModel.LanguageSettingsViewModel

class LanguageSettingsActivity: BaseActivity() {
    private lateinit var binding: LanguageSettingsBinding
    private lateinit var viewModel: LanguageSettingsViewModel

    private var allLanguages: List<Language> = emptyList()
    private var selectedLanguageId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = LanguageSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this).get(LanguageSettingsViewModel::class.java)

        setupUI()
        observeViewModel()

        // 初期データを取得
        viewModel.fetchInitialData()
    }

    private fun setupUI() {
        // 戻るボタン
        binding.backButton.setOnClickListener {
            finish()
        }

        // 適用ボタン
        binding.applyBtn.setOnClickListener {
            selectedLanguageId?.let { langId ->
                val selectedLang = allLanguages.find { it.id == langId }
                if (selectedLang != null) {
                    LocaleHelper.setAppLocale(this, selectedLang.code)

                    val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                    prefs.edit().putString("lang_code", selectedLang.code).apply()

                    recreate()

                    viewModel.updateLanguage(langId)
                }
            } ?: Toast.makeText(this, getString(R.string.language_setting_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        // 初期データ（全言語とユーザー情報）の監視
        viewModel.languages.observe(this) { setupSpinnerWithOptions() }
        viewModel.userProfile.observe(this) { setupSpinnerWithOptions() }

        // 更新結果の監視
        viewModel.updateResult.observe(this) { result ->
            binding.progressBar.visibility = if (result is NetworkResult.Loading) View.VISIBLE else View.GONE

            if (result is NetworkResult.Success) {
//                Toast.makeText(this, "言語設定を更新しました", Toast.LENGTH_SHORT).show()
                setResult(Activity.RESULT_OK)
                finish() // 更新成功したら画面を閉じる
            } else if (result is NetworkResult.Error) {
                Toast.makeText(this, "${getString(R.string.update_failed)} ${result.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // 全言語とユーザー情報が揃ったらスピナーをセットアップする
    private fun setupSpinnerWithOptions() {
        val languagesResult = viewModel.languages.value
        val profileResult = viewModel.userProfile.value

        if (languagesResult is NetworkResult.Success && profileResult is NetworkResult.Success) {
            allLanguages = languagesResult.data
            val currentUserLanguage = profileResult.data.language

            val languageNames = allLanguages.map { it.name }
            val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageNames)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.languageSpinner.adapter = adapter

            // 現在のユーザーの言語を初期選択状態にする
            val currentPosition = allLanguages.indexOfFirst { it.id == currentUserLanguage.id }
            if (currentPosition != -1) {
                binding.languageSpinner.setSelection(currentPosition)
            }

            // スピナーの選択を監視
            binding.languageSpinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                    selectedLanguageId = allLanguages[position].id
                }
                override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
            }
        }
    }
}
