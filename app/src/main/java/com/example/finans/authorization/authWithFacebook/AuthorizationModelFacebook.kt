package com.example.finans.authorization.authWithFacebook

import android.app.Activity
import android.util.Log
import com.example.finans.authorization.AuthorizationActivity
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class AuthorizationModelFacebook(private val auth: FirebaseAuth, private val activity: Activity) {

    private fun handleFacebookAccessToken(token: AccessToken, activity: AuthorizationActivity) {

        val credential = FacebookAuthProvider.getCredential(token.token)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(activity) { task ->
                if (task.isSuccessful) {
                    Log.d(AuthorizationPresenterFacebook.TAG, "signInWithCredential:success")



                    activity.showMainScreen()
                } else {
                    Log.w(
                        AuthorizationPresenterFacebook.TAG,
                        "signInWithCredential:failure",
                        task.exception
                    )
                    activity.showMainScreen()
                }
            }
    }

    fun signIn(activity: AuthorizationActivity, callbackManager: CallbackManager) {
        LoginManager.getInstance().logInWithReadPermissions(activity, listOf("email"))
        LoginManager.getInstance().registerCallback(callbackManager,
            object : FacebookCallback<LoginResult> {
                override fun onSuccess(loginResult: LoginResult) {
                    Log.d(AuthorizationPresenterFacebook.TAG, "facebook:onSuccess:$loginResult")
                    handleFacebookAccessToken(loginResult.accessToken, activity)
                }

                override fun onCancel() {
                    Log.d(AuthorizationPresenterFacebook.TAG, "facebook:onCancel")
                }

                override fun onError(error: FacebookException) {
                    Log.d(AuthorizationPresenterFacebook.TAG, "facebook:onError", error)
                }
            })
    }
}