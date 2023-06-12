package com.example.finans.plans.calculator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.finans.R
import com.example.finans.operation.BottomSheetNewOperationFragment
import com.example.finans.plans.calcula.BottomSheetLoanCalculator
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetCalculatorChoice : BottomSheetDialogFragment() {



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight  = R.style.AppBottomSheetDialogTheme
        }

        dialog?.setCancelable(false)


        return inflater.inflate(R.layout.fragment_bottom_sheet_calculator_choice, container, false)
    }



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)


        view.findViewById<TextView>(R.id.plansChoiceExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<LinearLayout>(R.id.loanCalculatorLinearLayout).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetLoanCalculator") as? BottomSheetLoanCalculator
            if(bottomSheetFragment == null) {
                BottomSheetLoanCalculator().show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetLoanCalculator"
                )
            }
        }


        view.findViewById<LinearLayout>(R.id.investmentCalculatorLinearLayout).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetInvestmentCalculator") as? BottomSheetInvestmentCalculator
            if(bottomSheetFragment == null) {
                BottomSheetInvestmentCalculator().show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetInvestmentCalculator"
                )
            }
        }

        view.findViewById<LinearLayout>(R.id.financialCushionCalculatorLinearLayout).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetFinancialCushionCalculator") as? BottomSheetFinancialCushionCalculator
            if(bottomSheetFragment == null) {
                BottomSheetFinancialCushionCalculator().show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetLoanCalculator"
                )
            }
        }


    }
}