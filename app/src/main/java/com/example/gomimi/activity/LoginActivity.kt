package com.example.gomimi.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.gomimi.databinding.ActivityLoginBinding
import com.example.gomimi.retrofit.NetworkResult
import com.example.gomimi.retrofit.TokenManager
import com.example.gomimi.viewModel.UserViewModel


class LoginActivity: BaseActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel // APIの例

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater) // bindingの初期化
        setContentView(binding.root)

        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        // アカウント登録ボタンのクリックリスナー
        binding.signUpButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // ログインボタンのクリックリスナー
        binding.loginButton.setOnClickListener {
            val email = binding.emailEditText.text.toString().trim() // 入力値から空白を除去
            val password = binding.passwordEditText.text.toString().trim() // 入力値から空白を除去

            if (email.isNotEmpty() && password.isNotEmpty()) {
                userViewModel.performLogin(email, password)
            } else {
                Toast.makeText(this, "メールアドレスとパスワードを入力してください。", Toast.LENGTH_SHORT).show()
            }
        }

        // ログイン結果の監視  本番
        userViewModel.loginResult.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // ローディング表示
                    binding.progressBar.visibility = View.VISIBLE
                    binding.loginButton.isEnabled = false // ボタンを無効化
                    binding.emailEditText.isEnabled = false // 入力欄を無効化
                    binding.passwordEditText.isEnabled = false // 入力欄を無効化
                }
                is NetworkResult.Success -> {
                    // ログイン成功時の処理
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    binding.emailEditText.isEnabled = true
                    binding.passwordEditText.isEnabled = true

                    val token = result.data.accessToken
                    Toast.makeText(this, "ログイン成功！", Toast.LENGTH_LONG).show()
                    Log.d("LoginActivity", "AccessToken: $token")
                    TokenManager.saveToken(token)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish() // LoginActivityを閉じる
                }
                is NetworkResult.Error -> {
                    // ログイン失敗時の処理
                    binding.progressBar.visibility = View.GONE
                    binding.loginButton.isEnabled = true
                    binding.emailEditText.isEnabled = true
                    binding.passwordEditText.isEnabled = true
//                    Toast.makeText(this, "エラー: ${result.message}", Toast.LENGTH_LONG).show()
                    Toast.makeText(this, "メールアドレスかパスワードが間違っています。", Toast.LENGTH_LONG).show()
                }
            }
        }

    }
}