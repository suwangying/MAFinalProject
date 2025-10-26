package com.example.ma_final_project

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.*
import com.example.ma_final_project.utils.ValidationUtils

class ForgotPasswordActivity : AppCompatActivity() {
    private lateinit var etPhone: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnReset: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_forgot_password)

        etPhone = findViewById(R.id.etPhone)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnReset = findViewById(R.id.btnReset)

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

        etNewPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = etNewPassword.text.toString()
                if (!ValidationUtils.isValidPassword(password)) {
                    etNewPassword.error = "Password must have 6+ chars with letters & numbers"
                } else {
                    etNewPassword.error = null
                }
            }
        }

        etConfirmPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = etNewPassword.text.toString()
                val confirm = etConfirmPassword.text.toString()
                if (confirm != password) {
                    etConfirmPassword.error = "Passwords do not match"
                } else {
                    etConfirmPassword.error = null
                }
            }
        }

        btnReset.setOnClickListener {
            val phone = etPhone.text.toString().trim()
            val newPassword = etNewPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (phone.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // todo: Add SQLite Authentication to check phone number

            Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show()
            finish() // Go back to Login

        }




    }
}