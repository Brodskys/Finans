package com.example.finans.registration

import android.content.ContentValues
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
        if (e2.x > e1.x && abs(e2.x - e1.x) > 200 && abs(velocityX) > 200) {
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

                            val name: String = user.displayName ?: "UserName"
                            val photo: Uri = user.photoUrl ?: Uri.parse("https://firebasestorage.googleapis.com/v0/b/finans-44544.appspot.com/o/images%2Fperson.png?alt=media&token=ee359b89-4846-4243-acd2-b7a09364c806")

                            val profileUpdates = userProfileChangeRequest {
                                displayName = name
                                photoUri = photo
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
        val format = SimpleDateFormat("dd.M.yyyy", Locale.getDefault());
        val currentDate = format.format(Date())

        val hashMap = hashMapOf<String, Any>(
            "date_registration" to currentDate,
            "balance" to 0,
        )
        val fireStoreDatabase = FirebaseFirestore.getInstance()
        fireStoreDatabase.collection("users").document(Firebase.auth.uid.toString()).collection("user").document("information").set(hashMap)
            .addOnSuccessListener {
                Log.d("Registration", "Added document with ID")
            }
            .addOnFailureListener { exception ->
                Log.d("Registration", "Error adding document $exception")

            }

        val sourceCollectionRef = FirebaseFirestore.getInstance().collection("category")
        val targetCollectionRef = FirebaseFirestore.getInstance().collection("users").document(Firebase.auth.uid.toString()).collection("category")

        sourceCollectionRef.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    val data = document.data
                    val documentId = document.id
                    targetCollectionRef.document(documentId).set(data)
                }
            }
            .addOnFailureListener { exception ->
                Log.w("category", "Ошибка получения документов: ", exception)
            }
    }
}