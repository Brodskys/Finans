package com.example.finans.settings

import android.Manifest
import android.annotation.SuppressLint
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SwitchCompat
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.finans.*
import com.example.finans.analytics.AnalyticsActivity
import com.example.finans.authorization.AuthorizationActivity
import com.example.finans.language.BottomSheetLanguageFragment
import com.example.finans.language.loadLocale
import com.example.finans.operation.BottomSheetNewOperationFragment
import com.example.finans.operation.HomeActivity
import com.example.finans.other.deletionWarning
import com.example.finans.plans.PlansActivity
import com.example.finans.сurrency.BottomSheetCurrencyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import kotlin.math.abs

class SettingsActivity : AppCompatActivity(), GestureDetector.OnGestureListener {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private val REQUEST_CODE_SMS_PERMISSION = 100
    private lateinit var gestureDetector: GestureDetector

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        gestureDetector = GestureDetector(this, this)

        if (switchState) {
            setContentView(R.layout.activity_dark_settings)
            window.statusBarColor = getColor(R.color.background2_dark)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList =
                ContextCompat.getColorStateList(this, R.drawable.selector_dark)
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor =
                ContextCompat.getColorStateList(this, R.drawable.selector_dark)

            findViewById<AppCompatButton>(R.id.editProfile_btn).background =
                getDrawable(R.drawable.round_20_dark)

            val parentLayout = findViewById<LinearLayout>(R.id.setting_layout)

            for (i in 0 until parentLayout.childCount) {
                val childView = parentLayout.getChildAt(i)

                if (childView is LinearLayout) {
                    childView.background = getDrawable(R.drawable.round_20_dark)
                }
            }
        } else {
            setContentView(R.layout.activity_settings)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList =
                ContextCompat.getColorStateList(this, R.drawable.selector)
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor =
                ContextCompat.getColorStateList(this, R.drawable.selector)

            findViewById<AppCompatButton>(R.id.editProfile_btn).background =
                getDrawable(R.drawable.round_20)

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
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_SMS
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.READ_SMS),
                        REQUEST_CODE_SMS_PERMISSION
                    )
                    smsSwith.isChecked = false
                } else {
                    startService(intent)
                }
            } else {
                stopService(intent)
            }
        }

        findViewById<LinearLayout>(R.id.exitBtn).setOnClickListener {

            deletionWarning(this) { result ->

                if (result) {

                    sharedPreferences = getSharedPreferences("Settings", MODE_PRIVATE)!!


                    if (Firebase.auth.currentUser!!.isAnonymous) {
                        deleteUser(sharedPreferences, this)

                    } else {

                        val editor = sharedPreferences.edit()
                        editor?.remove("Pincode")
                        editor?.apply()
                    }

                    Firebase.auth.signOut()
                    startActivity(Intent(this, AuthorizationActivity::class.java))
                    finish()
                }
            }
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

            val existingFragment =
                supportFragmentManager.findFragmentByTag("BottomSheetCurrencyFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetCurrencyFragment.newInstance("change")

                newFragment.show(
                    supportFragmentManager,
                    "BottomSheetCurrencyFragment"
                )
            }

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


        findViewById<RelativeLayout>(R.id.questionsProgram).setOnClickListener {

            val items = arrayOf(getString(R.string.financialAdvice), getString(R.string.help))

            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.questions)
            builder.setIcon(R.drawable.question)
                .setItems(items) { _, item ->

                    when (item) {

                        0 -> {

                            val bottomSheetFragment =
                                supportFragmentManager.findFragmentByTag("BottomSheetFinancialAdvice") as? BottomSheetFinancialAdvice
                            if (bottomSheetFragment == null)
                                BottomSheetFinancialAdvice().show(
                                    supportFragmentManager,
                                    "BottomSheetFinancialAdvice"
                                )

                        }

                        1 -> {

                            val bottomSheetFragment =
                                supportFragmentManager.findFragmentByTag("BottomSheetSettingsHelp") as? BottomSheetSettingsHelp
                            if (bottomSheetFragment == null)
                                BottomSheetSettingsHelp().show(
                                    supportFragmentManager,
                                    "BottomSheetSettingsHelp"
                                )

                        }

                    }

                }
            val dialog = builder.create()
            dialog.show()

        }

        findViewById<RelativeLayout>(R.id.aboutProgram).setOnClickListener {

            val items = arrayOf(getString(R.string.privacyPolicy), getString(R.string.aboutProgram))

            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.aboutProgram)
                .setItems(items) { _, item ->

                    when (item) {

                        0 -> {

                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.data =
                                Uri.parse("https://www.freeprivacypolicy.com/live/7af84e6a-a34b-415b-985d-d43ae72f64c7")
                            startActivity(intent)

                        }

                        1 -> {
                            builder.setTitle(R.string.aboutProgram)

                            builder.setIcon(R.mipmap.ic_launcher)

                            val str1 = getString(R.string.str1)
                            val str2 = getString(R.string.str2)
                            val str3 = getString(R.string.str3)
                            val str4 = getString(R.string.str4)
                            val str5 = getString(R.string.str5)

                            builder.setMessage("$str1\n$str2\n$str3\n$str4\n$str5")

                            builder.setNegativeButton(
                                "Ok"
                            ) { dialog, id ->
                            }
                            builder.show()
                        }

                    }

                }
            val dialog = builder.create()
            dialog.show()

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

    override fun onTouchEvent(event: MotionEvent): Boolean {
        return gestureDetector.onTouchEvent(event)
    }
    override fun onDown(p0: MotionEvent): Boolean {
        return true
    }

    override fun onShowPress(p0: MotionEvent) {
    }

    override fun onSingleTapUp(p0: MotionEvent): Boolean {
        return true
    }

    override fun onScroll(p0: MotionEvent, p1: MotionEvent, p2: Float, p3: Float): Boolean {
        return true
    }

    override fun onLongPress(p0: MotionEvent) {
    }

    override fun onFling(
        e1: MotionEvent,
        e2: MotionEvent,
        velocityX: Float,
        velocityY: Float
    ): Boolean {

        val swipeThreshold = 200
        val swipeVelocityThreshold = 200

        if (e2.x > e1.x && abs(e2.x - e1.x) > swipeThreshold && abs(velocityX) > swipeVelocityThreshold) {
            this.startActivity(Intent(this, AnalyticsActivity::class.java))
            overridePendingTransition(0, 0)
            return true
        }
        return false
    }
}