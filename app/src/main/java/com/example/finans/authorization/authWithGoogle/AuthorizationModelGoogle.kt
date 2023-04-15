package com.example.finans.authorization.authWithGoogle

import android.app.Activity
import com.example.finans.authorization.authWithGoogle.AuthorizationPresenterGoogle.Companion.RC_SIGN_IN
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class AuthorizationModelGoogle(private val auth: FirebaseAuth, private val googleSignInClient: GoogleSignInClient) {

    fun firebaseAuthWithGoogle(idToken: String, onCompleteListener: (Boolean) -> Unit) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onCompleteListener(true)
                } else {
                    onCompleteListener(false)
                }
            }
    }

    fun signIn(activity: Activity) {
        val signInIntent = googleSignInClient.signInIntent
        activity.startActivityForResult(signInIntent, RC_SIGN_IN)
    }
}