package com.example.finans.plans.budgets.addBudgets

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.ViewModelProvider
import com.example.finans.R
import com.example.finans.accounts.AccountsBudgetsViewModel
import com.example.finans.accounts.BottomSheetAccounts
import com.example.finans.category.BottomSheetCategoryFragment
import com.example.finans.category.CategoriesBudgetsViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Date
import java.util.UUID


class BottomSheetAddBudgets : BottomSheetDialogFragment() {
    private lateinit var categoriesBudgetsViewModel: CategoriesBudgetsViewModel
    private lateinit var accountsBudgetsViewModel: AccountsBudgetsViewModel
    private var categories: ArrayList<String>? = null
    private var accounts: ArrayList<String>? = null
    private lateinit var selectTypeRu: String
    private lateinit var selectTypeEn: String
    private lateinit var startDate: Date
    private lateinit var endDate: Date

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }

        dialog?.setCancelable(false)
        return inflater.inflate(R.layout.fragment_bottom_sheet_add_budgets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        view.findViewById<TextView>(R.id.budgetsAddExit).setOnClickListener {
            dismiss()
        }

        val type = arrayOf(
            getString(R.string.weekly),
            getString(R.string.monthly),
            getString(R.string.annual)
        )

        var selectedOptionIndex = 1
        view.findViewById<TextView>(R.id.budgetsPeriodAddNameEdit).text =
            type[selectedOptionIndex]
        selectTypeEn = "Monthly"
        selectTypeRu = "Месячный"



        val calendar = Calendar.getInstance()

        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        startDate = calendar.time

        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        endDate = calendar.time

        val budgetsBalanceNameEdit = view.findViewById<EditText>(R.id.budgetsBalanceNameEdit)

        budgetsBalanceNameEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().contains(".")) {
                    val digitsAfterPoint = s.toString().substring(s.toString().indexOf(".") + 1)
                    if (digitsAfterPoint.length > 2) {
                        s?.replace(s.length - 1, s.length, "")
                    }
                }

                val decimalRegex =
                    "^(-)?\\\$?([1-9]{1}[0-9]{0,2}(\\,[0-9]{3})*(\\.[0-9]{0,2})?|[1-9]{1}[0-9]{0,}(\\.[0-9]{0,2})?|0(\\.[0-9]{0,2})?|(\\.[0-9]{1,2})?)\$"
                val match = s.toString().replace(",", "").matches(decimalRegex.toRegex())
                if (!match) {
                    s?.replace(s.length - 1, s.length, "")
                }
            }
        })

        budgetsBalanceNameEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(budgetsBalanceNameEdit.windowToken, 0)
                true
            } else {
                false
            }
        }


        view.findViewById<RelativeLayout>(R.id.budgetsPeriodAddBtn).setOnClickListener {

            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle(getString(R.string.budgetPeriod))
            alertDialogBuilder.setSingleChoiceItems(
                type,
                selectedOptionIndex
            ) { dialogInterface: DialogInterface, selectedIndex: Int ->
                selectedOptionIndex = selectedIndex
            }
            alertDialogBuilder.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->

                view.findViewById<TextView>(R.id.budgetsPeriodAddNameEdit).text =
                    type[selectedOptionIndex]


                when (selectedOptionIndex) {
                    0 -> {
                        selectTypeEn = "Weekly"
                        selectTypeRu = "Недельный"

                        calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                         startDate = calendar.time

                        calendar.add(Calendar.DAY_OF_WEEK, 6)
                        calendar.set(Calendar.HOUR_OF_DAY, 23)
                        calendar.set(Calendar.MINUTE, 59)
                         endDate = calendar.time
                    }

                    1 -> {
                        selectTypeEn = "Monthly"
                        selectTypeRu = "Месячный"

                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                         startDate = calendar.time

                        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                        calendar.set(Calendar.HOUR_OF_DAY, 23)
                        calendar.set(Calendar.MINUTE, 59)
                         endDate = calendar.time
                    }

                    2 -> {
                        selectTypeEn = "Annual"
                        selectTypeRu = "Годовой"

                        calendar.set(Calendar.DAY_OF_YEAR, 1)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                         startDate = calendar.time

                        calendar.set(Calendar.DAY_OF_YEAR, calendar.getActualMaximum(Calendar.DAY_OF_YEAR))
                        calendar.set(Calendar.HOUR_OF_DAY, 23)
                        calendar.set(Calendar.MINUTE, 59)
                         endDate = calendar.time

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

        view.findViewById<RelativeLayout>(R.id.budgetsCategoryAddBtn).setOnClickListener {
            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetCategoryFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetCategoryFragment.newInstance("budgets")
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetCategoryFragment"
                )
            }
        }

        view.findViewById<RelativeLayout>(R.id.budgetsAccountsAddBtn).setOnClickListener {
            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAccounts")
            if (existingFragment == null) {
                val newFragment = BottomSheetAccounts.newInstance("budgets")
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAccounts"
                )
            }
        }

        val budgetsCategoryAddNameEdit =
            view.findViewById<TextView>(R.id.budgetsCategoryAddNameEdit)

        categories = ArrayList()
        accounts = ArrayList()
        categoriesBudgetsViewModel =
            ViewModelProvider(requireActivity())[CategoriesBudgetsViewModel::class.java]
        categoriesBudgetsViewModel.getSelectedCategoriesBudgets().observe(this) { category ->

            if (category != null) {
                var str = ""
                for (item in category) {
                    if (item != category[category.size - 1]) {
                        str += item.nameRus + ", "
                    } else
                        str += item.nameRus
                    categories!!.add(item.name!!)
                }
                budgetsCategoryAddNameEdit.text = str
                categoriesBudgetsViewModel.clearCategoriesBudgets()
            }
        }
        val budgetsAccountsAddNameEdit =
            view.findViewById<TextView>(R.id.budgetsAccountsAddNameEdit)


        accountsBudgetsViewModel =
            ViewModelProvider(requireActivity())[AccountsBudgetsViewModel::class.java]
        accountsBudgetsViewModel.getSelectedAccountsBudgets().observe(this) { acc ->

            if (acc != null) {
                var str = ""
                for (item in acc) {
                    if (item != acc[acc.size - 1]) {
                        str += item.nameRus + ", "
                    } else
                        str += item.nameRus

                    accounts!!.add(item.name!!)
                }
                budgetsAccountsAddNameEdit.text = str
                accountsBudgetsViewModel.clearAccountsBudgets()
            }
        }



        view.findViewById<LinearLayout>(R.id.budgetsAddLinearLayout).setOnClickListener {
            val budgetsBalanceNameEdit = view.findViewById<EditText>(R.id.budgetsBalanceNameEdit)
            val addBudgetsNameEdit = view.findViewById<EditText>(R.id.addBudgetsNameEdit)

            if (budgetsBalanceNameEdit.text.toString()
                    .isNotEmpty() && addBudgetsNameEdit.text.toString()
                    .isNotEmpty()
            ) {


                val db = Firebase.firestore

                val settings = FirebaseFirestoreSettings.Builder()
                    .setPersistenceEnabled(true)
                    .build()

                db.firestoreSettings = settings


                val userID = db.collection("users").document(Firebase.auth.uid.toString())
                val id = UUID.randomUUID().toString()


                val ac = accounts
                val categ = categories

                val hashMap = hashMapOf<String, Any>(
                    "id" to id,
                    "name" to addBudgetsNameEdit.text.toString(),
                    "maxValue" to budgetsBalanceNameEdit.text.toString().toInt(),
                    "valueNow" to 0,
                    "accounts" to ac!!,
                    "categories" to categ!!,
                    "typeRu" to selectTypeRu,
                    "typeEn" to selectTypeEn,
                    "timeStart" to startDate,
                    "timeEnd" to endDate
                )
                accounts = null
                categories = null

                userID
                    .collection("budgets").document(id)
                    .set(hashMap)
                    .addOnSuccessListener { documentReference ->
                        dismiss()
                    }
                    .addOnFailureListener { }



                dismiss()
            } else {
                val builder = android.app.AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.error)
                builder.setMessage(R.string.fillInAllFields)
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setPositiveButton("OK", null)
                val dialog = builder.create()
                dialog.show()
            }


        }


    }

}