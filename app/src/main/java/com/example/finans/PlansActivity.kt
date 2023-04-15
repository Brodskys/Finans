package com.example.finans

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.finans.language.loadLocale
import com.example.finans.operation.HomeActivity
import com.example.finans.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PlansActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences


    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)


        if(switchState){
            setContentView(R.layout.activity_dark_plans)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList = ContextCompat.getColorStateList(this, R.drawable.selector_dark)
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor = ContextCompat.getColorStateList(this, R.drawable.selector_dark)
            window.statusBarColor = getColor(R.color.background2_dark)

        }
        else{
            setContentView(R.layout.activity_plans)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList = ContextCompat.getColorStateList(this, R.drawable.selector)
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor = ContextCompat.getColorStateList(this, R.drawable.selector)

        }

        loadLocale(resources, this)

        findViewById<FloatingActionButton>(R.id.navigationViewAdd).backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#2d313d"))


        bottomNav = findViewById(R.id.bottomNavigationView)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
        bottomNav.menu.getItem(1).isChecked = true

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

    fun add(view: View) {

        val bottomSheetFragment = supportFragmentManager.findFragmentByTag("BottomSheetNewOperationFragment") as? BottomSheetNewOperationFragment
        if (bottomSheetFragment == null)
            BottomSheetNewOperationFragment().show(supportFragmentManager, "BottomSheetNewOperationFragment")

    }
}