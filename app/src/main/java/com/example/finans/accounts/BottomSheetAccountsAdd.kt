package com.example.finans.accounts

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.finans.R
import com.example.finans.category.updateCategory.BottomSheetUpdateIcon
import com.example.finans.category.updateCategory.IconViewModel
import com.example.finans.сurrency.CurrencyViewModel
import com.example.finans.сurrency.BottomSheetCurrencyFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso


class BottomSheetAccountsAdd : BottomSheetDialogFragment() {
    private lateinit var currencyViewModel: CurrencyViewModel
    private lateinit var iconViewModel: IconViewModel
    private lateinit var gsReference: StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {
            it.behavior.peekHeight  = R.style.AppBottomSheetDialogTheme
        }
        return inflater.inflate(R.layout.fragment_bottom_sheet_accounts_add, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val amount = view.findViewById<EditText>(R.id.accountsBalanceNameEdit)

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


        amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

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

        view.findViewById<RelativeLayout>(R.id.accountsCurrencyAddBtn).setOnClickListener {
            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetCurrencyFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetCurrencyFragment()

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetCurrencyFragment"
                )
            }
        }
        val accountsCurrencyNameEdit = view.findViewById<TextView>(R.id.accountsCurrencyNameEdit)

        currencyViewModel = ViewModelProvider(requireActivity())[CurrencyViewModel::class.java]
        currencyViewModel.getSelectedCurrency().observe(this) { currency ->
            if(currency != null)
                accountsCurrencyNameEdit.text = currency
        }

        val storage = Firebase.storage
        var icon = ""

        iconViewModel = ViewModelProvider(requireActivity())[IconViewModel::class.java]
        iconViewModel.getSelectedIcon().observe(this) { ic ->
            if (ic != null) {
                if (ic.type == "addAccounts") {
                    icon = ic.icon
                    gsReference = storage.getReferenceFromUrl(ic.icon)
                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri)
                            .into(view.findViewById<ImageView>(R.id.addAccountsEditIcon))
                    }.addOnFailureListener {
                        Picasso.get().load(R.drawable.wallet)
                            .into(view.findViewById<ImageView>(R.id.addAccountsEditIcon))
                    }

                    iconViewModel.clearIcon()
                }
            }
        }


        view.findViewById<ImageView>(R.id.accountsUpdateIcon).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetUpdateIcon") as? BottomSheetUpdateIcon

            if (bottomSheetFragment == null) {
                val newFragment =
                    BottomSheetUpdateIcon.newInstance("addAccounts")
                newFragment.setTargetFragment(this@BottomSheetAccountsAdd, 0)

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetUpdateIcon"
                )
            }
        }

        val addAccountsNameEdit = view.findViewById<EditText>(R.id.addAccountsNameEdit)

        view.findViewById<TextView>(R.id.newAccountsDone).setOnClickListener {
            newAccounts(icon,addAccountsNameEdit.text.toString(), amount.text.toString(), accountsCurrencyNameEdit.text.toString())
        }

        view.findViewById<RelativeLayout>(R.id.accountsAddRelativeLayout).setOnClickListener {
            newAccounts(icon, addAccountsNameEdit.text.toString(), amount.text.toString(), accountsCurrencyNameEdit.text.toString())
        }

        view.findViewById<TextView>(R.id.accountsAddExit).setOnClickListener {
            dismiss()
        }

    }

    private fun newAccounts(icon: String, accountsName: String, amount: String, accountsCurrenc: String) {
        if(icon != "" && accountsName!= "" && amount!= "" && accountsCurrenc!= ""){
            val accountsMap = hashMapOf<String, Any>(
                "name" to accountsName,
                "balance" to amount.toDouble(),
                "nameRus" to accountsName,
                "nameEng" to accountsName,
                "icon" to icon,
                "currency" to accountsCurrenc,
                "new" to "true",
            )


            FirebaseFirestore.getInstance().collection("users").document(Firebase.auth.uid.toString()).collection("accounts").document(accountsName).set(accountsMap, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("Add", "Added document")
                    dismiss()
                }
                .addOnFailureListener { exception ->
                    Log.d("Add", "Error adding document $exception")

                }
        }
        else{
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