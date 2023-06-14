package com.example.finans.operation

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.preference.PreferenceManager
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.finans.analytics.AnalyticsActivity
import com.example.finans.R
import com.example.finans.accounts.BottomSheetAccountsChange
import com.example.finans.language.loadLocale
import com.example.finans.operation.operationDetail.BottomSheetOperationDetailFragment
import com.example.finans.other.deletionWarning
import com.example.finans.plans.PlansActivity
import com.example.finans.plans.budgets.Budgets
import com.example.finans.plans.paymentPlanning.PaymentPlanning
import com.example.finans.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class HomeActivity : AppCompatActivity(), OnItemClickListener{

    inner class SwipeToDeleteCallback(private val adapter: RecyclerView.Adapter<*>) :
        ItemTouchHelper.Callback() {

        override fun getMovementFlags(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder
        ): Int {
            val swipeFlags = ItemTouchHelper.LEFT
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

                        val money = if (operation.typeEn == "Income") -operation.value!!  else operation.value

                        val accounts = pref.getString("accounts", "")

                        if (documentId != null) {
                            userId
                                .collection("accounts")
                                .document(accounts!!)
                                .collection("operation")
                                .document(documentId)
                                .delete()
                                .addOnSuccessListener {

                                    userId
                                        .collection("user").document("information")
                                        .update("total_balance", FieldValue.increment(money!!))
                                        .addOnSuccessListener {}
                                        .addOnFailureListener {}

                                    userId
                                        .collection("accounts").document(accounts)
                                        .update("balance", FieldValue.increment(money))
                                        .addOnSuccessListener {}
                                        .addOnFailureListener {}

                                    if(operation.typeEn == "Expense") {
                                        val budgetsCollectionRef =
                                            Firebase.firestore.collection("users")
                                                .document(Firebase.auth.uid.toString())
                                                .collection("budgets")

                                        budgetsCollectionRef
                                            .whereGreaterThan("timeEnd", Timestamp.now())
                                            .get()
                                            .addOnSuccessListener { querySnapshot ->
                                                for (document in querySnapshot.documents) {
                                                    val data =
                                                        document.toObject(Budgets::class.java)

                                                    val accountsList = data!!.accounts

                                                    if (accountsList != null && (accountsList.isEmpty() || accounts in accountsList)) {

                                                        val categoriesList = data.categories

                                                        val parts = operation.category!!.split("/")
                                                        val categoryName = parts.getOrNull(2)

                                                        if (categoriesList != null && (categoriesList.isEmpty() || categoryName in categoriesList)) {

                                                            println("${data.name}   $categoriesList")

                                                            budgetsCollectionRef.document(data.id!!)
                                                                .update(
                                                                    "valueNow",
                                                                    FieldValue.increment(-operation.value!!)
                                                                )
                                                                .addOnSuccessListener {
                                                                    budgetsCollectionRef.document(data.id!!)
                                                                        .collection("operation")
                                                                        .document(documentId)
                                                                        .delete()
                                                                        .addOnSuccessListener {}
                                                                }
                                                                .addOnFailureListener {}
                                                        }
                                                    }
                                                }
                                            }
                                            .addOnFailureListener { e ->

                                            }
                                    }

                                }
                                .addOnFailureListener { error ->
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
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var pref: SharedPreferences
    private lateinit var userId: DocumentReference
    private lateinit var accounts: String
    private var selectedDate: Date? = null

    @SuppressLint("ResourceType", "SuspiciousIndentation")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pref = getSharedPreferences("Settings", MODE_PRIVATE)


        if (pref.contains("shortcuts")) {

            val bottomSheetFragment = BottomSheetNewOperationFragment()
            bottomSheetFragment.show(supportFragmentManager, " BottomSheetDialog")

            pref.edit().remove("shortcuts").apply()

        }

        if (pref.contains("paymentPlanning")) {

            val paymentPlanningJson = pref.getString("paymentPlanning", null)
            val paymentPlanning = Gson().fromJson(paymentPlanningJson, PaymentPlanning::class.java)

            val fragment = BottomSheetNewOperationFragment.newInstance(paymentPlanning)
            fragment.show(supportFragmentManager, "BottomSheetNewOperationFragment")

            pref.edit().remove("paymentPlanning").apply()
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
        val dateTimeFormat = SimpleDateFormat("LLLL yyyy", Locale.getDefault())
        selectedDate = Date()
        date.text = dateTimeFormat.format(selectedDate!!).capitalize(Locale.ROOT)

        val loc = this.getSharedPreferences("Settings", Context.MODE_PRIVATE)


        date.setOnClickListener{
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)

            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, monthOfYear, _ ->
                    calendar.set(year, monthOfYear, 1)
                    selectedDate = calendar.time
                    date.text = dateTimeFormat.format(selectedDate).capitalize(Locale.ROOT)

                    operationArrayList = arrayListOf()

                    operationAdapter = OperationAdapter(operationArrayList)
                    operationAdapter.setOnItemClickListener(this)

                    operationAdapter.setSharedPreferencesLocale(loc, switchState)

                    operationRecyclerView.adapter = operationAdapter

                    operationAdapter.notifyDataSetChanged()

                    getOperationData()

                },
                currentYear,
                currentMonth,
                1
            )

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()
        }


        operationRecyclerView = findViewById(R.id.itemsRecyclerView)
        operationRecyclerView.layoutManager = LinearLayoutManager(this)

        operationArrayList = arrayListOf()

        operationAdapter = OperationAdapter(operationArrayList)
        operationAdapter.setOnItemClickListener(this)

        operationAdapter.setSharedPreferencesLocale(loc, switchState)

        operationRecyclerView.adapter = operationAdapter

        operationAdapter.notifyDataSetChanged()

        userId = FirebaseFirestore.getInstance().collection("users")
            .document(Firebase.auth.uid.toString())


        getOperationData()

        val searchView = findViewById<SearchView>(R.id.operationSearchView)

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                operationAdapter.getFilter().filter(newText)
                return false
            }

        })

        val swipeToDeleteCallback = SwipeToDeleteCallback(operationAdapter)
        val itemTouchHelper = ItemTouchHelper(swipeToDeleteCallback)
        itemTouchHelper.attachToRecyclerView(operationRecyclerView)


        var userRef = userId.collection("user").document("information")


        userRef.get()
            .addOnSuccessListener { snapshot ->
                val ac = snapshot.getString("accounts")
                if (ac != null) {
                    accounts = ac.toString()

                    val editor = pref.edit()
                    editor!!.putString("accounts", accounts)
                    editor.apply()

                    userRef = userId.collection("accounts").document(accounts)

                    userRef.get()
                        .addOnSuccessListener { documentSnapshot ->
                            val balance = documentSnapshot.getDouble("balance")
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
                                val decimalFormat = DecimalFormat("#,##0.00")

                                homeBalanceTextView?.text = decimalFormat.format(balance).toString()
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

                searchView.setQuery("", false)
                searchView.clearFocus()
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0)

                operationArrayList = arrayListOf()

                operationAdapter = OperationAdapter(operationArrayList)
                operationAdapter.setOnItemClickListener(this)

                operationAdapter.setSharedPreferencesLocale(loc, switchState)

                operationRecyclerView.adapter = operationAdapter

                operationAdapter.notifyDataSetChanged()

                getOperationData()

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
        var accounts: String?


        val userRef = userId.collection("user").document("information")

        userRef.get()
            .addOnSuccessListener { snapshot ->
                val ac = snapshot.getString("accounts")
                if (ac != null) {
                    accounts = ac.toString()

                    val calendar = Calendar.getInstance()
                    calendar.time = selectedDate!!

                    val currentMonth = calendar.get(Calendar.MONTH) + 1
                    val currentYear = calendar.get(Calendar.YEAR)

                    val startDate = Timestamp(Date(currentYear - 1900, currentMonth - 1, 1))
                    val endDate = Timestamp(Date(currentYear - 1900, currentMonth, 1))

                    userId
                        .collection("accounts")
                        .document(accounts!!)
                        .collection("operation")
                        .whereGreaterThanOrEqualTo("timestamp", startDate)
                        .whereLessThan("timestamp", endDate)
                        .addSnapshotListener { value, error ->
                            if (error != null) {
                                return@addSnapshotListener
                            }

                            for (dc in value?.documentChanges!!) {
                                val operation = dc.document.toObject(Operation::class.java)
                                val index = operationArrayList.indexOfFirst { it.id == operation.id }
                                when (dc.type) {
                                    DocumentChange.Type.ADDED -> {
                                        if (index == -1) {
                                            operationArrayList.add(operation)
                                        }
                                    }
                                    DocumentChange.Type.MODIFIED -> {
                                        if (index != -1) {
                                            operationArrayList[index] = operation
                                        }
                                    }
                                    DocumentChange.Type.REMOVED -> {
                                        if (index != -1) {
                                            operationArrayList.removeAt(index)
                                        }
                                    }
                                }
                            }
                            operationAdapter.notifyDataSetChanged()

                        }
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


    fun add(view: View) {

        val bottomSheetFragment =
            supportFragmentManager.findFragmentByTag("BottomSheetNewOperationFragment") as? BottomSheetNewOperationFragment
        if (bottomSheetFragment == null) {
            val newOperationFragment = BottomSheetNewOperationFragment()
            newOperationFragment.show(
                supportFragmentManager,
                "BottomSheetNewOperationFragment"
            )
        }

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


}