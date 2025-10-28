package com.example.ma_final_project.utils

object ValidationUtils {

    fun isValidName(name: String): Boolean {
        // Regex: starts with capital letter, followed by lowercase letters only, at least 1 char
        val regex = "^[A-Z][a-z]{1,}$".toRegex()
        return regex.matches(name)
    }

    // Validate phone number: exactly 10 digits
    fun isValidPhone(phone: String): Boolean {
        val phoneRegex = Regex("^[0-9]{10}$")
        return phone.matches(phoneRegex)
    }

    // Validate password: at least 6 chars, letters and numbers
    fun isValidPassword(password: String): Boolean {
        val passwordRegex = Regex("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{6,}$")
        return password.matches(passwordRegex)
    }

    // Validate email
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

}
