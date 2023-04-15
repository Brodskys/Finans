package com.example.finans.registration

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.finans.PinCodeActivity
import com.example.finans.R
import com.example.finans.authorization.AuthorizationActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.lang.Math.abs
import java.text.SimpleDateFormat
import java.util.*

class RegistrationActivity : AppCompatActivity(), GestureDetector.OnGestureListener {

    lateinit var gestureDetector: GestureDetector
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

        if(password.text.toString() == confirmPassword.text.toString()) {
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
                        val prefs: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
                        prefs.edit().putBoolean("isPassword", true).apply()
                        uploadData()
                        startActivity(Intent(this, PinCodeActivity::class.java))
                        finish()

                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("createUserWithEmail", "createUserWithEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext, "Authentication failed.",
                            Toast.LENGTH_SHORT
                        ).show()
                        startActivity(Intent(this, AuthorizationActivity::class.java))
                        finish()
                    }
                }
        }
        else {
            Toast.makeText(baseContext, "Passwords do not match.", Toast.LENGTH_SHORT).show()
        }
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