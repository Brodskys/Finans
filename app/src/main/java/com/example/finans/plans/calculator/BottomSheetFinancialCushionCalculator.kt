package com.example.finans.plans.calculator

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.finans.R
import com.example.finans.other.setDigitsAndMaxLength
import com.example.finans.other.setPercentageInput
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import java.text.DecimalFormat

class BottomSheetFinancialCushionCalculator : BottomSheetDialogFragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight  = R.style.AppBottomSheetDialogTheme
        }

        dialog?.setCancelable(false)
        return inflater.inflate(
            R.layout.fragment_bottom_sheet_financial_cushion_calculator,
            container,
            false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        view.findViewById<TextView>(R.id.financialCushionCalculatorExit).setOnClickListener {
            dismiss()
        }

        val summaFinancialCushionTextInputEditText =
            view.findViewById<TextInputEditText>(R.id.summaFinancialCushionTextInputEditText)
        val expensesFinancialCushionTextInputEditText =
            view.findViewById<TextInputEditText>(R.id.expensesFinancialCushionTextInputEditText)
        val periodFinancialCushionTextInputEditText =
            view.findViewById<TextInputEditText>(R.id.periodFinancialCushionTextInputEditText)

        val currencyFinancialCushionSpinner = view.findViewById<Spinner>(R.id.currencyFinancialCushionSpinner)
        val monthYearFinancialCushionSpinner = view.findViewById<Spinner>(R.id.monthYearFinancialCushionSpinner)


        val pref =
            requireActivity().getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)

        val locale = pref.getString("locale", "")

        val lang = locale == "ru"

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.currency,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_item)
            currencyFinancialCushionSpinner.adapter = adapter
        }


        if (lang) {
            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.month_year_array_ru,
                R.layout.spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item)
                monthYearFinancialCushionSpinner.adapter = adapter
            }
        } else {
            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.month_year_array_en,
                R.layout.spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item)
                monthYearFinancialCushionSpinner.adapter = adapter
            }
        }

        view.findViewById<Button>(R.id.calculateFinancialCushionBtn).setOnClickListener {
            if (summaFinancialCushionTextInputEditText.text.toString() == "") {
                summaFinancialCushionTextInputEditText.error = getString(R.string.fillInAllFields)
            } else if (expensesFinancialCushionTextInputEditText.text.toString() == "") {
                expensesFinancialCushionTextInputEditText.error = getString(R.string.fillInAllFields)
            } else if (periodFinancialCushionTextInputEditText.text.toString() == "") {
                periodFinancialCushionTextInputEditText.error = getString(R.string.fillInAllFields)
            } else{

                val currencyFormat = DecimalFormat("#,##0.00 ${currencyFinancialCushionSpinner.selectedItem}")
                var months = 0.0

                when (monthYearFinancialCushionSpinner.selectedItemPosition) {
                    0 -> {
                        months = periodFinancialCushionTextInputEditText.text.toString().toDouble()
                    }
                    1 -> {
                        months = periodFinancialCushionTextInputEditText.text.toString().toDouble() * 12
                    }
                }

                val monthlyExpenses = expensesFinancialCushionTextInputEditText.text.toString().toDouble()
                val monthlyIncome = summaFinancialCushionTextInputEditText.text.toString().toDouble()

                val financialCushion = monthlyExpenses * months
                val monthlySavings = monthlyIncome - monthlyExpenses

                val totalRequiredSavings = financialCushion + (monthlySavings * months)


                view.findViewById<TextView>(R.id.financialCushionTextView).text = currencyFormat.format(totalRequiredSavings)

            }


            }

        summaFinancialCushionTextInputEditText.setDigitsAndMaxLength(7)
        expensesFinancialCushionTextInputEditText.setDigitsAndMaxLength(7)
        periodFinancialCushionTextInputEditText.setDigitsAndMaxLength(2)


    }
}