package com.example.finans.analytics

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.chart.common.listener.Event
import com.anychart.chart.common.listener.ListenersInterface
import com.anychart.charts.Pie
import com.example.finans.R
import com.example.finans.accounts.AccountsViewModel
import com.example.finans.accounts.BottomSheetAccounts
import com.example.finans.language.loadLocale
import com.example.finans.map.BottomSheetMapFragment
import com.example.finans.operation.BottomSheetNewOperationFragment
import com.example.finans.operation.HomeActivity
import com.example.finans.operation.Operation
import com.example.finans.operation.OperationAdapter
import com.example.finans.plans.PlansActivity
import com.example.finans.settings.SettingsActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class AnalyticsActivity : AppCompatActivity() {
    private lateinit var operationRecyclerView: RecyclerView
    private lateinit var operationArrayList: ArrayList<Operation>
    private lateinit var operationAdapter: OperationAdapter

    private var startD: Date? = null
    private var endD: Date? = null

    private lateinit var bottomNav: BottomNavigationView
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var fromDate: TextView
    private lateinit var beforeDate: TextView

    private lateinit var fromDateRelativeLayout: RelativeLayout
    private lateinit var beforeDateRelativeLayout: RelativeLayout
    var selectedOptionIndex = 0

    private lateinit var accountsViewModel: AccountsViewModel

    private lateinit var db: FirebaseFirestore
    private var locale: String? = null

    private var acNam: String? = null

    private lateinit var pie: Pie
    private lateinit var operation: CollectionReference

    private lateinit var chart: AnyChartView
    private lateinit var user: FirebaseUser


    @OptIn(DelicateCoroutinesApi::class)
    @SuppressLint("ResourceType", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)


        if (switchState) {
            setContentView(R.layout.activity_dark_analytics)
            window.statusBarColor = getColor(R.color.background2_dark)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList =
                ContextCompat.getColorStateList(this, R.drawable.selector_dark)
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor =
                ContextCompat.getColorStateList(this, R.drawable.selector_dark)

        } else {
            setContentView(R.layout.activity_analytics)

            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemIconTintList =
                ContextCompat.getColorStateList(this, R.drawable.selector)
            findViewById<BottomNavigationView>(R.id.bottomNavigationView).itemTextColor =
                ContextCompat.getColorStateList(this, R.drawable.selector)

        }

        loadLocale(resources, this)


        val options = arrayOf(
            getString(R.string.thisMonth),
            getString(R.string.threeMonths),
            getString(R.string.sixMonths),
            getString(R.string.thisYear),
            getString(R.string.setUp)
        )

        user = Firebase.auth.currentUser!!


        chart = findViewById(R.id.staticChart)

        findViewById<TextView>(R.id.startDateTextView).text = options[0]

        operationRecyclerView = findViewById(R.id.staticOperationRecyclerView)
        operationRecyclerView.layoutManager = LinearLayoutManager(this)

        operationArrayList = arrayListOf()

        operationAdapter = OperationAdapter(operationArrayList)
        val loc = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        operationAdapter.setSharedPreferencesLocale(loc, switchState)
        operationRecyclerView.adapter = operationAdapter
        operationAdapter.notifyDataSetChanged()



        operation = FirebaseFirestore.getInstance().collection("users")
            .document(Firebase.auth.uid.toString())
            .collection("accounts")


        findViewById<FloatingActionButton>(R.id.navigationViewAdd).backgroundTintList =
            ColorStateList.valueOf(Color.parseColor("#2d313d"))

        bottomNav = findViewById(R.id.bottomNavigationView)
        bottomNav.setOnNavigationItemSelectedListener(navListener)
        bottomNav.menu.getItem(3).isChecked = true


        val pref = getSharedPreferences("Settings", MODE_PRIVATE)
        locale = pref.getString("locale", "")

        pref.getString("accounts", "")


        db = FirebaseFirestore.getInstance()

        pie = AnyChart.pie()
        chart.setChart(pie)


        val calendar = Calendar.getInstance()
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)



        fromDateRelativeLayout = findViewById(R.id.fromDateRelativeLayout)
        fromDate = findViewById(R.id.fromDateTextView)

        beforeDateRelativeLayout = findViewById(R.id.beforeDateRelativeLayout)
        beforeDate = findViewById(R.id.beforeDateTextView)


        val s = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        acNam = s.getString("accounts", "")

        val docRef = db.collection("users").document(Firebase.auth.uid.toString())
            .collection("accounts")
            .document(acNam!!)

        val accountN = findViewById<TextView>(R.id.analyticsAccountNameTextView)
        val accountIcon = findViewById<ImageView>(R.id.analyticsAccountIconImageView)

        docRef.get()
            .addOnSuccessListener { snapshot ->
                acNam = snapshot.getString("name")
                if (locale == "ru") {

                    accountN.text = snapshot!!.getString("nameRus")

                } else {
                    accountN.text = snapshot!!.getString("nameEng")
                }

                val gsReference = Firebase.storage.getReferenceFromUrl(snapshot.getString("icon")!!)

                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString()).into(accountIcon)
                }.addOnFailureListener {
                    Picasso.get().load(R.drawable.coins).into(accountIcon)
                }

            }

        accountsViewModel = ViewModelProvider(this)[AccountsViewModel::class.java]
        accountsViewModel.getSelectedAccounts().observe(this) { acc ->
            if (acc != null) {

                if (locale == "ru") {
                    accountN.text = acc.nameRus
                } else {
                    accountN.text = acc.nameEng
                }

                val gsReference = Firebase.storage.getReferenceFromUrl(acc.icon!!)

                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString()).into(accountIcon)
                }.addOnFailureListener {
                    Picasso.get().load(R.drawable.coins).into(accountIcon)
                }
                acNam = acc.name
                accountsViewModel.clearAccounts()

                chart.visibility = View.GONE
                operationRecyclerView.visibility = View.GONE
                findViewById<Button>(R.id.staticReportButton).visibility = View.GONE

            }
        }

        findViewById<RelativeLayout>(R.id.analyticsAccountsRelativeLayout).setOnClickListener {

            val existingFragment =
                supportFragmentManager.findFragmentByTag("BottomSheetAccounts")
            if (existingFragment == null) {
                val newFragment = BottomSheetAccounts.newInstance("newOper")
                newFragment.show(
                    supportFragmentManager,
                    "BottomSheetAccounts"
                )
            }

        }
        val calend = Calendar.getInstance()

        calend.set(Calendar.DAY_OF_MONTH, 1)
        startD = calend.time

        calend.set(
            Calendar.DAY_OF_MONTH,
            calend.getActualMaximum(Calendar.DAY_OF_MONTH)
        )
        endD = calend.time

        analyticsDate()


        findViewById<RelativeLayout>(R.id.forPeriodRelativeLayout).setOnClickListener {


            val alertDialogBuilder = AlertDialog.Builder(this)
            alertDialogBuilder.setTitle(getString(R.string.forPeriod))
            alertDialogBuilder.setSingleChoiceItems(
                options,
                selectedOptionIndex
            ) { dialogInterface: DialogInterface, selectedIndex: Int ->
                selectedOptionIndex = selectedIndex
            }
            alertDialogBuilder.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->
                findViewById<TextView>(R.id.startDateTextView).text = options[selectedOptionIndex]
                val calend = Calendar.getInstance()


                when (selectedOptionIndex) {
                    0 -> {
                        clear()
                        calend.set(Calendar.DAY_OF_MONTH, 1)
                        startD = calend.time

                        calend.set(
                            Calendar.DAY_OF_MONTH,
                            calend.getActualMaximum(Calendar.DAY_OF_MONTH)
                        )
                        endD = calend.time

                        analyticsDate()
                    }

                    1 -> {
                        clear()

                        calend.add(Calendar.MONTH, -2)
                        startD = calend.time
                        endD = Date()

                        analyticsDate()
                    }

                    2 -> {
                        clear()

                        calend.add(Calendar.MONTH, -5)
                        startD = calend.time
                        endD = Date()

                        analyticsDate()
                    }

                    3 -> {
                        clear()

                        calend.add(Calendar.YEAR, -1)
                        startD = calend.time
                        endD = Date()

                        analyticsDate()
                    }

                    4 -> {

                        fromDateRelativeLayout.visibility = View.VISIBLE
                    }
                }

                dialogInterface.dismiss()
            }
            alertDialogBuilder.setNegativeButton(getString(R.string.cancel)) { dialogInterface: DialogInterface, i: Int ->
                dialogInterface.dismiss()
            }
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }


        findViewById<RelativeLayout>(R.id.fromDateRelativeLayout).setOnClickListener {

            val datePickerDialog = DatePickerDialog(
                this,
                { view, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    startD = calendar.time
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    fromDate.text =
                        dateFormat.format(startD!!)
                    beforeDateRelativeLayout.visibility = View.VISIBLE

                },
                initialYear,
                initialMonth,
                initialDayOfMonth
            )

            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
            datePickerDialog.show()

        }

        findViewById<RelativeLayout>(R.id.beforeDateRelativeLayout).setOnClickListener {

            val datePickerDialog = DatePickerDialog(
                this,
                { view, year, month, dayOfMonth ->
                    calendar.set(year, month, dayOfMonth)
                    endD = calendar.time
                    val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                    beforeDate.text =
                        dateFormat.format(endD!!)
                    analyticsDate()
                },
                initialYear,
                initialMonth,
                initialDayOfMonth
            )

            datePickerDialog.datePicker.minDate = startD!!.time
            datePickerDialog.datePicker.maxDate = System.currentTimeMillis()

            datePickerDialog.show()

        }

        findViewById<Button>(R.id.staticReportButton).setOnClickListener {


            val isConnected = isInternetConnected(this)
            if (isConnected) {


                val userId = FirebaseFirestore.getInstance().collection("users")
                    .document(Firebase.auth.uid.toString())

                var userRef = userId.collection("user").document("information")
                var templateData: Map<String, String?> = HashMap()

                val th = this

                userRef.get()
                    .addOnSuccessListener { snapshot ->
                        val ac = snapshot.getString("accounts")
                        val date_registration = snapshot.getString("date_registration")
                        val total_balance = snapshot.getLong("total_balance")
                        val decimalFormat = DecimalFormat("#,##0.00")
                        var accounts = ""

                        if (ac != null) {

                            userRef = userId.collection("accounts").document(ac.toString())

                            userRef.get()
                                .addOnSuccessListener { documentSnapshot ->
                                    val balance = documentSnapshot.getDouble("balance")
                                    val currency = documentSnapshot.getString("currency")

                                    val locale = pref.getString("locale", "")

                                    accounts = if (locale == "ru") {
                                        documentSnapshot.getString("nameRus")!!
                                    } else {
                                        documentSnapshot.getString("nameEng")!!
                                    }

                                    templateData = mapOf(
                                        getString(R.string.user) to Firebase.auth.currentUser!!.displayName.toString(),
                                        getString(R.string.email) to Firebase.auth.currentUser!!.email.toString(),
                                        getString(R.string.dateRegistration) to date_registration,
                                        getString(R.string.totalBalance) to decimalFormat.format(
                                            total_balance
                                        ).toString(),
                                        getString(R.string.account) to accounts,
                                        getString(R.string.balance) to decimalFormat.format(
                                            balance
                                        )
                                            .toString(),
                                        getString(R.string.currency) to currency.toString(),
                                        getString(R.string.forPeriod) to options[selectedOptionIndex]
                                    )


                                    GlobalScope.launch(Dispatchers.IO) {

                                        sendEmail(
                                            Firebase.auth.currentUser?.email,
                                            templateData,
                                            operationArrayList,
                                            th
                                        )


                                    }
                                    Toast.makeText(
                                        th,
                                        th.getString(R.string.emailSent),
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        }
                    }

            } else {
                val builder = AlertDialog.Builder(this)

                builder.setTitle(R.string.error)

                builder.setMessage(R.string.internetConnection)

                builder.setNegativeButton(
                    "Ok"
                ) { dialog, id ->
                }
                builder.show()
            }
        }



        pie.setOnClickListener(object :
            ListenersInterface.OnClickListener(arrayOf<String>("x", "value")) {
            override fun onClick(event: Event) {

                val selectedCategory = event.data["x"]

                val calendar = Calendar.getInstance()

                calendar.time = startD!!
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startDate = calendar.time

                calendar.time = endD!!
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val endDate = calendar.time

                val categ = if (locale == "ru") "categoryRu" else "categoryEn"

                val query = operation.document(acNam!!)
                    .collection("operation")
                    .whereGreaterThanOrEqualTo("timestamp", startDate)
                    .whereLessThanOrEqualTo("timestamp", endDate)
                    .whereEqualTo(categ, selectedCategory)

                getOperationData(query)
            }
        })


    }

    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    private fun getOperationData(query: Query) {

        query.addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }

            operationArrayList.clear()
            for (dc in value?.documentChanges!!) {
                operationArrayList.add(dc.document.toObject(Operation::class.java))
            }

            operationAdapter.notifyDataSetChanged()

            if (operationArrayList.size > 0) {
                operationRecyclerView.visibility = View.VISIBLE



                if (!user.isAnonymous)
                    findViewById<Button>(R.id.staticReportButton).visibility = View.VISIBLE
            }
        }

    }

    private fun analyticsDate() {

        val calendar = Calendar.getInstance()

        calendar.time = startD!!
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        startD = calendar.time

        calendar.time = endD!!
        calendar.add(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        endD = calendar.time

        val s = getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val ac = s.getString("accounts", "")

        acNam = acNam ?: ac

        db.collection("users").document(Firebase.auth.uid.toString()).collection("accounts")
            .document(acNam!!)
            .collection("operation")
            .whereGreaterThanOrEqualTo("timestamp", startD!!)
            .whereLessThanOrEqualTo("timestamp", endD!!)
            .get().addOnSuccessListener { result ->

                val entriesMap = mutableMapOf<String, Float>()

                for (document in result) {
                    val value = document.getDouble("value")?.toFloat()

                    val label = if (locale == "ru") {
                        document.getString("categoryRu")!!
                    } else {
                        document.getString("categoryEn")!!
                    }

                    if (value != null) {

                        if (entriesMap.containsKey(label)) {
                            val oldValue = entriesMap[label]
                            entriesMap[label] = oldValue!! + value
                        } else {
                            entriesMap[label] = value
                        }

                    }
                }

                val entries = mutableListOf<DataEntry>()
                for ((label, value) in entriesMap) {
                    entries.add(ValueDataEntry(label, value))
                }



                if (entries.size > 0) {

                    chart.visibility = View.VISIBLE


                    pie.background().fill("#fdf4e3")

                    pie.legend().title().enabled(false);

                    pie.data(entries)

                }

                val calendar = Calendar.getInstance()

                calendar.time = startD!!
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val startDate = calendar.time

                calendar.time = endD!!
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val endDate = calendar.time

                val query = operation.document(acNam!!)
                    .collection("operation")
                    .whereGreaterThanOrEqualTo("timestamp", startDate)
                    .whereLessThanOrEqualTo("timestamp", endDate)

                getOperationData(query)
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


    fun clear() {
        fromDateRelativeLayout.visibility = View.GONE
        beforeDateRelativeLayout.visibility = View.GONE

        startD = null
        endD = null
        fromDate.text = ""
        beforeDate.text = ""
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