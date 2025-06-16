package com.example.yourapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.test.R
import com.example.test.databinding.ActivityLoginBinding
import com.example.test.retrofit.NetworkResult
import com.example.test.retrofit.UserViewModel


class LoginActivity: BaseActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var userViewModel: UserViewModel // APIの例

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val sign_button = findViewById<View>(R.id.sign_in_button)
        sign_button.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // APIの例
        userViewModel = ViewModelProvider(this).get(UserViewModel::class.java)

        val userId = intent.getStringExtra("USER_ID") ?: return // インテントからIDを取得

        userViewModel.fetchUser(userId)

        userViewModel.user.observe(this) { result ->
            when (result) {
                is NetworkResult.Loading -> {
                    // ローディング表示
//                    binding.progressBar.visibility = android.view.View.VISIBLE
//                    binding.userDetailLayout.visibility = android.view.View.GONE
                }
                is NetworkResult.Success -> {
                    // 成功時の処理
//                    binding.progressBar.visibility = android.view.View.GONE
//                    binding.userDetailLayout.visibility = android.view.View.VISIBLE
//                    binding.userNameTextView.text = result.data.name
//                    binding.userEmailTextView.text = result.data.email
                }
                is NetworkResult.Error -> {
                    // エラー時の処理
//                    binding.progressBar.visibility = android.view.View.GONE
//                    binding.userDetailLayout.visibility = android.view.View.GONE
//                    Toast.makeText(this, "Error: ${result.message}", Toast.LENGTH_LONG).show()
                }
            }
        }

    }




}