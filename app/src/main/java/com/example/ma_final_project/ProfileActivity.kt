package com.example.ma_final_project

import android.content.Intent
import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.ma_final_project.utils.ValidationUtils

class ProfileActivity : AppCompatActivity() {

    private lateinit var dbHelper: DatabaseHelper
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var etEmail: EditText
    private lateinit var tvEditName: TextView
    private lateinit var tvEditPhone: TextView
    private lateinit var tvEditEmail: TextView
    private lateinit var btnChangePassword: Button
    private lateinit var btnDeleteAccount: Button
    private var currentPhone: String? = null  // Logged-in user

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        dbHelper = DatabaseHelper(this)
        etName = findViewById(R.id.etName)
        etPhone = findViewById(R.id.etPhone)
        etEmail = findViewById(R.id.etEmail)
        tvEditName = findViewById(R.id.tvEditName)
        tvEditPhone = findViewById(R.id.tvEditPhone)
        tvEditEmail = findViewById(R.id.tvEditEmail)
        btnChangePassword = findViewById(R.id.btnChangePassword)
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount)
        val btnLogout = findViewById<Button>(R.id.btnLogout)

        val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
        currentPhone = prefs.getString("USER_PHONE", null)

        if (currentPhone == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            loadUserInfo()
        }

        tvEditName.setOnClickListener { toggleEdit(etName, "Name", tvEditName) }
        tvEditPhone.setOnClickListener { toggleEdit(etPhone, "Phone", tvEditPhone) }
        tvEditEmail.setOnClickListener { toggleEdit(etEmail, "Email", tvEditEmail) }


        btnChangePassword.setOnClickListener { showChangePasswordDialog() }

        btnDeleteAccount.setOnClickListener { confirmDeleteAccount() }

        btnLogout.setOnClickListener {
            val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
            prefs.edit().clear().apply() // Clear user session

            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun loadUserInfo() {
        val cursor = dbHelper.getUser(currentPhone!!)
        if (cursor.moveToFirst()) {
            val firstname = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FIRSTNAME))
            val lastname = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_LASTNAME))
            etName.setText("$firstname $lastname")
            etPhone.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PHONE)))
            etEmail.setText(cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL)))
        }
        cursor.close()
    }


    private fun toggleEdit(editText: EditText, field: String, editLabel: TextView) {
        if (editText.isEnabled) {
            // Save new value
            val newValue = editText.text.toString().trim()
            if (field != "Email" && newValue.isEmpty()) {
                Toast.makeText(this, "$field cannot be empty", Toast.LENGTH_SHORT).show()
                return
            }

            // Fetch current user info from DB
            val cursor = dbHelper.getUser(currentPhone!!)
            if (!cursor.moveToFirst()) {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                cursor.close()
                return
            }

            var firstname = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FIRSTNAME))
            var lastname = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_LASTNAME))
            var phone = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PHONE))
            var email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL))
            var password = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_PASSWORD))
            cursor.close()

            val success = when (field) {
                "Name" -> {
                    val parts = newValue.split(" ")
                    if (parts.size < 2) {
                        Toast.makeText(this, "Please enter both first and last name", Toast.LENGTH_SHORT).show()
                        false
                    } else if (!ValidationUtils.isValidName(newValue)) {
                        Toast.makeText(this, "Invalid name format", Toast.LENGTH_SHORT).show()
                        false
                    } else {
                        firstname = parts[0]
                        lastname = parts.subList(1, parts.size).joinToString(" ")
                        dbHelper.updateUserExceptPhone(firstname, lastname, email, password, phone) > 0
                    }
                }
                "Phone" -> {
                    if (!ValidationUtils.isValidPhone(newValue)) {
                        Toast.makeText(this, "Phone must be 10 digits", Toast.LENGTH_SHORT).show()
                        false
                    } else {
                        val updated = dbHelper.updatePhone(phone, newValue) > 0
                        if (updated) {
                            // Update SharedPreferences
                            currentPhone = newValue
                            val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
                            prefs.edit().putString("USER_PHONE", newValue).apply()
                        }
                        updated
                    }
                }
                "Email" -> {
                    if (newValue.isNotEmpty() && !ValidationUtils.isValidEmail(newValue)) {
                        Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
                        false
                    } else {
                        email = newValue
                        dbHelper.updateUserExceptPhone(firstname, lastname, email, password, phone) > 0
                    }
                }
                else -> false
            }

            if (success) Toast.makeText(this, "$field updated", Toast.LENGTH_SHORT).show()
            editText.isEnabled = false
            editLabel.text = "Edit"
        } else {
            editText.isEnabled = true
            editText.requestFocus()
            editLabel.text = "Save"
        }
    }


    private fun showChangePasswordDialog() {
        val input = EditText(this)
        input.hint = "Enter new password"
        input.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(input)
            .setPositiveButton("Update") { _, _ ->
                val newPass = input.text.toString().trim()
                if (newPass.length < 6) {
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                } else {
                    val cursor = dbHelper.getUser(currentPhone!!)
                    if (cursor.moveToFirst()) {
                        val firstname = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_FIRSTNAME))
                        val lastname = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_LASTNAME))
                        val email = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseHelper.COL_USER_EMAIL))
                        dbHelper.updateUserExceptPhone(firstname, lastname, email, newPass, currentPhone!!)
                        Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                    }
                    cursor.close()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun confirmDeleteAccount() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete your account? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val deleted = dbHelper.deleteUser(currentPhone!!)
                if (deleted > 0) {
                    val prefs = getSharedPreferences("UserSession", MODE_PRIVATE)
                    prefs.edit().clear().apply() // Clear user session
                    Toast.makeText(this, "Account deleted", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(this, "Failed to delete account", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

}