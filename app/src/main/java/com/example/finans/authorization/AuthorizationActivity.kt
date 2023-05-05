package com.example.finans.authorization

import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.finans.operation.HomeActivity
import com.example.finans.PinCodeActivity
import com.example.finans.R
import com.example.finans.authorization.authWithFacebook.AuthorizationModelFacebook
import com.example.finans.authorization.authWithFacebook.AuthorizationPresenterFacebook
import com.example.finans.authorization.authWithGoogle.AuthorizationModelGoogle
import com.example.finans.authorization.authWithGoogle.AuthorizationPresenterGoogle
import com.example.finans.authorization.passwordResetEmail.BottomSheetPasswordResetFragment
import com.example.finans.isEmailValid
import com.example.finans.isValidPassword
import com.example.finans.registration.RegistrationActivity
import com.facebook.CallbackManager
import com.facebook.FacebookSdk
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.tasks.Task
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*


class AuthorizationActivity : AppCompatActivity(), AuthorizationView {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var presenterGoogle: AuthorizationPresenterGoogle
    private lateinit var presenterFacebook: AuthorizationPresenterFacebook
    private lateinit var splashScreen: SplashScreen
    private lateinit var sharedPreferences: SharedPreferences

    lateinit var callbackManager: CallbackManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        splashScreen = installSplashScreen()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        if(switchState){
            setContentView(R.layout.activity_dark_authorization)
            window.statusBarColor = getColor(R.color.background2_dark)

        }
        else{
            setContentView(R.layout.activity_authorization)
        }



        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        if (!sharedPref.contains("locale")) {

            val editor = sharedPref?.edit()
            editor!!.putString("locale", Locale.getDefault().language)
            editor.apply()
        }

        FacebookSdk.sdkInitialize(getApplicationContext())

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = Firebase.auth

        callbackManager = CallbackManager.Factory.create()

        presenterGoogle = AuthorizationPresenterGoogle(this,this, AuthorizationModelGoogle(auth, googleSignInClient))
        presenterFacebook = AuthorizationPresenterFacebook(this, AuthorizationModelFacebook(auth, this))
    }

    override fun onStart() {
        super.onStart()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isPassword = prefs.getBoolean("isPassword", false)

        val currentUser = auth.currentUser

        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkRequest = NetworkRequest.Builder().build()

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                if (currentUser != null) {

                    when (intent.action) {
                        "new_operation_action" -> {
                            val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
                            val editor = sharedPref?.edit()
                            editor!!.putString("shortcuts", intent.action.toString())
                            editor.apply()
                        }
                    }

                    if(isPassword) {
                        this@AuthorizationActivity.startActivity(Intent(this@AuthorizationActivity, PinCodeActivity::class.java))
                        finish()
                    } else
                        this@AuthorizationActivity.startActivity(Intent(this@AuthorizationActivity, HomeActivity::class.java))
                    finish()

                }
            }

            override fun onLost(network: Network) {
                Toast.makeText(this@AuthorizationActivity, "No Internet", Toast.LENGTH_LONG).show()
            }
        }
        connectivityManager.registerNetworkCallback(networkRequest, networkCallback)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenterGoogle.onActivityResult(requestCode, resultCode, data)
        //presenterFacebook.onActivityResult(requestCode, resultCode, data)
    }

    override fun showMainScreen() {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putBoolean("isPassword", true).apply()

        val db = FirebaseFirestore.getInstance()
        val usersCollectionRef = db.collection("users")

        val userQuery = usersCollectionRef.document(Firebase.auth.uid.toString())

//        userQuery.get()
//            .addOnSuccessListener { document ->
//                if (document.exists()) {
//                    startActivity(Intent(this, PinCodeActivity::class.java))
//                    finish()
//                } else {
//                    uploadData()
//                    startActivity(Intent(this, PinCodeActivity::class.java))
//                    finish()
//                }
//            }
//            .addOnFailureListener { exception ->
//            }


        startActivity(Intent(this, PinCodeActivity::class.java))
        finish()
    }

    private fun uploadData() {
        val format = SimpleDateFormat("dd.M.yyyy", Locale.getDefault());
        val currentDate = format.format(Date())

        val hashMap = hashMapOf<String, Any>(
            "date_registration" to currentDate,
            "balance" to 0,
            "image" to ""
        )
        val fireStoreDatabase = FirebaseFirestore.getInstance()

        fireStoreDatabase.collection("users").document(Firebase.auth.uid.toString()).collection("user").document("information").set(hashMap, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Registration", "Added document")
            }
            .addOnFailureListener { exception ->
                Log.d("Registration", "Error adding document $exception")

            }

        val sourceCollectionRef = FirebaseFirestore.getInstance().collection("category")
        val targetCollectionRef = FirebaseFirestore.getInstance().collection("users").document(Firebase.auth.uid.toString()).collection("category")


        sourceCollectionRef.get().addOnSuccessListener { querySnapshot ->
            for (documentSnapshot in querySnapshot.documents) {
                val documentData = documentSnapshot.data
                if (documentData != null) {
                    val targetDocumentRef = targetCollectionRef.document(documentSnapshot.id)
                    targetDocumentRef.set(documentData)
                        .addOnSuccessListener { Log.d(TAG, "Document copied successfully!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error copying document", e) }

                    val subcollectionRef = sourceCollectionRef.document(documentSnapshot.id).collection("subcategories")
                    val targetSubcollectionRef = targetDocumentRef.collection("subcategories")
                    subcollectionRef.get().addOnSuccessListener { subcollectionQuerySnapshot ->
                        for (subcollectionDocSnapshot in subcollectionQuerySnapshot.documents) {
                            targetSubcollectionRef.document(subcollectionDocSnapshot.id).set(subcollectionDocSnapshot.data!!)
                        }
                    }
                }
            }
        }




        val user = Firebase.auth.currentUser

        val profileUpdates = userProfileChangeRequest {
            displayName = "UserName"
            photoUri = Uri.parse("https://firebasestorage.googleapis.com/v0/b/finans-44544.appspot.com/o/images%2Fperson.png?alt=media&token=ee359b89-4846-4243-acd2-b7a09364c806")
        }

        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                }
            }

    }




    override fun showErrorScreen() {
        startActivity(Intent(this, AuthorizationActivity::class.java))
        finish()
    }

    fun signInWithGoogle(view: View) {
       presenterGoogle.signInWithGoogle()
    }

    fun signInWithFacebook(view: View) {
      presenterFacebook.signInWithFacebook()
    }



    fun signUp(view: View) {
        startActivity(Intent(this, RegistrationActivity::class.java))
        overridePendingTransition(0,0)
        finish()
    }

    fun signInWithAnonymously(view: View) {
        auth.signInAnonymously()
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d("signInAnonymously", "signInAnonymously:success")
                    val user = auth.currentUser
                    uploadData()
                    showMainScreen()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w("signInAnonymously", "signInAnonymously:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    showErrorScreen()
                }
            }
    }

    fun signInWithEmailAndPassword(view: View) {
        val email = findViewById<EditText>(R.id.editTextTextEmailAddress)
        val password = findViewById<EditText>(R.id.editTextTextPassword)

        val textInputLayout = password.parent.parent as? TextInputLayout

        if(password.text.toString().isValidPassword() != null) {

            textInputLayout?.setPasswordVisibilityToggleEnabled(false)
        }
        else
            textInputLayout?.setPasswordVisibilityToggleEnabled(true)

        email.error = email.text.toString().isEmailValid()
        password.error = password.text.toString().isValidPassword()

        if(email.text.toString().isEmailValid() == null && password.text.toString().isValidPassword() == null) {

            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("signInWithEmail", "signInWithEmail:success")
                        val user = auth.currentUser
                        showMainScreen()
                    } else {
                        Log.w("signInWithEmail", "signInWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        showErrorScreen()
                    }
                }
        }
    }

    fun resetPassword(view: View) {
        val bottomSheetFragment = supportFragmentManager.findFragmentByTag("BottomSheetPasswordResetFragment") as? BottomSheetPasswordResetFragment
        if (bottomSheetFragment == null)
            BottomSheetPasswordResetFragment().show(supportFragmentManager, "BottomSheetPasswordResetFragment")

    }

}