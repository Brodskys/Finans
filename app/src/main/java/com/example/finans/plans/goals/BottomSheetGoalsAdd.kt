package com.example.finans.plans.goals

import android.app.AlertDialog
import android.content.Context
import android.graphics.Paint
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import com.example.finans.R
import com.example.finans.accounts.AccountsViewModel
import com.example.finans.accounts.BottomSheetAccounts
import com.example.finans.category.updateCategory.BottomSheetUpdateIcon
import com.example.finans.category.updateCategory.IconViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.util.UUID


class BottomSheetGoalsAdd : BottomSheetDialogFragment() {
    private lateinit var accountsViewModel: AccountsViewModel
    private lateinit var iconViewModel: IconViewModel

    private lateinit var amount: EditText
    private lateinit var name: EditText

    private  var name1: String = ""
    private  var currency: String = ""
    private  var icon1: String = ""
    private  var icon2: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight  = R.style.AppBottomSheetDialogTheme
        }

        dialog?.setCancelable(false)

        return inflater.inflate(R.layout.fragment_bottom_sheet_goals_add, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        view.findViewById<TextView>(R.id.goalsAddExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.goalsAddDone).setOnClickListener {
            addGoals()
        }

        view.findViewById<LinearLayout>(R.id.addNawGoalsLinearLayout).setOnClickListener {
            addGoals()
        }

        amount = view.findViewById(R.id.goalsValueEditText)
        name = view.findViewById(R.id.goalsNameEditText)

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
                    s?.replace(s.length - 1, s.length, "")
                }
            }
        })

        val currencyBtn = view.findViewById<TextView>(R.id.goalsCurrencyTextView)
        currencyBtn.paintFlags = currencyBtn.paintFlags or Paint.UNDERLINE_TEXT_FLAG

        view.findViewById<RelativeLayout>(R.id.goalsAccountRelativeLayout).setOnClickListener {
            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAccounts")
            if (existingFragment == null) {
                val newFragment = BottomSheetAccounts.newInstance("newOper")
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAccounts"
                )
            }
        }

        val s = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val locale = s.getString("locale", "")

        val account = view.findViewById<TextView>(R.id.goalsAccountName)
        val accountIcon = view.findViewById<ImageView>(R.id.goalsAccountIcon)


        accountsViewModel = ViewModelProvider(requireActivity())[AccountsViewModel::class.java]
        accountsViewModel.getSelectedAccounts().observe(this) { acc ->
            if(acc!=null) {

                if (locale == "ru"){

                    account.text = acc.nameRus

                } else {
                    account.text = acc.nameEng
                }

                val gsReference = Firebase.storage.getReferenceFromUrl(acc.icon!!)

                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString()).into(accountIcon)
                }.addOnFailureListener {
                    Picasso.get().load(R.drawable.coins).into(accountIcon)
                }
                currencyBtn.text = acc.currency

                name1 = acc.name!!
                currency = acc.currency!!
                icon2 = acc.icon!!

                accountsViewModel.clearAccounts()
            }
        }


        view.findViewById<RelativeLayout>(R.id.goalsNoteRelativeLayout).setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.note)

            val input = EditText(requireContext())
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

            if (view.findViewById<TextView>(R.id.goalsNoteTextView).text.isNotEmpty()) {
                input.setText(view.findViewById<TextView>(R.id.goalsNoteTextView).text)
                input.setSelection(view.findViewById<TextView>(R.id.goalsNoteTextView).text.length)
            }

            builder.setView(input)

            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                val noteText = input.text.toString().trim()

                view.findViewById<TextView>(R.id.goalsNoteTextView).text = noteText

                dialog.dismiss()
            }
            builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }


        iconViewModel = ViewModelProvider(requireActivity())[IconViewModel::class.java]
        iconViewModel.getSelectedIcon().observe(this) { icon ->
            if (icon != null) {
                if (icon.type == "addGoals") {
                    val storage = Firebase.storage
                   val gsReference = storage.getReferenceFromUrl(icon.icon)
                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri)
                            .into(view.findViewById<ImageView>(R.id.goalsIconImageView))

                    }.addOnFailureListener {

                    }
                    icon1 = icon.icon
                    iconViewModel.clearIcon()
                }
            }
        }

        view.findViewById<ImageView>(R.id.goalsIconImageView).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetUpdateIcon") as? BottomSheetUpdateIcon

            if (bottomSheetFragment == null) {
                val newFragment =
                    BottomSheetUpdateIcon.newInstance("addGoals")
                newFragment.setTargetFragment(this@BottomSheetGoalsAdd, 0)

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetUpdateIcon"
                )
            }
        }

    }



    private fun addGoals() {
        val  db = Firebase.firestore

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        db.firestoreSettings = settings


        val userID = db.collection("users").document(Firebase.auth.uid.toString())

        val note = requireView().findViewById<TextView>(R.id.goalsNoteTextView).text

        val id = UUID.randomUUID().toString()

        val hashMap = hashMapOf<String, Any>(
            "id" to id,
            "name" to name.text.toString(),
            "value" to amount.text.toString().toInt(),
            "valueNow" to 0,
            "account" to name1,
            "currency" to currency,
            "note" to note,
            "icon" to icon1,
            "accountIcon" to icon2
        )

        userID
            .collection("goals").document(id)
            .set(hashMap)
            .addOnSuccessListener { documentReference ->
                dismiss()
            }
            .addOnFailureListener {  }
    }
}