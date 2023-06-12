package com.example.finans.authorization

import com.google.android.gms.auth.api.signin.GoogleSignInAccount

interface AuthorizationView {
    fun showMainScreen(account: GoogleSignInAccount?)
    fun showErrorScreen()
}