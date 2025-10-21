package com.example.ma_final_project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.EditText
import android.widget.Toast


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val forgotPasswd = findViewById<TextView>(R.id.tvForgotPassword)

        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        // Login Button -> go to Home Page
        btnLogin.setOnClickListener {

            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()
            if (phone.isNotEmpty() && password.isNotEmpty()) {
                // TODO: You can also add actual authentication here (SQLite/Firebase)
                val intent = Intent(this, HomeActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please enter phone number and password", Toast.LENGTH_SHORT).show()
            }
        }

        // SignUp Button -> go to SignUp Page
        btnSignup.setOnClickListener {
            /*val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)*/
        }

        // Forgot Password Link -> go to Forgot Password Page
        forgotPasswd.setOnClickListener {
            /*val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)*/
        }
    }
}