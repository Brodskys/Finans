package com.example.finans.сurrency

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.BottomSheetNewOperationFragment
import com.example.finans.R
import com.example.finans.settings.SettingsActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.*


class BottomSheetCurrencyFragment: BottomSheetDialogFragment(), OnItemClickListener {

    private lateinit var currencyRecyclerView: RecyclerView
    private lateinit var currencyArrayList: ArrayList<Currency>
    private lateinit var currencyAdapter: CurrencyAdapter
    private lateinit var dbref: FirebaseFirestore
    private lateinit var currencyName: String
    private lateinit var sharedPreferences: SharedPreferences
    var switchState: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {
            it.behavior.peekHeight  = R.style.AppBottomSheetDialogTheme
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_currency, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_currency, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val currencySearch = view.findViewById<SearchView>(R.id.currencySearch)

        currencySearch.queryHint = getText(R.string.search)

        if(switchState) {
            val searchEditText = currencySearch.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.background3_dark))
            searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.background3_dark))
        }
        else{
            val searchEditText = currencySearch.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.background3))
            searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.background3))
        }

        currencyRecyclerView = view.findViewById(R.id.currencyRecyclerView)
        currencyRecyclerView.layoutManager = LinearLayoutManager(context)
        currencyRecyclerView.setHasFixedSize(true)

        currencyArrayList = arrayListOf()

        val prefs = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val prefs2 =  PreferenceManager.getDefaultSharedPreferences(requireContext())


        currencyAdapter = CurrencyAdapter(currencyArrayList)
        currencyAdapter.setOnItemClickListener(this)

        if (prefs != null) {
            currencyAdapter.setSharedPreferencesLocale(prefs,prefs2)

        }


        currencyRecyclerView.adapter = currencyAdapter


        currencyAdapter.notifyDataSetChanged()

        getCurrencyData()



        val isCurrency = prefs?.contains("currency")

        val currencyExit =  view.findViewById<TextView>(R.id.currencyExit)
        val currencySave =  view.findViewById<TextView>(R.id.currencySave)

        if(isCurrency == false) {
            currencyExit.isVisible = false
            currencySave.isVisible = false
        }

        currencyExit.setOnClickListener {
            openBottomSheetFragment()
        }

        currencySave.setOnClickListener {
            val sharedPref = context?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val editor = sharedPref?.edit()
            editor!!.putString("currency", currencyName)
            editor.apply()

            if(isCurrency == false) {

                dismiss()

            }
            else
                openBottomSheetFragment()

        }


        currencySearch.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currencyAdapter.getFilter().filter(newText)
                return false
            }

        })


    }


    override fun onItemClick(currency: Currency) {
        view?.findViewById<TextView>(R.id.currencySave)!!.isVisible = true
        currencyName = currency.Name.toString()
    }

    private fun openBottomSheetFragment(){
        if(activity is SettingsActivity) { }
        else
        {
            val existingFragment = requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetNewOperationFragment")
            if (existingFragment == null)
            {
                val newFragment = BottomSheetNewOperationFragment()
                newFragment.show(requireActivity().supportFragmentManager, "BottomSheetNewOperationFragment")
            }
        }

        dismiss()
    }

    private fun getCurrencyData() {
        dbref = FirebaseFirestore.getInstance()


        dbref.collection("currency").
        addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null) {
                    return
                }

                for (dc: DocumentChange in value?.documentChanges!!){
                    if (dc.type == DocumentChange.Type.ADDED){
                        currencyArrayList.add(dc.document.toObject(Currency::class.java))
                    }
                }
                currencyAdapter.notifyDataSetChanged()
            }
        })

    }

}