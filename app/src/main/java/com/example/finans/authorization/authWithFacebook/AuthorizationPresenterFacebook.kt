package com.example.finans.authorization.authWithFacebook

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.finans.authorization.AuthorizationActivity
import com.example.finans.authorization.AuthorizationView
import com.example.finans.authorization.authWithGoogle.AuthorizationModelGoogle
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AuthorizationPresenterFacebook(private val view: AuthorizationActivity, private val model: AuthorizationModelFacebook) {

    fun signInWithFacebook() {
        model.signIn(view, view.callbackManager)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        view.callbackManager.onActivityResult(requestCode, resultCode, data)
    }

    companion object {
        const val TAG = "FacebookLogin"
    }
}