package com.example.yourapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import com.example.test.R


class LoginActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val sign_button = findViewById<View>(R.id.sign_in_button)
        sign_button.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }


}