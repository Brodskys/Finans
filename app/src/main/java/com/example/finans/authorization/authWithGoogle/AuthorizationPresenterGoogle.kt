package com.example.finans.authorization.authWithGoogle

import android.app.Activity
import android.content.Intent
import android.util.Log
import com.example.finans.authorization.AuthorizationActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException

class AuthorizationPresenterGoogle(private val activity: Activity, private val view: AuthorizationActivity, private val model: AuthorizationModelGoogle) {

    fun signInWithGoogle() {
        model.signIn(activity)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                Log.d(TAG, "firebaseAuthWithGoogle:" + account.id)
                model.firebaseAuthWithGoogle(account.idToken!!) { isSuccess ->
                    if (isSuccess) {
                        view.showMainScreen()
                    } else {
                        view.showErrorScreen()
                    }
                }
            } catch (e: ApiException) {
                Log.w(TAG, "Google sign in failed", e)
            }
        }
    }

    companion object {
        const val TAG = "GoogleLogin"
        const val RC_SIGN_IN = 9001
    }
}