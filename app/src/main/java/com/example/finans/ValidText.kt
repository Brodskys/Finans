package com.example.finans

fun String.isEmailValid(): String? {

    if(!android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()) return "Некоректная почта"

    return null
}

fun String.isValidPassword(): String? {
    if (this.length < 6) return "Пароль меньше 6 символов"
    if (this.firstOrNull { it.isDigit() } == null) return "Пароль должен содержать хотя бы одну цифру"
    if (this.filter { it.isLetter() }.firstOrNull { it.isUpperCase() } == null) return "Пароль должен содержать хотя бы одну заглавную букву"
    if (this.filter { it.isLetter() }.firstOrNull { it.isLowerCase() } == null) return "Пароль должен содержать хотя бы одну строчную букву"

    return null
}