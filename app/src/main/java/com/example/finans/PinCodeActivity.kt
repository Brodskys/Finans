package com.example.finans

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Vibrator
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chaos.view.PinView
import com.example.finans.authorization.AuthorizationActivity
import com.example.finans.language.loadLocale
import com.example.finans.operation.HomeActivity
import com.example.finans.сurrency.BottomSheetCurrencyFragment
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import render.animations.Attention
import render.animations.Render

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




        if (!prefs.contains("currency")) {

            BottomSheetCurrencyFragment().show(
                supportFragmentManager,
                "BottomSheetCurrencyFragment"
            )

        }

        if (prefs.contains("shortcuts")) {

            val bottomSheetFragment = BottomSheetNewOperationFragment()
            bottomSheetFragment.show(supportFragmentManager, " BottomSheetDialog")

            prefs.edit().remove("shortcuts").apply()

        }


        errorPin = pinErrorText.text.toString()



        pinView = findViewById(R.id.PinView)
        pinText = findViewById(R.id.pinText)
        pinView.requestFocus()

        pin = ""
        render = Render(this)

        pref = getSharedPreferences("Settings", Context.MODE_PRIVATE)

        pinView.postDelayed({
            pinView.requestFocus()
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(pinView, InputMethodManager.SHOW_IMPLICIT)
        }, 350)

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
                                Firebase.auth.signOut()
                                pinErrorText.visibility = View.INVISIBLE
                                intentActivity(AuthorizationActivity())
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