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
        val dbHelper = DatabaseHelper(this)

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

            // Check if user exists
            val cursor = dbHelper.getUser(phone)
            if (!cursor.moveToFirst()) {
                Toast.makeText(this, "No user found with this phone number", Toast.LENGTH_SHORT).show()
                cursor.close()
                return@setOnClickListener
            }

            // Get existing firstName, lastName, email to keep them unchanged
            val firstName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FIRSTNAME))
            val lastName = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_LASTNAME))
            val email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL))
            cursor.close()

            // Update user password
            val updated = dbHelper.updateUserExceptPhone(firstName, lastName, email, newPassword, phone)
            if (updated > 0) {
                Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show()
                finish() // Go back to Login
            } else {
                Toast.makeText(this, "Failed to reset password", Toast.LENGTH_SHORT).show()
            }

        }




    }
}