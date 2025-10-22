package com.example.ma_final_project

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import com.example.ma_final_project.utils.ValidationUtils

class SignUpActivity : AppCompatActivity() {

    private lateinit var etFirstName: EditText
    private lateinit var etLastName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnSignUp: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        // Initialize views
        etFirstName = findViewById(R.id.etFirstName)
        etLastName = findViewById(R.id.etLastName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnSignUp = findViewById(R.id.btnSignUp)


        etFirstName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val firstName = etFirstName.text.toString().trim()
                etFirstName.error = when {
                    firstName.isEmpty() -> "First name required"
                    !ValidationUtils.isValidName(firstName) -> "Must start with capital letter and contain only letters"
                    else -> null
                }
            }
        }

        etLastName.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val lastName = etLastName.text.toString().trim()
                etLastName.error = when {
                    lastName.isEmpty() -> "Last name required"
                    !ValidationUtils.isValidName(lastName) -> "Must start with capital letter, should be more than one character and contain only letters"
                    else -> null
                }
            }
        }

        etEmail.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val email = etEmail.text.toString().trim()
                if (email.isNotEmpty() && !ValidationUtils.isValidEmail(email)) {
                    etEmail.error = "Invalid email address"
                } else {
                    etEmail.error = null
                }
            }
        }

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

        etConfirmPassword.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val password = etPassword.text.toString()
                val confirm = etConfirmPassword.text.toString()
                if (confirm != password) {
                    etConfirmPassword.error = "Passwords do not match"
                } else {
                    etConfirmPassword.error = null
                }
            }
        }

        btnSignUp.setOnClickListener {
            val firstName = etFirstName.text.toString().trim()
            val lastName = etLastName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()

            if (firstName.isEmpty() || lastName.isEmpty() || phone.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidName(firstName) || !ValidationUtils.isValidName(lastName)) {
                Toast.makeText(this, "Names must start with a capital letter and contain only letters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidPhone(phone)) {
                Toast.makeText(this, "Phone must be 10 digits", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!ValidationUtils.isValidPassword(password)) {
                Toast.makeText(this, "Password must have 6+ chars with letters & numbers", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isNotEmpty() && !ValidationUtils.isValidEmail(email)) {
                Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // TODO: Save user to SQLite or Firebase

            Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
