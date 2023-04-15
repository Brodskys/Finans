package com.example.finans.operation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.AnalyticsActivity
import com.example.finans.BottomSheetNewOperationFragment
import com.example.finans.PlansActivity
import com.example.finans.R
import com.example.finans.category.subcategory.BottomSheetSubcategoryFragment
import com.example.finans.language.loadLocale
import com.example.finans.operation.operationDetail.BottomSheetOperationDetailFragment
import com.example.finans.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class HomeActivity : AppCompatActivity(), OnItemClickListener {

    private lateinit var pref : SharedPreferences

    private lateinit var bottomNav : BottomNavigationView

    private lateinit var operationRecyclerView: RecyclerView
    private lateinit var operationArrayList: ArrayList<Operation>
    private lateinit var operationAdapter: OperationAdapter

    private lateinit var sharedPreferences: SharedPreferences


    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE)



        if (prefs.contains("shortcuts")) {

            val bottomSheetFragment = BottomSheetNewOperationFragment()
            bottomSheetFragment.show(supportFragmentManager, " BottomSheetDialog")

            prefs.edit().remove("shortcuts").apply()

        }

        loadLocale(resources, this)
        pref = getSharedPreferences("Password", Context.MODE_PRIVATE)


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)


        if(switchState){
            setContentView(R.layout.activity_dark_home)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList = ContextCompat.getColorStateList(this,
                R.drawable.selector_dark
            )
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor = ContextCompat.getColorStateList(this,
                R.drawable.selector_dark
            )
            val window: Window = window

            window.statusBarColor = getColor(R.color.background2_dark)
        }
        else{
            setContentView(R.layout.activity_home)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList = ContextCompat.getColorStateList(this,
                R.drawable.selector
            )
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor = ContextCompat.getColorStateList(this,
                R.drawable.selector
            )

        }





        operationRecyclerView = findViewById(R.id.itemsRecyclerView)
        operationRecyclerView.layoutManager = LinearLayoutManager(this)
        operationRecyclerView.setHasFixedSize(true)

        operationArrayList = arrayListOf()

        operationAdapter = OperationAdapter(operationArrayList)
        operationAdapter.setOnItemClickListener(this)

        operationRecyclerView.adapter = operationAdapter


        operationAdapter.notifyDataSetChanged()

        getOperationData()

        val fireStoreDatabase = FirebaseFirestore.getInstance()

        val userRef = fireStoreDatabase.collection("users").document(Firebase.auth.uid.toString())
            .collection("user").document("information")

        userRef.get()
            .addOnSuccessListener { documentSnapshot ->
                val balance = documentSnapshot.getLong("balance")
                if (balance != null) {
                    findViewById<TextView>(R.id.home_balance).text =  balance.toString()
                }
            }

        findViewById<FloatingActionButton>(R.id.navigationViewAdd).backgroundTintList = ColorStateList.valueOf(Color.parseColor("#2d313d"))

        bottomNav = findViewById(R.id.bottomNavigationView)
        bottomNav.setOnNavigationItemSelectedListener(navListener)



    }

    private fun getOperationData() {

        FirebaseFirestore.getInstance()
            .collection("users")
            .document(Firebase.auth.uid.toString())
            .collection("operation")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                for (dc in value?.documentChanges!!) {
                    operationArrayList.add(dc.document.toObject(Operation::class.java))
                }

                operationAdapter.notifyDataSetChanged()
            }
    }


    private val navListener = BottomNavigationView.OnNavigationItemSelectedListener{

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

    override fun onItemClick(operation: Operation) {

        val bottomSheetFragment =
            supportFragmentManager.findFragmentByTag("BottomSheetOperationDetailFragment") as? BottomSheetOperationDetailFragment

        if (bottomSheetFragment == null) {
            val newFragment = BottomSheetOperationDetailFragment.newInstance(operation)
            newFragment.show(
                supportFragmentManager,
                "BottomSheetSubcategoryFragment"
            )
        }

    }


}