package com.example.finans.operation

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.view.Window
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.finans.AnalyticsActivity
import com.example.finans.PlansActivity
import com.example.finans.R
import com.example.finans.authorization.AuthorizationActivity
import com.example.finans.language.loadLocale
import com.example.finans.operation.operationDetail.BottomSheetOperationDetailFragment
import com.example.finans.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SwipeToDeleteCallback(private val adapter: RecyclerView.Adapter<*>) : ItemTouchHelper.Callback() {


    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
        return makeMovementFlags(0, swipeFlags)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        return false
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        val position = viewHolder.adapterPosition
        adapter.notifyItemRemoved(position)
    }
}
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

        val prefs = getSharedPreferences("Settings", MODE_PRIVATE)



        if (prefs.contains("shortcuts")) {

            val bottomSheetFragment = BottomSheetNewOperationFragment()
            bottomSheetFragment.show(supportFragmentManager, " BottomSheetDialog")

            prefs.edit().remove("shortcuts").apply()

        }

        loadLocale(resources, this)
        pref = getSharedPreferences("Password", MODE_PRIVATE)


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


        val date =  findViewById<TextView>(R.id.mouth_operations)
        val dateTimeFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        date.text = dateTimeFormat.format(Date()).capitalize(Locale.ROOT)


        operationRecyclerView = findViewById(R.id.itemsRecyclerView)
        operationRecyclerView.layoutManager = LinearLayoutManager(this)
        operationRecyclerView.setHasFixedSize(true)

        operationArrayList = arrayListOf()

        operationAdapter = OperationAdapter(operationArrayList)
        operationAdapter.setOnItemClickListener(this)

        operationRecyclerView.adapter = operationAdapter

        operationAdapter.notifyDataSetChanged()



       getOperationData()


        val swipeToDeleteCallback = SwipeToDeleteCallback(operationAdapter)
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(operationRecyclerView)

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



        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)


       swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light
        )

        swipeRefreshLayout.setOnRefreshListener {
           swipeRefreshLayout.postDelayed({
               swipeRefreshLayout.isRefreshing = false

               operationArrayList = arrayListOf()

               operationAdapter = OperationAdapter(operationArrayList)
               operationAdapter.setOnItemClickListener(this)

               operationRecyclerView.adapter = operationAdapter


               operationAdapter.notifyDataSetChanged()
               getOperationData()


            }, 1000)
        }

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