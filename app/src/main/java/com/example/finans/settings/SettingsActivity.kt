package com.example.finans.settings

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.finans.*
import com.example.finans.authorization.AuthorizationActivity
import com.example.finans.language.BottomSheetLanguageFragment
import com.example.finans.language.loadLocale
import com.example.finans.operation.HomeActivity
import com.example.finans.сurrency.BottomSheetCurrencyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import android.Manifest
import android.app.ActivityManager
import android.content.BroadcastReceiver
import android.content.IntentFilter
import android.util.Log
import android.widget.Switch
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class SettingsActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val REQUEST_CODE_SMS_PERMISSION = 100


    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)


        if(switchState){
            setContentView(R.layout.activity_dark_settings)
            window.statusBarColor = getColor(R.color.background2_dark)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList = ContextCompat.getColorStateList(this, R.drawable.selector_dark)
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor = ContextCompat.getColorStateList(this, R.drawable.selector_dark)

            findViewById<AppCompatButton>(R.id.editProfile_btn).background = getDrawable(R.drawable.round_20_dark)

            val parentLayout = findViewById<LinearLayout>(R.id.setting_layout)

            for (i in 0 until parentLayout.childCount) {
                val childView = parentLayout.getChildAt(i)

                if (childView is LinearLayout) {
                    childView.background = getDrawable(R.drawable.round_20_dark)
                }
            }
        }
        else{
            setContentView(R.layout.activity_settings)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList = ContextCompat.getColorStateList(this, R.drawable.selector)
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor = ContextCompat.getColorStateList(this, R.drawable.selector)

            findViewById<AppCompatButton>(R.id.editProfile_btn).background = getDrawable(R.drawable.round_20)

            val parentLayout = findViewById<LinearLayout>(R.id.setting_layout)

            for (i in 0 until parentLayout.childCount) {
                val childView = parentLayout.getChildAt(i)

                if (childView is LinearLayout) {
                    childView.background = getDrawable(R.drawable.round_20)
                }
            }
        }


        loadLocale(resources, this)

        findViewById<FloatingActionButton>(R.id.navigationViewAdd).backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#2d313d"))

         Picasso.get().load(Firebase.auth.currentUser?.photoUrl)
             .placeholder(R.drawable.person)
             .error(R.drawable.person)
            .into(findViewById<ImageView>(R.id.userImageSetting))


        bottomNav = findViewById(R.id.bottomNavigationView)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
        bottomNav.menu.getItem(4).isChecked = true

        findViewById<TextView>(R.id.userEmail).text = Firebase.auth.currentUser?.displayName

        editor = sharedPreferences.edit()

        val passwordState = sharedPreferences.getBoolean("isPassword", false)
        findViewById<SwitchCompat>(R.id.passwordSwitch).isChecked = passwordState


        findViewById<SwitchCompat>(R.id.modeSwitch).isChecked = switchState

        isSwitchStateChecked(switchState)


        findViewById<SwitchCompat>(R.id.modeSwitch).setOnCheckedChangeListener { _, isChecked ->

            if (isSwitchStateChecked(isChecked)) {
                editor.putBoolean(
                    "modeSwitch",
                    findViewById<SwitchCompat>(R.id.modeSwitch).isChecked
                ).apply()
                recreate()
            } else {
                editor.putBoolean(
                    "modeSwitch",
                    findViewById<SwitchCompat>(R.id.modeSwitch).isChecked
                ).apply()
                recreate()
            }
        }

        findViewById<SwitchCompat>(R.id.passwordSwitch).setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                editor.putBoolean(
                    "isPassword",
                    findViewById<SwitchCompat>(R.id.passwordSwitch).isChecked
                ).apply()
            } else {
                editor.putBoolean(
                    "isPassword",
                    findViewById<SwitchCompat>(R.id.passwordSwitch).isChecked
                ).apply()
            }
        }

        val smsSwith = findViewById<SwitchCompat>(R.id.smsSwitch)


        smsSwith.isChecked = isServiceRunning(SmsService::class.java)

        smsSwith.setOnCheckedChangeListener { _, isChecked ->
            val intent = Intent(this, SmsService::class.java)
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_SMS), REQUEST_CODE_SMS_PERMISSION)
                } else {
                    startService(intent)
                }
            } else {
                stopService(intent)
            }
        }

        findViewById<LinearLayout>(R.id.exitBtn).setOnClickListener {
            sharedPreferences = getSharedPreferences("Settings", Context.MODE_PRIVATE)!!

            val editor = sharedPreferences.edit()
            editor?.remove("Pincode")
            editor?.apply()

//            editor?.remove("currency")
//            editor?.apply()

            Firebase.auth.signOut()
            startActivity(Intent(this, AuthorizationActivity::class.java))
            finish()
        }

        findViewById<AppCompatButton>(R.id.editProfile_btn).setOnClickListener {

            val bottomSheetFragment =
                supportFragmentManager.findFragmentByTag("BottomSheetSettingsFragment") as? BottomSheetSettingsFragment
            if (bottomSheetFragment == null)
                BottomSheetSettingsFragment().show(
                    supportFragmentManager,
                    "BottomSheetSettingsFragment"
                )

        }


        findViewById<RelativeLayout>(R.id.currencyBtn).setOnClickListener {

            val bottomSheetFragment =
                supportFragmentManager.findFragmentByTag("BottomSheetCurrencyFragment") as? BottomSheetCurrencyFragment
            if (bottomSheetFragment == null)
                BottomSheetCurrencyFragment().show(
                    supportFragmentManager,
                    "BottomSheetCurrencyFragment"
                )

        }
        findViewById<RelativeLayout>(R.id.languageBtn).setOnClickListener {

            val bottomSheetFragment =
                supportFragmentManager.findFragmentByTag("BottomSheetLanguageFragment") as? BottomSheetLanguageFragment
            if (bottomSheetFragment == null) {
                BottomSheetLanguageFragment().setActivity(this)
                BottomSheetLanguageFragment().show(
                    supportFragmentManager,
                    "BottomSheetLanguageFragment"
                )
            }
        }

    }



    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener {

        when (it.itemId) {
            R.id.home -> {
                this.startActivity(Intent(this, HomeActivity::class.java))
                overridePendingTransition(0, 0)
            }
            R.id.planning -> {
                this.startActivity(Intent(this, PlansActivity::class.java))
                overridePendingTransition(0, 0)
            }

            R.id.analytics -> {
                this.startActivity(Intent(this, AnalyticsActivity::class.java))
                overridePendingTransition(0, 0)
            }
            R.id.menu -> {
                this.startActivity(Intent(this, SettingsActivity::class.java))
                overridePendingTransition(0, 0)

            }
        }
        true
    }


    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    fun recreateActivity() {
        recreate()
        loadLocale(resources, this)
    }

    private fun isSwitchStateChecked(state: Boolean): Boolean {
        return if (state) {
            findViewById<TextView>(R.id.modeSwitchText).text = "Night Mode"
            findViewById<ImageView>(R.id.modeIcon).setImageResource(R.drawable.night_mode)
            true
        } else {
            findViewById<TextView>(R.id.modeSwitchText).text = "Light Mode"
            findViewById<ImageView>(R.id.modeIcon).setImageResource(R.drawable.light_mode)
            false
        }
    }

    fun add(view: View) {

        val bottomSheetFragment =
            supportFragmentManager.findFragmentByTag("BottomSheetNewOperationFragment") as? BottomSheetNewOperationFragment
        if (bottomSheetFragment == null)
            BottomSheetNewOperationFragment().show(
                supportFragmentManager,
                "BottomSheetNewOperationFragment"
            )

    }
}