package com.example.finans.plans.budgets

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.category.subcategory.BottomSheetSubcategoryFragment
import com.example.finans.plans.budgets.addBudgets.BottomSheetAddBudgets
import com.example.finans.plans.goals.Goals
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class BottomSheetBudgets : BottomSheetDialogFragment(), OnItemClickListener {
    private lateinit var budgetRecyclerView: RecyclerView
    private lateinit var budgetArrayList: ArrayList<Budgets>
    private lateinit var budgetAdapter: BudgetsAdapter
    private lateinit var userId: DocumentReference
    private lateinit var pref: SharedPreferences
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }
        dialog?.setCancelable(false)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if (switchState) {
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_budgets, container, false)
        } else {
            inflater.inflate(R.layout.fragment_bottom_sheet_budgets, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        view.findViewById<TextView>(R.id.budgetsExit).setOnClickListener {
            dismiss()
        }



        budgetRecyclerView = view.findViewById(R.id.budgetssRecyclerView)
        budgetRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        budgetArrayList = arrayListOf()

        budgetAdapter = BudgetsAdapter(budgetArrayList)
        budgetAdapter.setOnItemClickListener(this)

        val loc = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        budgetAdapter.setSharedPreferencesLocale(loc, switchState)


        budgetRecyclerView.adapter = budgetAdapter

        budgetAdapter.notifyDataSetChanged()

        userId = FirebaseFirestore.getInstance().collection("users")
            .document(Firebase.auth.uid.toString())

        getBudgetData()

        val searchView = view.findViewById<SearchView>(R.id.budgetsSearch)

        searchView.queryHint = getText(R.string.search)


        if (switchState) {
            val searchEditText =
                searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
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
                searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
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


        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchView.clearFocus()
                val imm = requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(searchView.windowToken, 0)
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                budgetAdapter.getFilter().filter(newText)
                return false
            }

        })

        view.findViewById<LinearLayout>(R.id.addBudgetsLinearLayout).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAddBudgets") as? BottomSheetAddBudgets
            if (bottomSheetFragment == null) {
                BottomSheetAddBudgets().show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAddBudgets"
                )
            }
        }

    }

    fun getBudgetData() {

        userId
            .collection("budgets")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                for (dc in value?.documentChanges!!) {
                    val budget = dc.document.toObject(Budgets::class.java)
                    val index = budgetArrayList.indexOfFirst { it.id == budget.id }
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            if (index == -1) {
                                budgetArrayList.add(budget)
                            }
                        }

                        DocumentChange.Type.MODIFIED -> {
                            if (index != -1) {
                                budgetArrayList[index] = budget
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            if (index != -1) {
                                budgetArrayList.removeAt(index)
                            }
                        }
                    }
                }

                budgetAdapter.notifyDataSetChanged()
            }

    }

    override fun onItemClick(budgets: Budgets) {
        val newFragment = BottomSheetBudget.newInstance(budgets)
        newFragment.setTargetFragment(this@BottomSheetBudgets, 0)

        newFragment.show(
            requireActivity().supportFragmentManager,
            "BottomSheetBudget"
        )

    }
}