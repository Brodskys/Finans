package com.example.finans.plans.budgets

import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import androidx.lifecycle.ViewModelProvider
import com.example.finans.R
import com.example.finans.accounts.AccountsBudgetsViewModel
import com.example.finans.accounts.BottomSheetAccounts
import com.example.finans.category.BottomSheetCategoryFragment
import com.example.finans.category.CategoriesBudgetsViewModel
import com.example.finans.language.languageInit
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Calendar
import java.util.Date

class BottomSheetBudgetUpdate : BottomSheetDialogFragment() {
    private lateinit var budgets: Budgets
    private lateinit var updateBudgetsNameEdit: EditText
    private lateinit var updateBudgetsBalanceNameEdit: EditText
    private lateinit var updateBudgetsPeriodAddBtn: RelativeLayout
    private lateinit var updateBudgetsPeriodNameEdit: TextView
    private lateinit var updateBudgetsCategoryBtn: RelativeLayout
    private lateinit var updateBudgetsCategoryIcon: ImageView
    private lateinit var updateBudgetsCategoryNameEdit: TextView
    private lateinit var updateBudgetsAccountsIcon: ImageView
    private lateinit var updateBudgetsAccountsNameEdit: TextView
    private lateinit var updateNotification80Switch: SwitchCompat
    private lateinit var updateOverrunsSwitch: SwitchCompat
    private lateinit var updateBudgetsAccountsBtn: RelativeLayout

    private lateinit var categoriesBudgetsViewModel: CategoriesBudgetsViewModel
    private lateinit var accountsBudgetsViewModel: AccountsBudgetsViewModel

    private var categories: ArrayList<String>? = null
    private var accounts: ArrayList<String>? = null

    private  var selectTypeRu: String? = null
    private  var selectTypeEn: String? = null
    private lateinit var startDate: Date
    private lateinit var endDate: Date
    private lateinit var sharedPreferencesTheme : SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }
        dialog?.setCancelable(false)

        sharedPreferencesTheme = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferencesTheme.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_budget_update, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_budget_update, container, false)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)
        budgets = arguments?.getParcelable("budgets")!!

        updateBudgetsNameEdit = view.findViewById(R.id.updateBudgetsNameEdit)
        updateBudgetsBalanceNameEdit = view.findViewById(R.id.updateBudgetsBalanceNameEdit)
        updateBudgetsPeriodAddBtn = view.findViewById(R.id.updateBudgetsPeriodAddBtn)
        updateBudgetsPeriodNameEdit = view.findViewById(R.id.updateBudgetsPeriodNameEdit)
        updateBudgetsCategoryBtn = view.findViewById(R.id.updateBudgetsCategoryBtn)
        updateBudgetsCategoryIcon = view.findViewById(R.id.updateBudgetsCategoryIcon)
        updateBudgetsCategoryNameEdit = view.findViewById(R.id.updateBudgetsCategoryNameEdit)
        updateBudgetsAccountsIcon = view.findViewById(R.id.updateBudgetsAccountsIcon)
        updateBudgetsAccountsNameEdit = view.findViewById(R.id.updateBudgetsAccountsNameEdit)
        updateNotification80Switch = view.findViewById(R.id.updateNotification80Switch)
        updateOverrunsSwitch = view.findViewById(R.id.updateOverrunsSwitch)
        updateBudgetsAccountsBtn = view.findViewById(R.id.updateBudgetsAccountsBtn)

        val type = arrayOf(
            getString(R.string.weekly),
            getString(R.string.monthly),
            getString(R.string.annual)
        )

        var selectedOptionIndex =  if(type.indexOf(budgets.typeEn) == -1)  type.indexOf(budgets.typeRu) else type.indexOf(budgets.typeEn)

        val calendar = Calendar.getInstance()

        updateBudgetsNameEdit.setText(budgets.name)
        updateBudgetsBalanceNameEdit.setText(budgets.maxValue.toString())

        if (languageInit(requireActivity())) {
            updateBudgetsPeriodNameEdit.text = budgets.typeRu
            selectTypeRu = budgets.typeRu!!
        } else {
            updateBudgetsPeriodNameEdit.text = budgets.typeEn
            selectTypeEn = budgets.typeEn!!
        }

        when (budgets.typeEn) {
            "Weekly" -> {

                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                startDate = calendar.time

                calendar.add(Calendar.DAY_OF_WEEK, 6)
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                endDate = calendar.time
            }

            "Monthly" -> {


                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                startDate = calendar.time

                calendar.set(
                    Calendar.DAY_OF_MONTH,
                    calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                )
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                endDate = calendar.time
            }

            "Annual" -> {

                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                startDate = calendar.time

                calendar.set(
                    Calendar.DAY_OF_YEAR,
                    calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
                )
                calendar.set(Calendar.HOUR_OF_DAY, 23)
                calendar.set(Calendar.MINUTE, 59)
                endDate = calendar.time

            }
        }

        updateBudgetsBalanceNameEdit.addTextChangedListener(object : TextWatcher {
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
        updateBudgetsBalanceNameEdit.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(
                    updateBudgetsBalanceNameEdit.windowToken,
                    0
                )
                true
            } else {
                false
            }
        }



        updateBudgetsPeriodAddBtn.setOnClickListener {

            val alertDialogBuilder = AlertDialog.Builder(requireContext())
            alertDialogBuilder.setTitle(getString(R.string.budgetPeriod))
            alertDialogBuilder.setSingleChoiceItems(
                type,
                selectedOptionIndex
            ) { dialogInterface: DialogInterface, selectedIndex: Int ->
                selectedOptionIndex = selectedIndex
            }
            alertDialogBuilder.setPositiveButton("OK") { dialogInterface: DialogInterface, i: Int ->

                updateBudgetsPeriodNameEdit.text =
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

                        calendar.set(
                            Calendar.DAY_OF_MONTH,
                            calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        )
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

                        calendar.set(
                            Calendar.DAY_OF_YEAR,
                            calendar.getActualMaximum(Calendar.DAY_OF_YEAR)
                        )
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

        updateBudgetsCategoryBtn.setOnClickListener {
            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetCategoryFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetCategoryFragment.newInstance("budgets")
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetCategoryFragment"
                )
            }
            updateBudgetsCategoryNameEdit.text = getString(R.string.all)
            categories = null
        }

        categories = budgets.categories
        accounts = budgets.accounts

        categoriesBudgetsViewModel =
            ViewModelProvider(requireActivity())[CategoriesBudgetsViewModel::class.java]
        categoriesBudgetsViewModel.getSelectedCategoriesBudgets().observe(this) { category ->
            categories = ArrayList()
            if (category != null) {
                var str = ""
                for (item in category) {
                    str += if (item != category[category.size - 1]) {
                        item.nameRus + ", "
                    } else
                        item.nameRus
                    categories!!.add(item.name!!)

                }
                updateBudgetsCategoryNameEdit.text = str
                categoriesBudgetsViewModel.clearCategoriesBudgets()
            }
        }

        updateBudgetsCategoryNameEdit.text = if(budgets.categories?.size!! <1) getString(R.string.all) else budgets.categories?.joinToString(", ")

        updateBudgetsAccountsNameEdit.text = if(budgets.accounts?.size!! <1) getString(R.string.all) else budgets.accounts?.joinToString(", ")


        updateBudgetsAccountsBtn.setOnClickListener {
            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAccounts")
            if (existingFragment == null) {
                val newFragment = BottomSheetAccounts.newInstance("budgets")
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAccounts"
                )
            }
            updateBudgetsAccountsNameEdit.text = getString(R.string.all)
            accounts = null
        }

        accountsBudgetsViewModel =
            ViewModelProvider(requireActivity())[AccountsBudgetsViewModel::class.java]
        accountsBudgetsViewModel.getSelectedAccountsBudgets().observe(this) { acc ->
            accounts = ArrayList()
            if (acc != null) {
                var str = ""
                for (item in acc) {
                    str += if (item != acc[acc.size - 1]) {
                        item.nameRus + ", "
                    } else
                        item.nameRus

                    accounts!!.add(item.name!!)
                }
                updateBudgetsAccountsNameEdit.text = str
                accountsBudgetsViewModel.clearAccountsBudgets()
            }
        }

        updateNotification80Switch.isChecked = budgets.notification80Is.toBoolean()
        updateOverrunsSwitch.isChecked = budgets.notificationOverrunsIs.toBoolean()

        val db = Firebase.firestore

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        db.firestoreSettings = settings

        val userID = db.collection("users").document(Firebase.auth.uid.toString())

        view.findViewById<LinearLayout>(R.id.updateBudgetsLinearLayout).setOnClickListener {

            if (updateBudgetsNameEdit.text.toString()
                    .isNotEmpty() && updateBudgetsBalanceNameEdit.text.toString()
                    .isNotEmpty()
            ) {

                val notification80 = (updateNotification80Switch.isChecked).toString()
                val notificationOverruns = (updateOverrunsSwitch.isChecked).toString()

                selectTypeRu = selectTypeRu ?: budgets.typeRu!!
                selectTypeEn = selectTypeEn ?: budgets.typeEn!!

                val hashMap = hashMapOf<String, Any>(
                    "name" to updateBudgetsNameEdit.text.toString(),
                    "maxValue" to updateBudgetsBalanceNameEdit.text.toString().toDouble(),
                    "accounts" to accounts!!,
                    "categories" to categories!!,
                    "typeRu" to selectTypeRu!!,
                    "typeEn" to selectTypeEn!!,
                    "timeStart" to startDate,
                    "timeEnd" to endDate,
                    "notification80Is" to notification80,
                    "notificationOverrunsIs" to notificationOverruns
                )
                accounts = null
                categories = null

                userID
                    .collection("budgets").document(budgets.id!!)
                    .update(hashMap)
                    .addOnSuccessListener { documentReference ->

                        if(budgets.accounts != accounts || budgets.categories != categories){
                            val budgetsCollectionRef =
                                Firebase.firestore.collection("users")
                                    .document(Firebase.auth.uid.toString())
                                    .collection("budgets")

                            budgetsCollectionRef.document(budgets.id!!)
                                .update(
                                    "valueNow", 0
                                )
                                .addOnSuccessListener {
                                    budgetsCollectionRef.document(budgets.id!!)
                                        .collection("operation")
                                        .get()
                                        .addOnSuccessListener { querySnapshot ->
                                            val batch = Firebase.firestore.batch()

                                            for (document in querySnapshot.documents) {
                                                val operationRef = document.reference
                                                batch.delete(operationRef)
                                            }

                                            batch.commit()
                                                .addOnSuccessListener {
                                                }
                                                .addOnFailureListener { e ->
                                                }
                                        }
                                        .addOnFailureListener { e ->
                                        }
                                }
                        }

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

        view.findViewById<TextView>(R.id.budgetsUpdateDelete).setOnClickListener {

            userID
                .collection("budgets").document(budgets.id!!)
                        .delete()
                        .addOnSuccessListener {
                            dismiss()
                        }


        }

        view.findViewById<TextView>(R.id.budgetsUpdateExit).setOnClickListener {
            dismiss()
        }

    }

    companion object {

        fun newInstance(
            budgets: Budgets
        ): BottomSheetBudgetUpdate {
            val args = Bundle()
            args.putParcelable("budgets", budgets)

            val fragment = BottomSheetBudgetUpdate()
            fragment.arguments = args

            return fragment
        }
    }
}