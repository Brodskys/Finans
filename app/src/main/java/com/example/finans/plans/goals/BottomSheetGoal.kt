package com.example.finans.plans.goals

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.finans.R
import com.example.finans.accounts.Accounts
import com.example.finans.accounts.AccountsViewModel
import com.example.finans.accounts.BottomSheetAccounts
import com.example.finans.authorization.authWithFacebook.AuthorizationPresenterFacebook
import com.example.finans.category.Category
import com.example.finans.category.subcategory.BottomSheetSubcategoryFragment
import com.example.finans.category.updateCategory.BottomSheetUpdateIcon
import com.example.finans.category.updateCategory.IconViewModel
import com.example.finans.other.deletionWarning
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class BottomSheetGoal : BottomSheetDialogFragment() {
    private lateinit var goals: Goals
    private lateinit var db: FirebaseFirestore
    private lateinit var accountsViewModel: AccountsViewModel
    private  var name1: String = ""
    private  var currency: String = ""
    private  var icon: String = ""
    private var ic: String = ""
    private lateinit var iconViewModel: IconViewModel
    private lateinit var sharedPreferences : SharedPreferences

    private  var isFull: Boolean = false

    private lateinit var iconImageView: ImageView
    private lateinit var nameEditText: EditText
    private lateinit var valueEditText: EditText
    private lateinit var currency1: TextView
    private lateinit var valueNowEditText: EditText
    private lateinit var currency2: TextView
    private lateinit var iconAccountImageView: ImageView
    private lateinit var nameAccountTextView: TextView
    private lateinit var noteTextView: TextView


    companion object {
        fun newInstance(
            goals: Goals
        ): BottomSheetGoal {
            val args = Bundle()
            args.putParcelable("goals", goals)

            val fragment = BottomSheetGoal()
            fragment.arguments = args

            return fragment
        }
    }

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

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_goal, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_goal, container, false)
        }
    }

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        goals = arguments?.getParcelable("goals")!!
        db = Firebase.firestore


        isFull = goals.valueNow!! >= goals.value!!

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        db.firestoreSettings = settings

        name1 = goals.account!!
        currency = goals.currency!!
        icon = goals.icon!!



        iconImageView = view.findViewById(R.id.goalIconImageView)
        nameEditText = view.findViewById(R.id.goalNameEditText)

        valueEditText = view.findViewById(R.id.goalsValueEditText)
        currency1 = view.findViewById(R.id.goalCurrencyTextView)

        valueNowEditText = view.findViewById(R.id.goalsValueEditText2)
        currency2 = view.findViewById(R.id.goalCurrencyTextView2)

        iconAccountImageView = view.findViewById(R.id.goalAccountIcon)
        nameAccountTextView = view.findViewById(R.id.goalAccountName)

        noteTextView = view.findViewById(R.id.goalNoteTextView)

        isFull = goals.valueNow!! >= goals.value!!

        if(isFull) {
            iconImageView.isEnabled = false
            nameEditText.isEnabled = false
            valueEditText.isEnabled = false
            valueNowEditText.isEnabled = false
            view.findViewById<RelativeLayout>(R.id.goalAccountRelativeLayout).isEnabled = false
            view.findViewById<RelativeLayout>(R.id.goalNoteRelativeLayout).isEnabled = false
        }


        if (goals.icon != "") {
            val gsReference = Firebase.storage.getReferenceFromUrl(goals.icon!!)
            gsReference.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri)
                    .into(iconImageView)
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.question)
                    .into(iconImageView)
            }
        }

        if (goals.accountIcon != "") {
            val gsReference = Firebase.storage.getReferenceFromUrl(goals.accountIcon!!)
            gsReference.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri)
                    .into(iconAccountImageView)
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.wallet)
                    .into(iconAccountImageView)
            }
        }

        nameEditText.setText(goals.name)

        valueEditText.setText(goals.value.toString())

        currency1.text = goals.currency.toString()

        valueNowEditText.setText(goals.valueNow.toString())

        currency2.text = goals.currency.toString()

        nameAccountTextView.text = goals.account.toString()

        noteTextView.text = goals.note.toString()


        nameEditText
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

                override fun afterTextChanged(s: Editable?) {
                    if(!isFull) isVisibility()
                }
            })


        valueEditText
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
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
                    if(!isFull) isVisibility()
                }
            })

        valueNowEditText
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
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
                    if(!isFull) isVisibility()
                }
            })

        val s = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val locale = s.getString("locale", "")

        view.findViewById<RelativeLayout>(R.id.goalAccountRelativeLayout).setOnClickListener {
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

        accountsViewModel = ViewModelProvider(requireActivity())[AccountsViewModel::class.java]
        accountsViewModel.getSelectedAccounts().observe(this) { acc ->
            if (acc != null) {
                if(!isFull) isVisibility()

                if (locale == "ru") {

                    nameAccountTextView.text = acc.nameRus

                } else {
                    nameAccountTextView.text = acc.nameEng
                }

                val gsReference = Firebase.storage.getReferenceFromUrl(acc.icon!!)

                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString())
                        .into(iconAccountImageView)
                }.addOnFailureListener {
                    Picasso.get().load(R.drawable.coins)
                        .into(iconAccountImageView)
                }
                currency2.text = acc.currency
                name1 = acc.name!!
                currency = acc.currency!!
                icon = acc.icon!!
                accountsViewModel.clearAccounts()
            }
        }


        iconViewModel = ViewModelProvider(requireActivity())[IconViewModel::class.java]
        iconViewModel.getSelectedIcon().observe(this) { icon ->
            if (icon != null) {
                if (icon.type == "goals") {
                    if(!isFull) isVisibility()
                    val storage = Firebase.storage
                    val gsReference = storage.getReferenceFromUrl(icon.icon)
                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri)
                            .into(iconImageView)

                    }.addOnFailureListener {

                    }
                    ic = icon.icon
                    iconViewModel.clearIcon()
                }
            }
        }

        view.findViewById<ImageView>(R.id.goalIconImageView).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetUpdateIcon") as? BottomSheetUpdateIcon

            if (bottomSheetFragment == null) {
                val newFragment =
                    BottomSheetUpdateIcon.newInstance("goals")
                newFragment.setTargetFragment(this@BottomSheetGoal, 0)

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetUpdateIcon"
                )
            }
        }


        view.findViewById<RelativeLayout>(R.id.goalNoteRelativeLayout).setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.note)

            val input = EditText(requireContext())
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

            if (noteTextView.text.isNotEmpty()) {
                input.setText(noteTextView.text)
                input.setSelection(noteTextView.text.length)


            }

            builder.setView(input)

            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                val noteText = input.text.toString().trim()

                noteTextView.text = noteText

                dialog.dismiss()

                if(!isFull) isVisibility()
            }
            builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }

        view.findViewById<TextView>(R.id.goalExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.goalDeleteDone).setOnClickListener {
            deletionWarning(requireContext()) { result ->

                if (result) {

                    val docRef = db.collection("users").document(Firebase.auth.uid.toString())
                        .collection("goals").document(goals.id!!)

                    docRef.delete()
                        .addOnSuccessListener {
                            Log.d(
                                AuthorizationPresenterFacebook.TAG,
                                "DocumentSnapshot successfully deleted!"
                            )
                            dismiss()
                        }
                        .addOnFailureListener { e ->
                            Log.w(AuthorizationPresenterFacebook.TAG, "Error deleting document", e)
                        }


                }
            }
        }

        view.findViewById<LinearLayout>(R.id.goalLinearLayout).setOnClickListener {

            if (nameEditText.text.toString() != "" && valueEditText.text.toString() != ""
                && valueNowEditText.text.toString() != "") {

                val hashMap = hashMapOf<String, Any>(
                    "name" to nameEditText.text.toString(),
                    "value" to valueEditText.text.toString().toDouble(),
                    "valueNow" to valueNowEditText.text.toString().toDouble(),
                    "account" to name1,
                    "currency" to currency,
                    "note" to noteTextView.text,
                    "icon" to ic,
                    "accountIcon" to icon
                )

                val documentRef = FirebaseFirestore.getInstance().collection("users")
                    .document(Firebase.auth.uid.toString())
                    .collection("goals").document(goals.id!!)
                documentRef.update(hashMap as Map<String, Any>)
                    .addOnSuccessListener {

                        FirebaseFirestore.getInstance().collection("users")
                            .document(Firebase.auth.uid.toString())
                            .collection("user").document("information")
                            .update("total_balance", FieldValue.increment(goals.valueNow!!))
                            .addOnSuccessListener {
                                FirebaseFirestore.getInstance().collection("users")
                                    .document(Firebase.auth.uid.toString())
                                    .collection("user").document("information")
                                    .update("total_balance", FieldValue.increment(-valueNowEditText.text.toString().toDouble()))
                                    .addOnSuccessListener {}
                                    .addOnFailureListener {}
                            }
                            .addOnFailureListener {}

                        FirebaseFirestore.getInstance().collection("users")
                            .document(Firebase.auth.uid.toString())
                            .collection("accounts").document(goals.account!!)
                            .update("balance", FieldValue.increment(goals.valueNow!!))
                            .addOnSuccessListener {
                                FirebaseFirestore.getInstance().collection("users")
                                    .document(Firebase.auth.uid.toString())
                                    .collection("accounts").document(goals.account!!)
                                    .update("balance", FieldValue.increment(-valueNowEditText.text.toString().toDouble()))
                                    .addOnSuccessListener {}
                                    .addOnFailureListener {}
                            }
                            .addOnFailureListener {}


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

    fun isVisibility(){
        view?.findViewById<LinearLayout>(R.id.goalLinearLayout)?.visibility = View.VISIBLE
    }

}