package com.example.ma_final_project.utils

object ValidationUtils {

    fun isValidName(name: String): Boolean {
        // Each part: Capital + >=1 lowercase. Parts separated by space or hyphen.
        val regex = """^[\p{Lu}][\p{Ll}]{1,}(?:[ -][\p{Lu}][\p{Ll}]{1,})*$""".toRegex()
        return regex.matches(name.trim())
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
