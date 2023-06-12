package com.example.finans.accounts

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.finans.R
import com.example.finans.category.Category
import com.example.finans.category.subcategory.BottomSheetSubcategoryFragment
import com.example.finans.category.updateCategory.BottomSheetUpdateIcon
import com.example.finans.category.updateCategory.IconViewModel
import com.example.finans.other.deletionWarning
import com.example.finans.сurrency.BottomSheetCurrencyFragment
import com.example.finans.сurrency.CurrencyViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class BottomSheetAccount : BottomSheetDialogFragment() {
    private lateinit var gsReference: StorageReference
    private lateinit var currencyViewModel: CurrencyViewModel
    private lateinit var iconViewModel: IconViewModel
    private lateinit var accountChangeDeleteTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {
            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }
        return inflater.inflate(R.layout.fragment_bottom_sheet_account, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pref =
            requireActivity().getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)
        val accounts = pref.getString("accounts", "")
        val userId = FirebaseFirestore.getInstance().collection("users")
            .document(Firebase.auth.uid.toString())
        val userRef = userId.collection("accounts").document(accounts!!)
        val storage = Firebase.storage

        var id = ""
        val updateAccountNameEdit = view.findViewById<EditText>(R.id.updateAccountNameEdit)
        val amount = view.findViewById<EditText>(R.id.updateAccountBalanceNameEdit)
        val currencyEdit = view.findViewById<TextView>(R.id.updateAccountCurrencyNameEdit)
        accountChangeDeleteTextView = view.findViewById<TextView>(R.id.accountChangeDeleteTextView)
        var ic = ""

        userRef.get()
            .addOnSuccessListener { documentSnapshot ->

                val new: String? = documentSnapshot.getString("new")

                if (new != null) {
                    accountChangeDeleteTextView.text = getString(R.string.delete)
                }

                val balance = documentSnapshot.getLong("balance")
                val currency = documentSnapshot.getString("currency")
                val icon = documentSnapshot.getString("icon")
                ic = icon!!
                id = documentSnapshot.getString("name")!!
                val nameEng = documentSnapshot.getString("nameEng")
                val nameRus = documentSnapshot.getString("nameRus")

                gsReference = storage.getReferenceFromUrl(icon)
                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri)
                        .into(view.findViewById<ImageView>(R.id.updateAccountEditIcon))
                }.addOnFailureListener {
                    Picasso.get().load(R.drawable.wallet)
                        .into(view.findViewById<ImageView>(R.id.updateAccountEditIcon))
                }

                val sharedPref = pref.getString("locale", "")
                if (sharedPref == "ru") {
                    updateAccountNameEdit.setText(nameRus)
                } else {
                    updateAccountNameEdit.setText(nameEng)
                }

                amount.setText(balance.toString())
                currencyEdit.text = currency

            }

        currencyViewModel = ViewModelProvider(requireActivity())[CurrencyViewModel::class.java]
        currencyViewModel.getSelectedCurrency().observe(this) { currency ->
            if (currency != null) {
                view.findViewById<TextView>(R.id.updateAccountCurrencyNameEdit).text =
                    currency
                accountChangeDeleteTextView.text = getString(R.string.update)
                currencyViewModel.clearCurrency()
            }
        }

        iconViewModel = ViewModelProvider(requireActivity())[IconViewModel::class.java]
        iconViewModel.getSelectedIcon().observe(this) { icon ->
            if (icon != null) {
                if (icon.type == "addAccounts") {

                    gsReference = storage.getReferenceFromUrl(icon.icon)
                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri)
                            .into(view.findViewById<ImageView>(R.id.updateAccountEditIcon))
                        accountChangeDeleteTextView.text = getString(R.string.update)

                    }.addOnFailureListener {
                        Picasso.get().load(ic)
                            .into(view.findViewById<ImageView>(R.id.updateAccountEditIcon))
                    }
                    ic = icon.icon
                    iconViewModel.clearIcon()
                }
            }
        }

        var update = false

        updateAccountNameEdit.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (update) {
                    accountChangeDeleteTextView.text = getString(R.string.update)
                } else
                    update = true
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {


            }
        })

        var update2 = false

        amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                if (update2) {
                    accountChangeDeleteTextView.text = getString(R.string.update)
                } else
                    update2 = true
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
                    amount.error = "Incorrect input format"
                    s?.replace(s.length - 1, s.length, "")
                }
            }
        })

        amount.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(amount.windowToken, 0)
                true
            } else {
                false
            }
        }

        view.findViewById<ImageView>(R.id.updateAccountUpdateIcon).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetUpdateIcon") as? BottomSheetUpdateIcon

            if (bottomSheetFragment == null) {
                val newFragment =
                    BottomSheetUpdateIcon.newInstance("addAccounts")
                newFragment.setTargetFragment(this@BottomSheetAccount, 0)

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetUpdateIcon"
                )
            }
        }

        view.findViewById<RelativeLayout>(R.id.accountUpdateCurrencyBtn).setOnClickListener {
            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetCurrencyFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetCurrencyFragment.newInstance("changeAc")

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetCurrencyFragment"
                )
            }
        }


        view.findViewById<TextView>(R.id.accountUpdateTextViewExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.accountUpdateTextViewDone).setOnClickListener {
            newAccounts(
                id,
                ic,
                updateAccountNameEdit.text.toString(),
                amount.text.toString(),
                currencyEdit.text.toString()
            )
        }

        view.findViewById<LinearLayout>(R.id.accountLinearLayout).setOnClickListener {
            newAccounts(
                id,
                ic,
                updateAccountNameEdit.text.toString(),
                amount.text.toString(),
                currencyEdit.text.toString()
            )
        }

    }

    private fun newAccounts(
        id: String,
        icon: String,
        accountsName: String,
        amount: String,
        accountsCurrenc: String
    ) {
        if (accountChangeDeleteTextView.text == getString(R.string.delete)) {
            deletionWarning(requireContext()) { result ->

                if (result) {
                    val documentRef = FirebaseFirestore.getInstance().collection("users")
                        .document(Firebase.auth.uid.toString()).collection("accounts")
                        .document(id)

                    documentRef.delete()
                        .addOnSuccessListener {
                            println("Документ успешно удален.")

                            val sharedPref = requireActivity().getSharedPreferences(
                                "Settings",
                                Context.MODE_PRIVATE
                            )

                            val docRef = FirebaseFirestore.getInstance().collection("users")
                                .document(Firebase.auth.uid.toString()).collection("user")
                                .document("information")

                            val accounts = hashMapOf<String, Any>(
                                "accounts" to "cash"
                            )

                            val close = requireActivity()

                            docRef.update(accounts)
                                .addOnSuccessListener {

                                    val editor = sharedPref?.edit()
                                    editor!!.putString("accounts", "cash")
                                    editor.apply()

                                    close.recreate()
                                    dismiss()
                                }
                                .addOnFailureListener { e ->
                                }

                        }
                        .addOnFailureListener { exception ->
                            println("Ошибка при удалении документа: $exception")
                        }

                }

            }
        } else
            if (icon != "" && accountsName != "" && amount != "" && accountsCurrenc != "") {

                val accountsMap = hashMapOf<String, Any>(
                    "balance" to amount.toDouble(),
                    "nameRus" to accountsName,
                    "nameEng" to accountsName,
                    "icon" to icon,
                    "currency" to accountsCurrenc
                )


                val documentRef = FirebaseFirestore.getInstance().collection("users")
                    .document(Firebase.auth.uid.toString()).collection("accounts")
                    .document(id)
                documentRef.update(accountsMap as Map<String, Any>)
                    .addOnSuccessListener {
                        requireActivity().recreate()
                        dismiss()
                    }
                    .addOnFailureListener { exception -> }


            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.error)
                builder.setMessage(R.string.fillInAllFields)
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setPositiveButton("OK", null)
                val dialog = builder.create()
                dialog.show()
            }


    }
}