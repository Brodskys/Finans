package com.example.finans

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.hardware.fingerprint.FingerprintManager
import android.os.Bundle
import android.os.Vibrator
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.chaos.view.PinView
import com.example.finans.authorization.AuthorizationActivity
import com.example.finans.language.loadLocale
import com.example.finans.operation.BottomSheetNewOperationFragment
import com.example.finans.operation.HomeActivity
import com.example.finans.plans.paymentPlanning.PaymentPlanning
import com.example.finans.settings.deleteUser
import com.example.finans.сurrency.BottomSheetCurrencyFragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import render.animations.Attention
import render.animations.Render
import java.util.concurrent.Executor


class PinCodeActivity : AppCompatActivity() {

    lateinit var pinView: PinView
    lateinit var pin: String
    lateinit var pinText: TextView
    lateinit var pinErrorText: TextView
    lateinit var pref: SharedPreferences
    private lateinit var render: Render
    private var currentUser: FirebaseUser? = null
    var error: Int = 4
    private lateinit var errorPin: String
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo
    @SuppressLint("CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currentUser = Firebase.auth.currentUser

        if (currentUser == null) {
            intentActivity(AuthorizationActivity())
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)
        loadLocale(resources, this)


        if(switchState){
            setContentView(R.layout.activity_dark_pin_code)
            window.statusBarColor = getColor(R.color.background2_dark)

        }
        else{
            setContentView(R.layout.activity_pin_code)

        }




        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        pinErrorText = findViewById(R.id.pinErrorText)

        pinView = findViewById(R.id.PinView)
        pinText = findViewById(R.id.pinText)
        pinView.requestFocus()

        pin = ""
        render = Render(this)

        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)

                    val fingerprintManager = ContextCompat.getSystemService(
                        this@PinCodeActivity,
                        FingerprintManager::class.java
                    )

                    if (fingerprintManager != null && fingerprintManager.isHardwareDetected) {

                        if (!fingerprintManager.hasEnrolledFingerprints())
                            Toast.makeText(
                                applicationContext,
                                "$errString", Toast.LENGTH_SHORT
                            )
                                .show()
                    }
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    intentActivity(HomeActivity())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()

                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(getString(R.string.fingerprint) + " \"" + getString(R.string.app_name)+ "\"")
            .setNegativeButtonText(getString(R.string.cancel))
            .build()


        val biometricState = sharedPreferences.getBoolean("isBiometric", false)

        if(biometricState) {
            findViewById<ImageView>(R.id.biometricImg).isVisible = true
            val fingerprintManager =
                ContextCompat.getSystemService(this, FingerprintManager::class.java)
            if (fingerprintManager != null && fingerprintManager.isHardwareDetected) {

                if (fingerprintManager.hasEnrolledFingerprints()) {
                    biometricPrompt.authenticate(promptInfo)
                } else {
                    pinView.postDelayed({
                        pinView.requestFocus()
                        val imm =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(pinView, InputMethodManager.SHOW_IMPLICIT)
                    }, 350)
                }
            }
        }
        else{
            pinView.postDelayed({
                pinView.requestFocus()
                val imm =
                    getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(pinView, InputMethodManager.SHOW_IMPLICIT)
            }, 350)
        }


        if (prefs.contains("shortcuts")) {

            val bottomSheetFragment = BottomSheetNewOperationFragment()
            bottomSheetFragment.show(supportFragmentManager, " BottomSheetDialog")

            prefs.edit().remove("shortcuts").apply()

        }

        if (prefs.contains("paymentPlanning")) {

            val paymentPlanningJson = prefs.getString("paymentPlanning", null)
            val paymentPlanning = Gson().fromJson(paymentPlanningJson, PaymentPlanning::class.java)

            val fragment = BottomSheetNewOperationFragment.newInstance(paymentPlanning)
            fragment.show(supportFragmentManager, "BottomSheetNewOperationFragment")

            prefs.edit().remove("paymentPlanning").apply()
        }

        errorPin = pinErrorText.text.toString()


        val biometric = findViewById<ImageView>(R.id.biometricImg)

        biometric.setOnClickListener {
            biometricPrompt.authenticate(promptInfo)
        }





        pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)

//        pinView.postDelayed({
//            pinView.requestFocus()
//            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.showSoftInput(pinView, InputMethodManager.SHOW_IMPLICIT)
//        }, 350)

        pinView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.toString().length == 4) {
                    if (pref.contains("Pincode")) {

                        if (pref.getString("Pincode", "") == s.toString()) {

                            intentActivity(HomeActivity())

                        } else if (pref.getString("Pincode", "") != s.toString()) {
                            error--
                            errorPin()
                            pinErrorText.visibility = View.VISIBLE


                            val sb = StringBuilder()
                            sb.append(errorPin).append(" $error")


                            pinErrorText.text = sb

                            if (error == 0) {

                                val editor = pref.edit()
                                editor?.remove("Pincode")
                                editor?.apply()

                                if (Firebase.auth.currentUser!!.isAnonymous) {
                                    deleteUser(sharedPreferences, this@PinCodeActivity)
                                }
                                else {

                                    Firebase.auth.signOut()
                                    pinErrorText.visibility = View.INVISIBLE
                                    intentActivity(AuthorizationActivity())
                                }
                            }
                        }

                    } else {
                        if (pin == "") {
                            pin = s.toString()
                            pinText.setText(R.string.repeatPIN)
                            pinView.text?.clear()
                        } else {
                            if (s.toString().length == 4) {
                                if (s.toString() == pin) {

                                    val editor = pref.edit()

                                    editor?.putString("Pincode", pin)
                                    editor?.apply()

                                    intentActivity(HomeActivity())
                                }

                            } else {

                                errorPin()

                                pinText.setText(R.string.enterPIN)
                                pin = ""

                            }
                        }
                    }
                }

            }

        })

    }


    override fun onBackPressed() {}


    fun intentActivity(activity: AppCompatActivity) {
        val intent = Intent(this, activity::class.java)
        startActivity(intent)
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        finish()

    }

    fun errorPin() {
        render.setAnimation(Attention().Swing(pinView))
        render.start()
        val vibratorService =
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        vibratorService.vibrate(500)
        pinView.text?.clear()
    }

}