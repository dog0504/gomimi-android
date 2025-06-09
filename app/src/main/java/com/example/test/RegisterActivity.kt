package com.example.yourapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import com.example.test.R


class RegisterActivity: BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val register_btn = findViewById<Button>(R.id.register_btn)
        register_btn.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
        }

    }


}