package com.example.gomimi.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.gomimi.R
import com.example.gomimi.dataClass.Language
import com.example.gomimi.databinding.ActivityLoginBinding
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.retrofit.UserRepository
import com.example.gomimi.retrofit.TokenManager
import com.example.gomimi.utils.LocaleHelper
import com.example.gomimi.viewModel.UserViewModel
import kotlinx.coroutines.launch

class LoginActivity: BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel
    private val languageList = mutableListOf<Language>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        fetchLanguages()

        val languageButton = findViewById<ImageView>(R.id.languageButton)
        languageButton.setOnClickListener {
            if (languageList.isEmpty()) {
                Toast.makeText(this, getString(R.string.language_loading), Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val popup = PopupMenu(this, languageButton)
            languageList.forEachIndexed { index, lang ->
                popup.menu.add(0, index, index, lang.name)
            }

            popup.setOnMenuItemClickListener { item ->
                val selectedLang = languageList[item.itemId]

                LocaleHelper.setAppLocale(this, selectedLang.code)

                val prefs = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
                prefs.edit().putString("lang_code", selectedLang.code).apply()

                recreate()
                true
            }

            popup.show()
        }

        binding.signUpButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim()
            val password = binding.passwordEditText.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                userViewModel.performLogin(email, password)
            } else {
                Toast.makeText(this, getString(R.string.enter), Toast.LENGTH_SHORT).show()
            }
        }

        userViewModel.loginResult.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false
                    binding.emailEditText.isEnabled = false
                    binding.passwordEditText.isEnabled = false
                }
                is NetworkResult.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    binding.emailEditText.isEnabled = true
                    binding.passwordEditText.isEnabled = true

                    val token = result.data.accessToken
                    Toast.makeText(this, getString(R.string.login_successful), Toast.LENGTH_LONG).show()
                    Log.d("LoginActivity", "AccessToken: $token")
                    TokenManager.saveToken(token)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                is NetworkResult.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    binding.emailEditText.isEnabled = true
                    binding.passwordEditText.isEnabled = true
                    Toast.makeText(this, getString(R.string.incorrect), Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun fetchLanguages() {
        val userRepository = UserRepository()
        lifecycleScope.launch {
            when (val result = userRepository.getLanguages()) {
                is NetworkResult.Success -> {
                    languageList.clear()
                    languageList.addAll(result.data)
                }
                is NetworkResult.Error -> {
                    Toast.makeText(this@LoginActivity, getString(R.string.language_setting_error), Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }
}
