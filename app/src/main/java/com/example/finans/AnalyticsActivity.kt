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
import com.example.finans.operation.BottomSheetNewOperationFragment
import com.example.finans.operation.HomeActivity
import com.example.finans.settings.SettingsActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.util.Random

class AnalyticsActivity : AppCompatActivity() {
    private lateinit var bottomNav: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences


    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)


        if(switchState){
            setContentView(R.layout.activity_dark_analytics)
            window.statusBarColor = getColor(R.color.background2_dark)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList = ContextCompat.getColorStateList(this, R.drawable.selector_dark)
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor = ContextCompat.getColorStateList(this, R.drawable.selector_dark)

        }
        else{
            setContentView(R.layout.activity_analytics)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList = ContextCompat.getColorStateList(this, R.drawable.selector)
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor = ContextCompat.getColorStateList(this, R.drawable.selector)

        }

        loadLocale(resources, this)


        findViewById<FloatingActionButton>(R.id.navigationViewAdd).backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#2d313d"))

        bottomNav = findViewById(R.id.bottomNavigationView)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
        bottomNav.menu.getItem(3).isChecked = true


        val db = FirebaseFirestore.getInstance()
        val collectionRef = db.collection("users")
            .document(Firebase.auth.uid.toString())
            .collection("operation")

        collectionRef.get().addOnSuccessListener { result ->

            val entriesMap = mutableMapOf<String, Float>()

            for (document in result) {
                val value = document.getDouble("value")?.toFloat()
                val label = document.getString("category")
                if (value != null && label != null) {

                    if (entriesMap.containsKey(label)) {
                        val oldValue = entriesMap[label]
                        entriesMap[label] = oldValue!! + value
                    } else {
                        entriesMap[label] = value
                    }

                }
            }


            val entries = mutableListOf<PieEntry>()
            for ((label, value) in entriesMap) {

                var formattedLabel = label
                if (label.length > 10) {
                    formattedLabel =  label.substring(0, 10) + "\n" + label.substring(10)
                }

                entries.add(PieEntry(value, formattedLabel))
            }

            val random = Random()
            val colors = mutableListOf<Int>()
            for (i in 0 until entries.size) {
                colors.add(Color.argb(255, random.nextInt(256), random.nextInt(256), random.nextInt(256)))
            }

            val dataSet = PieDataSet(entries, null)
            dataSet.colors = colors

            val data = PieData(dataSet)
            val chart = findViewById<PieChart>(R.id.staticChart)

            data.setValueTextSize(14f)
            chart.description.text = ""

            chart.data = data

            chart.legend.isEnabled = false

            chart.invalidate()

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



    fun add(view: View) {

        val bottomSheetFragment = supportFragmentManager.findFragmentByTag("BottomSheetNewOperationFragment") as? BottomSheetNewOperationFragment
        if (bottomSheetFragment == null)
            BottomSheetNewOperationFragment().show(supportFragmentManager, "BottomSheetNewOperationFragment")

    }

}