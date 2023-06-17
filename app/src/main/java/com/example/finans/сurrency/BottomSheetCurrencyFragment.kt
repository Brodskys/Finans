package com.example.finans.сurrency

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.PinCodeActivity
import com.example.finans.R
import com.example.finans.other.deletionWarning
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase
import okhttp3.OkHttpClient
import java.util.Locale
import java.util.concurrent.TimeUnit


class BottomSheetCurrencyFragment : BottomSheetDialogFragment(), OnItemClickListener {

    private lateinit var currencyRecyclerView: RecyclerView
    private lateinit var currencyArrayList: ArrayList<Currency>
    private lateinit var currencyAdapter: CurrencyAdapter
    private lateinit var dbref: FirebaseFirestore
    private lateinit var currencyName: String
    private lateinit var type: String
    private lateinit var sharedPreferences: SharedPreferences
    var switchState: Boolean = false
    private lateinit var currencyViewModel: CurrencyViewModel
    var isCurrency: Boolean = true
    private lateinit var editor: SharedPreferences.Editor

    companion object {
        fun newInstance(type: String): BottomSheetCurrencyFragment {
            val args = Bundle()
            args.putString("type", type)

            val fragment = BottomSheetCurrencyFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {
            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }

        dialog?.setCancelable(false)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if (switchState) {
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_currency, container, false)
        } else {
            inflater.inflate(R.layout.fragment_bottom_sheet_currency, container, false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        currencyViewModel = ViewModelProvider(requireActivity())[CurrencyViewModel::class.java]
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currencySearch = view.findViewById<SearchView>(R.id.currencySearch)
        val currencyExit = view.findViewById<TextView>(R.id.currencyExit)
        val currencySave = view.findViewById<TextView>(R.id.currencySave)

        val userRef = FirebaseFirestore.getInstance().collection("users")
            .document(Firebase.auth.uid.toString()).collection("accounts").document("cash")

        var currency = ""

        userRef.get()
            .addOnSuccessListener { snapshot ->
                currency = snapshot.getString("currency")!!
                if (currency != "") {
                    currencyExit.isVisible = true
                    isCurrency = false
                }
            }



        currencySearch.queryHint = getText(R.string.search)
        type = arguments?.getString("type").toString()

        if (switchState) {
            val searchEditText =
                currencySearch.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText.setHintTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.background3_dark
                )
            )
            searchEditText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.background3_dark
                )
            )
        } else {
            val searchEditText =
                currencySearch.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText.setHintTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.background3
                )
            )
            searchEditText.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.background3
                )
            )
        }

        currencyRecyclerView = view.findViewById(R.id.currencyRecyclerView)
        currencyRecyclerView.layoutManager = LinearLayoutManager(context)
        currencyRecyclerView.setHasFixedSize(true)

        currencyArrayList = arrayListOf()

        val prefs = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val prefs2 = PreferenceManager.getDefaultSharedPreferences(requireContext())


        currencyAdapter = CurrencyAdapter(currencyArrayList)
        currencyAdapter.setOnItemClickListener(this)
        currencyAdapter.setSharedPreferencesLocale(prefs, prefs2)
        currencyRecyclerView.adapter = currencyAdapter


        currencyAdapter.notifyDataSetChanged()

        getCurrencyData()


        currencyExit.setOnClickListener {

            dismiss()
        }

        editor = sharedPreferences.edit()


        currencySave.setOnClickListener {
            if (type == "change" || currency == "" || type == "changeAc") {
                val sharedPref =
                    requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)

                val accounts = sharedPref.getString("accounts", "")

                val userID = FirebaseFirestore.getInstance().collection("users")
                    .document(Firebase.auth.uid.toString())

                val docRef = userID.collection("accounts")
                    .document(accounts!!)

                if(type == "changeAc"){
                    currencyViewModel.selectedCurrency(currencyName)
                }

                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()


                if(currencyExit.isVisible) {
                    docRef.get().addOnSuccessListener { documentSnapshot ->
                        if (documentSnapshot.exists()) {
                            val currency = documentSnapshot.getString("currency")!!
                            var balanc = documentSnapshot.getDouble("balance")!!

                            if (currency != currencyName) {

                                convertCurrency(
                                    lifecycleScope,
                                    client,
                                    currency,
                                    currencyName
                                ) { value ->
                                    requireActivity().runOnUiThread {
                                        currencyConvertWarning(
                                            currency,
                                            currencyName,
                                            value
                                        ) { result ->


                                            if (result) {

                                                userID.collection("accounts")
                                                    .document(accounts).collection("operation")
                                                    .get().addOnSuccessListener { querySnapshot ->
                                                        for (document in querySnapshot.documents) {
                                                            val sum = document.getDouble("value")!!

                                                            val convertedValue = String.format(
                                                                Locale.ENGLISH,
                                                                "%.2f",
                                                                sum * value.toDouble()
                                                            ).toDouble()

                                                            document.reference.update(
                                                                "value",
                                                                convertedValue
                                                            )
                                                                .addOnSuccessListener {

                                                                    val accountsCurrncy =
                                                                        hashMapOf<String, Any>(
                                                                            "currency" to currencyName,
                                                                            "balance" to String.format(
                                                                                Locale.ENGLISH,
                                                                                "%.2f",
                                                                                balanc * value.toDouble()
                                                                            ).toDouble(),
                                                                        )

                                                                    docRef.update(accountsCurrncy)
                                                                        .addOnSuccessListener {
                                                                            dismiss()
                                                                        }
                                                                        .addOnFailureListener { e ->
                                                                        }
                                                                }
                                                                .addOnFailureListener { e ->

                                                                }
                                                        }
                                                    }

                                            } else {
                                                val accountsCurrncy = hashMapOf<String, Any>(
                                                    "currency" to currencyName,
                                                )

                                                docRef.update(accountsCurrncy)
                                                    .addOnSuccessListener {
                                                        dismiss()
                                                    }
                                                    .addOnFailureListener { e ->
                                                    }
                                            }
                                        }
                                    }
                                }
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    getString(R.string.currencyWarning),
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    }
                }
                else{
                    val accountsCurrency = hashMapOf<String, Any>(
                        "currency" to currencyName,
                    )

                    docRef.update(accountsCurrency)
                        .addOnSuccessListener {
                            dismiss()
                            val intent = Intent (requireContext(), PinCodeActivity::class.java)
                            requireActivity().startActivity(intent)
                            requireActivity().finish()

                            editor.putBoolean(
                                "isCurrency",
                                true
                            ).apply()
                        }
                        .addOnFailureListener { e ->
                        }
                }

            }
            else {
                currencyViewModel.selectedCurrency(currencyName)
                dismiss()
            }

        }


        currencySearch.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currencyAdapter.getFilter().filter(newText)
                return false
            }

        })


    }

    fun currencyConvertWarning(
        biloDo: String,
        staloPosle: String,
        value: String,
        callback: (Boolean) -> Unit
    ) {

        val pref =
            requireActivity().getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)
        val locale = pref.getString("locale", "")

        val builder = AlertDialog.Builder(requireContext())

        builder.setTitle(getString(R.string.сonversion))


        if (locale == "ru") {
            builder.setMessage("Вы хотите конвертировать ${biloDo} в ${staloPosle} по курсу ${value}?")
        } else {
            builder.setMessage("Do you want to convert ${biloDo} to ${staloPosle} at the rate of ${value}?")
        }

        builder.setPositiveButton(
            getString(R.string.yes)
        ) { dialog, id ->
            callback(true)
        }

        builder.setNegativeButton(
            getString(R.string.no)
        ) { dialog, id ->
            callback(false)
        }

        builder.show()

    }

    override fun onItemClick(currency: Currency) {
        view?.findViewById<TextView>(R.id.currencySave)!!.isVisible = true
        currencyName = currency.Name.toString()
    }

    private fun getCurrencyData() {
        dbref = FirebaseFirestore.getInstance()


        dbref.collection("currency").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null) {
                    return
                }

                for (dc: DocumentChange in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        currencyArrayList.add(dc.document.toObject(Currency::class.java))
                    }
                }
                currencyAdapter.notifyDataSetChanged()
            }
        })

    }

}