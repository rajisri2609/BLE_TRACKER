package com.example.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button

    // Hardcoded credentials
    private val correctUsername = "admin"
    private val correctPassword = "admin"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        etUsername = findViewById(R.id.etUsername)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)

        btnLogin.setOnClickListener {
            login()
        }
    }

    private fun login() {
        val username = etUsername.text.toString()
        val password = etPassword.text.toString()

        if (username == correctUsername && password == correctPassword) {
            // Correct credentials, navigate to DashboardActivity
            val intent = Intent(this, DashboardActivityMain::class.java)
            startActivity(intent)
            finish() // Optional: Close the MainActivity
        } else {
            // Incorrect credentials, show error message
            Toast.makeText(this, getString(R.string.login_error_message), Toast.LENGTH_SHORT).show()
        }
    }
}
