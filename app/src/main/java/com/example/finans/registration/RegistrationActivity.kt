package com.example.finans.registration

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.example.finans.PinCodeActivity
import com.example.finans.R
import com.example.finans.authorization.AuthorizationActivity
import com.example.finans.other.isConfirmPassword
import com.example.finans.other.isEmailValid
import com.example.finans.other.isValidPassword
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*

class RegistrationActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    private lateinit var gestureDetector: GestureDetector
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        if(switchState){
            setContentView(R.layout.activity_dark_registration)
            window.statusBarColor = getColor(R.color.background2_dark)
        }
        else{
            setContentView(R.layout.activity_registration)
        }

        gestureDetector = GestureDetector(this, this)
        auth = Firebase.auth


        findViewById<TextInputEditText>(R.id.createUserTextEmailAddress).addTextChangedListener(textWatcher(findViewById(R.id.createUserTextEmailAddress)))
        findViewById<TextInputEditText>(R.id.createUserTextPassword).addTextChangedListener(textWatcher(findViewById(R.id.createUserTextPassword)))
        findViewById<TextInputEditText>(R.id.createUserTextConfirmPassword).addTextChangedListener(textWatcher(findViewById(R.id.createUserTextConfirmPassword)))

        }
    private fun textWatcher(editText: TextInputEditText): TextWatcher = object : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {

            val textInputLayout = editText.parent.parent as? TextInputLayout

            if (editText.inputType and InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
                editText.error = editText.text.toString().isEmailValid(this@RegistrationActivity)
            } else
                if (editText.inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD == InputType.TYPE_TEXT_VARIATION_PASSWORD) {

                    if(editText.text.toString().isValidPassword(this@RegistrationActivity) != null) {

                        textInputLayout?.setPasswordVisibilityToggleEnabled(false)
                    }
                    else
                        textInputLayout?.setPasswordVisibilityToggleEnabled(true)

                    editText.error = editText.text.toString().isValidPassword(this@RegistrationActivity)

                }
        }
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }

    override fun onDown(p0: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent) {}

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return true
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onLongPress(p0: MotionEvent) {}

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {
        val swipeThreshold = 200
        val swipeVelocityThreshold = 200

        if (e2.x > e1.x && abs(e2.x - e1.x) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
            this.startActivity(Intent(this, AuthorizationActivity::class.java))
            overridePendingTransition(0,0)
            finish()
        }
        return true
    }

    fun authorizationBack(view: View) {
        this.startActivity(Intent(this, AuthorizationActivity::class.java))
        overridePendingTransition(0,0)
        finish()
    }

    fun createUserWithEmailAndPassword(view: View) {
        val email = findViewById<EditText>(R.id.createUserTextEmailAddress)
        val password = findViewById<EditText>(R.id.createUserTextPassword)
        val confirmPassword = findViewById<EditText>(R.id.createUserTextConfirmPassword)

        val textInputLayout = password.parent.parent as? TextInputLayout
        val textInputLayout2 = confirmPassword.parent.parent as? TextInputLayout


        if(password.text.toString().isValidPassword(this) != null || password.text.toString().isConfirmPassword(this) != null) {

            textInputLayout?.setPasswordVisibilityToggleEnabled(false)
        }
        else
            textInputLayout?.setPasswordVisibilityToggleEnabled(true)


        if(confirmPassword.text.toString().isValidPassword(this) != null|| confirmPassword.text.toString().isConfirmPassword(this) != null) {

            textInputLayout2?.setPasswordVisibilityToggleEnabled(false)
        }
        else
            textInputLayout2?.setPasswordVisibilityToggleEnabled(true)




        email.error = email.text.toString().isEmailValid(this)
        password.error = password.text.toString().isValidPassword(this)
        confirmPassword.error = confirmPassword.text.toString().isValidPassword(this)

        if(email.text.toString().isEmailValid(this) == null && password.text.toString().isValidPassword(this) == null
            && confirmPassword.text.toString().isValidPassword(this) == null) {

            if (password.text.toString() == confirmPassword.text.toString()) {
                auth.createUserWithEmailAndPassword(email.text.toString(), password.text.toString())
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {

                            val user = Firebase.auth.currentUser

                            val languageCode = Locale.getDefault().language
                            auth.setLanguageCode(languageCode)

                            user!!.sendEmailVerification()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("Email", "Email sent.")
                                    }
                                }

                            val name: String = user.displayName ?: "Username"

                            val profileUpdates = userProfileChangeRequest {
                                displayName = name
                            }

                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d(ContentValues.TAG, "User profile updated.")
                                    }
                                }

                            val prefs: SharedPreferences =
                                PreferenceManager.getDefaultSharedPreferences(this)
                            prefs.edit().putBoolean("isPassword", true).apply()
                            uploadData()
                            startActivity(Intent(this, PinCodeActivity::class.java))
                            finish()

                        } else {

                            val exception = task.exception
                            if (exception is FirebaseAuthUserCollisionException) {

                                    val builder = AlertDialog.Builder(view.context)

                                    builder.setTitle(R.string.error)

                                    builder.setMessage(R.string.userAlreadyRegistered)

                                    builder.setNegativeButton(
                                        "Ok") { dialog, id ->
                                    }
                                    builder.show()

                                email.text.clear()
                                password.text.clear()
                                confirmPassword.text.clear()

                                }
                            }
                    }
            } else {
                password.error = email.text.toString().isConfirmPassword(this)
                confirmPassword.error = email.text.toString().isConfirmPassword(this)
            }
        }

    }

    private fun uploadData() {
        val format = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
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
                        .addOnSuccessListener { Log.d(ContentValues.TAG, "Document copied successfully!") }
                        .addOnFailureListener { e -> Log.w(ContentValues.TAG, "Error copying document", e) }

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
                    Log.d(ContentValues.TAG, "User profile updated.")
                }
            }
    }
}