package com.example.finans.other

import android.content.Context
import com.example.finans.R

fun String.isEmailValid(context: Context): String? {
    if (!android.util.Patterns.EMAIL_ADDRESS.matcher(this).matches()) {
        return context.getString(R.string.incorrectEmail)
    }
    return null
}

fun String.isValidPassword(context: Context): String? {
    if (this.length < 6) return context.getString(R.string.incorrectPasswordLength)
    if (this.firstOrNull { it.isDigit() } == null) return context.getString(R.string.incorrectPasswordDigit)
    if (this.filter { it.isLetter() }.firstOrNull { it.isUpperCase() } == null) return context.getString(
        R.string.incorrectPasswordCapitalLetter
    )
    if (this.filter { it.isLetter() }.firstOrNull { it.isLowerCase() } == null) return context.getString(
        R.string.incorrectPasswordLowercaseLetter
    )

    return null
}

fun String.isConfirmPassword(context: Context): String? {
    return context.getString(R.string.passwordsNotMatch)
}