package com.example.finans.authorization

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Configuration
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.finans.PinCodeActivity
import com.example.finans.R
import com.example.finans.authorization.authWithFacebook.AuthorizationModelFacebook
import com.example.finans.authorization.authWithFacebook.AuthorizationPresenterFacebook
import com.example.finans.authorization.authWithGoogle.AuthorizationModelGoogle
import com.example.finans.authorization.authWithGoogle.AuthorizationPresenterGoogle
import com.example.finans.authorization.passwordResetEmail.BottomSheetPasswordResetFragment
import com.example.finans.authorization.profile.BottomSheetIsEmailVerified
import com.example.finans.language.loadLocale
import com.example.finans.operation.BottomSheetNewOperationFragment
import com.example.finans.other.isEmailValid
import com.example.finans.other.isValidPassword
import com.example.finans.operation.HomeActivity
import com.example.finans.registration.RegistrationActivity
import com.facebook.CallbackManager
import com.facebook.FacebookSdk
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class AuthorizationActivity : AppCompatActivity(), AuthorizationView {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var presenterGoogle: AuthorizationPresenterGoogle
    private lateinit var presenterFacebook: AuthorizationPresenterFacebook
    private lateinit var splashScreen: SplashScreen
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private lateinit var email: EditText
    private lateinit var password: EditText

    lateinit var callbackManager: CallbackManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        splashScreen = installSplashScreen()

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        editor = sharedPreferences.edit()

        if(!sharedPreferences.contains("isPassword")){
            when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
                Configuration.UI_MODE_NIGHT_NO -> {
                    editor.putBoolean(
                        "modeSwitch",
                        false
                    ).apply()

                }

                Configuration.UI_MODE_NIGHT_YES -> {
                    editor.putBoolean(
                        "modeSwitch",
                        true
                    ).apply()
                }
            }
        }

        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)


        if (!sharedPref.contains("locale")) {

            val editor = sharedPref?.edit()
            editor!!.putString("locale", Locale.getDefault().language)
            editor.apply()
        }
        loadLocale(resources, this)

        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        if(switchState){
            setContentView(R.layout.activity_dark_authorization)
            window.statusBarColor = getColor(R.color.background2_dark)
        }
        else{
            setContentView(R.layout.activity_authorization)
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

        email = findViewById(R.id.editTextTextEmailAddress)
        password = findViewById(R.id.editTextTextPassword)

        email.addTextChangedListener(textWatcher(findViewById(R.id.editTextTextEmailAddress)))
        password.addTextChangedListener(textWatcher(findViewById(R.id.editTextTextPassword)))
    }





    @SuppressLint("SuspiciousIndentation")
    override fun onStart() {
        super.onStart()
        val prefs = PreferenceManager.getDefaultSharedPreferences(this)
        val isPassword = prefs.getBoolean("isPassword", false)

        val currentUser = auth.currentUser


                if (currentUser != null) {

                    when (intent.action) {
                        "new_operation_action" -> {
                            val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
                            val editor = sharedPref?.edit()
                            editor!!.putString("shortcuts", intent.action.toString())
                            editor.apply()
                        }
                    }

                //    if(Firebase.auth.currentUser!!.isEmailVerified) {

                        if (isPassword) {
                            this@AuthorizationActivity.startActivity(
                                Intent(
                                    this@AuthorizationActivity,
                                    PinCodeActivity::class.java
                                )
                            )
                            finish()
                        } else
                            this@AuthorizationActivity.startActivity(
                                Intent(
                                    this@AuthorizationActivity,
                                    HomeActivity::class.java
                                )
                            )
                        finish()
//                    }
//                    else{
//                        val bottomSheetFragment = supportFragmentManager.findFragmentByTag("BottomSheetIsEmailVerified") as? BottomSheetIsEmailVerified
//                        if (bottomSheetFragment == null)
//                            BottomSheetIsEmailVerified().show(supportFragmentManager, "BottomSheetIsEmailVerified")
//                    }
                }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenterGoogle.onActivityResult(requestCode, resultCode, data)
        presenterFacebook.onActivityResult(requestCode, resultCode, data)
    }



//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == AuthorizationPresenterGoogle.RC_SIGN_IN) {
//            val tk = GoogleSignIn.getSignedInAccountFromIntent(data)
//            try {
//                val account = tk.getResult(ApiException::class.java)
//                val email = account?.email
//
//                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email!!)
//                    .addOnCompleteListener { task ->
//                        if (task.isSuccessful) {
//                            val result = task.result
//                            if ((result?.signInMethods?.size ?: 0) > 0) {
//                                Toast.makeText(requireContext(), "Такой уже есть", Toast.LENGTH_SHORT).show()
//                            } else {
//
//                                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
//                                FirebaseAuth.getInstance().signInWithCredential(credential)
//                                    .addOnCompleteListener { tsk ->
//                                        if (tsk.isSuccessful) {
//                                            Log.d(ContentValues.TAG, "linkWithCredential:success")
//                                            bottomSheetSettingsFragment?.dismiss()
//                                            dismiss()
//                                            requireActivity().recreate()
//
//                                        } else {
//                                            Log.w(ContentValues.TAG, "linkWithCredential:failure", tsk.exception)
//                                            Toast.makeText(
//                                                requireContext(), "Authentication failed.",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        }
//                                    }
//
//                            }
//                        }
//                    }
//
//
//            } catch (e: ApiException) {
//            }
//        }
//    }
//
    override fun showMainScreen() {
        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        prefs.edit().putBoolean("isPassword", true).apply()

        val db = FirebaseFirestore.getInstance()

        db.document("/users/${Firebase.auth.uid.toString()}").get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val document = task.result
                if (document.exists()) {
                    startActivity(Intent(this, PinCodeActivity::class.java))
                    finish()
                } else {
                    uploadData()
                    startActivity(Intent(this, PinCodeActivity::class.java))
                    finish()
                }
            }
        }

    }

    private fun uploadData() {
        val format = SimpleDateFormat("dd.M.yyyy", Locale.getDefault());
        val currentDate = format.format(Date())

        val userId = FirebaseFirestore.getInstance().collection("users").document(Firebase.auth.uid.toString())

        val informationMap = hashMapOf<String, Any>(
            "date_registration" to currentDate,
            "total_balance" to 0,
            "accounts" to "cash"
        )

        userId.collection("user").document("information").set(informationMap, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Registration", "Added document")
            }
            .addOnFailureListener { exception ->
                Log.d("Registration", "Error adding document $exception")

            }

        val accountsMap = hashMapOf<String, Any>(
            "name" to "cash",
            "balance" to 0,
            "nameRus" to "Наличные",
            "nameEng" to "Cash",
            "icon" to "gs://finans-44544.appspot.com/accounts/coins.png",
            "currency" to ""
        )

            val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)

            val editor = sharedPref?.edit()
            editor!!.putString("accounts", "cash")
            editor.apply()


        userId.collection("accounts").document("cash").set(accountsMap, SetOptions.merge())
            .addOnSuccessListener {
                Log.d("Registration", "Added document")
            }
            .addOnFailureListener { exception ->
                Log.d("Registration", "Error adding document $exception")

            }


        val sourceCollectionRef = FirebaseFirestore.getInstance().collection("category")
        val targetCollectionRef = userId.collection("category")


        sourceCollectionRef.get().addOnSuccessListener { querySnapshot ->
            for (documentSnapshot in querySnapshot.documents) {
                val documentData = documentSnapshot.data
                if (documentData != null) {
                    val targetDocumentRef = targetCollectionRef.document(documentSnapshot.id)
                    targetDocumentRef.set(documentData)
                        .addOnSuccessListener { Log.d(TAG, "Document copied successfully!") }
                        .addOnFailureListener { e -> Log.w(TAG, "Error copying document", e) }

                    val subCollectionRef = sourceCollectionRef.document(documentSnapshot.id).collection("subcategories")
                    val targetSubCollectionRef = targetDocumentRef.collection("subcategories")
                    subCollectionRef.get().addOnSuccessListener { subCollectionQuerySnapshot ->
                        for (subCollectionDocSnapshot in subCollectionQuerySnapshot.documents) {
                            targetSubCollectionRef.document(subCollectionDocSnapshot.id).set(subCollectionDocSnapshot.data!!)
                        }
                    }
                }
            }
        }

        val user = Firebase.auth.currentUser

        val name: String = user?.displayName ?: "Username"

        val profileUpdates = userProfileChangeRequest {
            displayName = name
        }

        user!!.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User profile updated.")
                }
            }

    }

    override fun showErrorScreen() {

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

        val textInputLayout = password.parent.parent as? TextInputLayout

        if(password.text.toString().isValidPassword(this) != null) {

            textInputLayout?.setPasswordVisibilityToggleEnabled(false)
        }
        else
            textInputLayout?.setPasswordVisibilityToggleEnabled(true)

        email.error = email.text.toString().isEmailValid(this)
        password.error = password.text.toString().isValidPassword(this)

        if(email.text.toString().isEmailValid(this) == null && password.text.toString().isValidPassword(this) == null) {

            auth.signInWithEmailAndPassword(email.text.toString(), password.text.toString())
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        Log.d("signInWithEmail", "signInWithEmail:success")
                        showMainScreen()
                    } else {
                            val builder = AlertDialog.Builder(view.context)

                            builder.setTitle(R.string.error)

                            builder.setMessage(R.string.userNotFound)

                            builder.setNeutralButton(
                                "Ok"
                            ) { dialog, id ->
                                email.text.clear()
                                password.text.clear()
                            }

                        builder.setNegativeButton(
                            R.string.cancel
                        ) { dialog, id -> }
                            builder.show()
                    }
                }
        }
    }

    fun resetPassword(view: View) {
        val bottomSheetFragment = supportFragmentManager.findFragmentByTag("BottomSheetPasswordResetFragment") as? BottomSheetPasswordResetFragment
        if (bottomSheetFragment == null)
            BottomSheetPasswordResetFragment().show(supportFragmentManager, "BottomSheetPasswordResetFragment")

    }


    private fun textWatcher(editText: TextInputEditText): TextWatcher = object : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {

            val textInputLayout = editText.parent.parent as? TextInputLayout

            if (editText.inputType and InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
                editText.error = editText.text.toString().isEmailValid(this@AuthorizationActivity)
            } else
                if (editText.inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD == InputType.TYPE_TEXT_VARIATION_PASSWORD) {

                    if(editText.text.toString().isValidPassword(this@AuthorizationActivity) != null) {

                        textInputLayout?.setPasswordVisibilityToggleEnabled(false)
                    }
                    else
                        textInputLayout?.setPasswordVisibilityToggleEnabled(true)

                    editText.error = editText.text.toString().isValidPassword(this@AuthorizationActivity)

                }
        }
    }

}