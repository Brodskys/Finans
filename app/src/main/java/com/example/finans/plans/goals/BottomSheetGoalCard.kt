package com.example.finans.plans.goals

import android.app.AlertDialog
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.finans.R
import com.example.finans.accounts.BottomSheetAccounts
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class BottomSheetGoalCard : BottomSheetDialogFragment() {
    private lateinit var goals: Goals
    private lateinit var valueEditText: EditText
    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_goal_card, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_goal_card, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        goals = arguments?.getParcelable("goals")!!

        valueEditText = view.findViewById(R.id.goalCardAddValueEditText)

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
                }
            })




        view.findViewById<TextView>(R.id.updateGoalCardTextView).setOnClickListener {
            val newFragment = BottomSheetGoal.newInstance(goals)
            newFragment.setTargetFragment(this@BottomSheetGoalCard, 0)

            newFragment.show(
                requireActivity().supportFragmentManager,
                "BottomSheetGoal"
            )
            dismiss()
        }



        view.findViewById<RelativeLayout>(R.id.goalCardRelativeLayout).setOnClickListener {

            if(valueEditText.text.isNotEmpty()){

                val money = valueEditText.text.toString().toDouble()

                FirebaseFirestore.getInstance().collection("users")
                    .document(Firebase.auth.uid.toString())
                    .collection("goals").document(goals.id!!)
                .update("valueNow", FieldValue.increment(money))
                .addOnSuccessListener {
                    dismiss()

                    FirebaseFirestore.getInstance().collection("users")
                        .document(Firebase.auth.uid.toString())
                        .collection("user").document("information")
                        .update("total_balance", FieldValue.increment(-money))
                        .addOnSuccessListener {}
                        .addOnFailureListener {}

                    FirebaseFirestore.getInstance().collection("users")
                        .document(Firebase.auth.uid.toString())
                        .collection("accounts").document(goals.account!!)
                        .update("balance", FieldValue.increment(-money))
                        .addOnSuccessListener {}
                        .addOnFailureListener {}

                }
                .addOnFailureListener {}
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


    companion object {
        fun newInstance(
            goals: Goals
        ): BottomSheetGoalCard {
            val args = Bundle()
            args.putParcelable("goals", goals)

            val fragment = BottomSheetGoalCard()
            fragment.arguments = args

            return fragment
        }
    }
}