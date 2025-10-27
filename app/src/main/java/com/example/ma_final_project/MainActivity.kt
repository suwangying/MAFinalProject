package com.example.ma_final_project

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.widget.EditText
import android.widget.Toast
import com.example.ma_final_project.utils.ValidationUtils


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        val loggedInUser = prefs.getString("USER_PHONE", null)

        if (loggedInUser != null) {
            // User already logged in → go to HomeActivity
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnSignup = findViewById<Button>(R.id.btnSignup)
        val forgotPasswd = findViewById<TextView>(R.id.tvForgotPassword)

        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etPassword = findViewById<EditText>(R.id.etPassword)

        etPhone.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val phone = etPhone.text.toString().trim()
                if (!ValidationUtils.isValidPhone(phone)) {
                    etPhone.error = "Phone must be 10 digits"
                } else {
                    etPhone.error = null
                }
            }
        }

        etPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = etPassword.text.toString()
                if (!ValidationUtils.isValidPassword(password)) {
                    etPassword.error = "Password must have 6+ chars with letters & numbers"
                } else {
                    etPassword.error = null
                }
            }
        }

        // Login Button -> go to Home Page
        btnLogin.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (phone.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter phone number and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidPhone(phone)) {
                Toast.makeText(this, "Phone number must be exactly 10 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidPassword(password)) {
                Toast.makeText(
                    this,
                    "Password must be at least 6 characters and contain letters and numbers",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            // todo: sqlite authentication here
            val dbHelper = DatabaseHelper(this)
            val cursor = dbHelper.getUser(phone)
            if (cursor.moveToFirst()) {
                val storedPassword =
                    cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD))

                if (storedPassword == password) {
                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()

                    // Save user session
                    val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
                    prefs.edit().putString("USER_PHONE", phone).apply()

                    val intent = Intent(this, HomeActivity::class.java)
                    startActivity(intent)
                    //finish()
                } else {
                    Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                }
            }
            else {
                Toast.makeText(this, "User not found. Please sign up first.", Toast.LENGTH_SHORT).show()
            }
        }


        // SignUp Button -> go to SignUp Page
        btnSignup.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        // Forgot Password Link -> go to Forgot Password Page
        forgotPasswd.setOnClickListener {
           val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }
    }
}