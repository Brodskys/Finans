package com.example.finans.operation

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.Window
import android.widget.RelativeLayout
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
import com.example.finans.accounts.BottomSheetAccountsChange
import com.example.finans.authorization.AuthorizationActivity
import com.example.finans.language.loadLocale
import com.example.finans.operation.operationDetail.BottomSheetOperationDetailFragment
import com.example.finans.other.deletionWarning
import com.example.finans.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class RecyclerViewTouchListener(
    private val gestureDetector: GestureDetector
) : RecyclerView.OnItemTouchListener {

    override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(e)
        return false
    }

    override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
}

class HomeActivity : AppCompatActivity(), OnItemClickListener, GestureDetector.OnGestureListener {
    inner class SwipeToDeleteCallback(private val adapter: RecyclerView.Adapter<*>) :
        ItemTouchHelper.Callback() {

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val swipeFlags = ItemTouchHelper.START or ItemTouchHelper.END
            return makeMovementFlags(0, swipeFlags)
        }

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean {
            return false
        }

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            deletionWarning(this@HomeActivity) { result ->

                if (result) {

                    try {
                        val position = viewHolder.adapterPosition

                        val operation = operationArrayList[position]
                        val documentId = operation.id
                        val accounts = pref.getString("accounts", "")

                        if (documentId != null) {
                            userId
                                .collection("accounts")
                                .document(accounts!!)
                                .collection("operation")
                                .document(documentId)
                                .delete()
                                .addOnSuccessListener {
                                    adapter.notifyItemRemoved(position)
                                    recreate()
                                }
                                .addOnFailureListener { error ->
                                    // Handle the failure case if needed
                                }
                        }
                    } catch (e: Exception) {
                        println(e.message)
                    }
                } else
                    operationAdapter.notifyDataSetChanged()
            }
        }
    }


    private lateinit var bottomNav: BottomNavigationView

    private lateinit var operationRecyclerView: RecyclerView
    private lateinit var operationArrayList: ArrayList<Operation>
    private lateinit var operationAdapter: OperationAdapter
    private lateinit var gestureDetector: GestureDetector
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pref: SharedPreferences
    private lateinit var recyclerViewTouchListener: RecyclerViewTouchListener
    private lateinit var userId: DocumentReference
    private lateinit var accounts: String

    @SuppressLint("ResourceType", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = getSharedPreferences("Settings", MODE_PRIVATE)


        if (pref.contains("shortcuts")) {

            val bottomSheetFragment = BottomSheetNewOperationFragment()
            bottomSheetFragment.show(supportFragmentManager, " BottomSheetDialog")

            pref.edit().remove("shortcuts").apply()

        }

        loadLocale(resources, this)


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)


        if (switchState) {
            setContentView(R.layout.activity_dark_home)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList =
                ContextCompat.getColorStateList(
                    this,
                    R.drawable.selector_dark
                )
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor =
                ContextCompat.getColorStateList(
                    this,
                    R.drawable.selector_dark
                )
            val window: Window = window

            window.statusBarColor = getColor(R.color.background2_dark)
        } else {
            setContentView(R.layout.activity_home)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList =
                ContextCompat.getColorStateList(
                    this,
                    R.drawable.selector
                )
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor =
                ContextCompat.getColorStateList(
                    this,
                    R.drawable.selector
                )

        }


        val date = findViewById<TextView>(R.id.mouth_operations)
        val dateTimeFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        date.text = dateTimeFormat.format(Date()).capitalize(Locale.ROOT)


        operationRecyclerView = findViewById(R.id.itemsRecyclerView)
        operationRecyclerView.layoutManager = LinearLayoutManager(this)
        operationRecyclerView.setHasFixedSize(true)

        operationArrayList = arrayListOf()

        operationAdapter = OperationAdapter(operationArrayList)
        operationAdapter.setOnItemClickListener(this)

        val loc = this.getSharedPreferences("Settings", Context.MODE_PRIVATE)


        operationAdapter.setSharedPreferencesLocale(loc, switchState)

        operationRecyclerView.adapter = operationAdapter

        operationAdapter.notifyDataSetChanged()

        userId = FirebaseFirestore.getInstance().collection("users")
            .document(Firebase.auth.uid.toString())


        getOperationData()

        gestureDetector = GestureDetector(this, this)
        recyclerViewTouchListener = RecyclerViewTouchListener(gestureDetector)
        operationRecyclerView.addOnItemTouchListener(recyclerViewTouchListener)

        val swipeToDeleteCallback = SwipeToDeleteCallback(operationAdapter)
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(operationRecyclerView)

        val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)


        var userRef = userId.collection("user").document("information")


        userRef.get()
            .addOnSuccessListener { snapshot ->
                val ac = snapshot.getString("accounts")
                if (ac != null) {
                    accounts = ac.toString()

                    val editor = sharedPref?.edit()
                    editor!!.putString("accounts", accounts)
                    editor.apply()

                    userRef = userId.collection("accounts").document(accounts)

                    userRef.get()
                        .addOnSuccessListener { documentSnapshot ->
                            val balance = documentSnapshot.getLong("balance")
                            val currency = documentSnapshot.getString("currency")

                            val locale = pref.getString("locale", "")

                            val name = if (locale == "ru") {
                                documentSnapshot.getString("nameRus")!!
                            } else {
                                documentSnapshot.getString("nameEng")!!
                            }
                            if (balance != null) {
                                val homeBalanceTextView =
                                    findViewById<TextView>(R.id.accounts_balance)
                                homeBalanceTextView?.text = balance.toString()
                            }
                            if (currency != null) {
                                val accountsCurrency =
                                    findViewById<TextView>(R.id.accounts_currency)
                                accountsCurrency?.text = currency.toString()
                            }
                            if (currency != null) {
                                val accountsCurrency =
                                    findViewById<TextView>(R.id.accounts_currency)
                                accountsCurrency?.text = currency.toString()
                            }
                            val accountsName = findViewById<TextView>(R.id.accounts_name)
                            accountsName?.text = name.toString()

                        }
                }
            }


        findViewById<FloatingActionButton>(R.id.navigationViewAdd).backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#2d313d"))

        bottomNav = findViewById(R.id.bottomNavigationView)
        bottomNav.setOnNavigationItemSelectedListener(navListener)



        findViewById<RelativeLayout>(R.id.accounts_RelativeLayout).setOnClickListener {
            Toast.makeText(this, "", Toast.LENGTH_LONG).show()
        }


        val swipeRefreshLayout = findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)


        swipeRefreshLayout.setColorSchemeResources(
            android.R.color.holo_blue_bright,
            android.R.color.holo_green_light,
            android.R.color.holo_orange_light
        )

        swipeRefreshLayout.setOnRefreshListener {
            swipeRefreshLayout.postDelayed({

                recreate()

                swipeRefreshLayout.isRefreshing = false


            }, 1000)
        }



        findViewById<RelativeLayout>(R.id.accounts_RelativeLayout).setOnClickListener {
            val bottomSheetFragment =
                supportFragmentManager.findFragmentByTag("BottomSheetAccountsChange") as? BottomSheetAccountsChange
            if (bottomSheetFragment == null)
                BottomSheetAccountsChange().show(
                    supportFragmentManager,
                    "BottomSheetAccountsChange"
                )
        }

    }

    private fun getOperationData() {
        pref = getSharedPreferences("Settings", MODE_PRIVATE)
        val accounts = pref.getString("accounts", "")

        userId
            .collection("accounts")
            .document(accounts!!)
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

        val bottomSheetFragment =
            supportFragmentManager.findFragmentByTag("BottomSheetNewOperationFragment") as? BottomSheetNewOperationFragment
        if (bottomSheetFragment == null)
            BottomSheetNewOperationFragment().show(
                supportFragmentManager,
                "BottomSheetNewOperationFragment"
            )

    }

    private var isItemClickEnabled = true

    override fun onItemClick(operation: Operation) {

        if (isItemClickEnabled) {
            isItemClickEnabled = false

            val newFragment = BottomSheetOperationDetailFragment.newInstance(operation)
            newFragment.show(
                supportFragmentManager,
                "BottomSheetSubcategoryFragment"
            )

            Handler().postDelayed({
                isItemClickEnabled = true
            }, 1000)
        }


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
        val diffX = e2.x.minus(e1.x)
        val diffY = e2.y.minus(e1.y)

        if (abs(diffX) > abs(diffY)) {
            if (abs(diffX) > 200 && abs(velocityX) > 200) {
                if (diffX < 0) {

                    this.startActivity(Intent(this, PlansActivity::class.java))
                    overridePendingTransition(0, 0)
                }
                return true
            }
        }
        return false

    }

}