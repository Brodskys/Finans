package com.example.finans.language

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.settings.SettingsActivity
import com.example.finans.сurrency.CurrencyAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetLanguageFragment : BottomSheetDialogFragment(), OnItemClickListener {

    private lateinit var languageRecyclerView: RecyclerView
    private lateinit var languageArrayList: ArrayList<Language>
    private lateinit var languageAdapter: LanguageAdapter
    private lateinit var languageName: String
    private var act: SettingsActivity? = null
    private lateinit var sharedPreferences: SharedPreferences
    var switchState: Boolean = false

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is SettingsActivity) {
            act = context
        }
    }

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
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_language, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_language, container, false)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val languageSearch = view.findViewById<SearchView>(R.id.languageSearch)

        languageSearch.queryHint = getText(R.string.search)

        if(switchState) {
            val searchEditText = languageSearch.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.background3_dark))
            searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.background3_dark))
        }
        else{
            val searchEditText = languageSearch.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.background3))
            searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.background3))
        }

        languageRecyclerView = view.findViewById(R.id.languageRecyclerView)
        languageRecyclerView.layoutManager = LinearLayoutManager(context)
        languageRecyclerView.setHasFixedSize(true)


        languageArrayList = arrayListOf()


        val prefs = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val prefs2 =  PreferenceManager.getDefaultSharedPreferences(requireContext())


        languageAdapter = LanguageAdapter(languageArrayList)
        languageAdapter.setOnItemClickListener(this)

        if (prefs != null) {
            languageAdapter.setSharedPreferencesLocale(prefs,prefs2)
        }

        languageRecyclerView.adapter = languageAdapter


        languageAdapter.notifyDataSetChanged()


        getLanguageData()


        view.findViewById<TextView>(R.id.languageExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.languageSave).setOnClickListener {

            val sharedPref = context?.getSharedPreferences("Settings", Context.MODE_PRIVATE)

            val editor = sharedPref?.edit()

            editor?.putString("locale", languageName)
            editor?.apply()

            dismiss()
        }

        languageSearch.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                languageAdapter.getFilter().filter(newText)
                return false
            }

        })

    }
    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        act?.recreateActivity()
    }

    fun setActivity(activity: SettingsActivity) {
        this.act = activity
    }

    override fun onItemClick(language: Language) {
        languageName = language.Language.toString()
    }


    private fun getLanguageData() {

        languageArrayList.add(Language("Русский язык", "Russian language","RUS", "ru"))
        languageArrayList.add(Language("Английский язык", "English language", "ENG","en"))



        languageAdapter.notifyDataSetChanged()
    }




}