package com.example.finans.plans.budgets

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import com.example.finans.R
import com.example.finans.category.Category
import com.example.finans.category.subcategory.BottomSheetSubcategoryFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetBudget : BottomSheetDialogFragment() {
   private lateinit var budgets: Budgets

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }

        return inflater.inflate(R.layout.fragment_bottom_sheet_budget, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        budgets = arguments?.getParcelable("budgets")!!


        view.findViewById<EditText>(R.id.budgetsBalanceNameEdit).setText(budgets.valueNow.toString())

        view.findViewById<TextView>(R.id.budgetExit).setOnClickListener {
            dismiss()
        }

    }

    companion object {
        fun newInstance(
            budgets: Budgets
        ): BottomSheetBudget {
            val args = Bundle()
            args.putParcelable("budgets", budgets)

            val fragment = BottomSheetBudget()
            fragment.arguments = args

            return fragment
        }
    }

}