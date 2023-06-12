package com.example.finans.plans.calculator

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.finans.R
import com.example.finans.other.setDigitsAndMaxLength
import com.example.finans.other.setPercentageInput
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import java.text.DecimalFormat

class BottomSheetInvestmentCalculator : BottomSheetDialogFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }

        dialog?.setCancelable(false)
        return inflater.inflate(
            R.layout.fragment_bottom_sheet_investment_calculator,
            container,
            false
        )
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        view.findViewById<TextView>(R.id.investmentCalculatorExit).setOnClickListener {
            dismiss()
        }

        val summaInvestmentTextInputEditText =
            view.findViewById<TextInputEditText>(R.id.summaInvestmentTextInputEditText)
        val percentInvestmentTextInputEditText =
            view.findViewById<TextInputEditText>(R.id.percentInvestmentTextInputEditText)
        val periodInvestmentTextInputEditText =
            view.findViewById<TextInputEditText>(R.id.periodInvestmentTextInputEditText)

        val currencyInvestmentSpinner = view.findViewById<Spinner>(R.id.currencyInvestmentSpinner)
        val monthYearInvestmentSpinner = view.findViewById<Spinner>(R.id.monthYearInvestmentSpinner)
        val interestAccrualModeInvestSpinner =
            view.findViewById<Spinner>(R.id.interestAccrualModeInvestSpinner)


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
            currencyInvestmentSpinner.adapter = adapter
        }


        if (lang) {
            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.month_year_array_ru,
                R.layout.spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item)
                monthYearInvestmentSpinner.adapter = adapter
            }

            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.interest_accrual_mode_ru,
                R.layout.spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item)
                interestAccrualModeInvestSpinner.adapter = adapter
            }
        } else {
            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.month_year_array_en,
                R.layout.spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item)
                monthYearInvestmentSpinner.adapter = adapter
            }

            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.interest_accrual_mode_en,
                R.layout.spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item)
                interestAccrualModeInvestSpinner.adapter = adapter
            }
        }


        view.findViewById<Button>(R.id.calculateInvestmentBtn).setOnClickListener {
            if (summaInvestmentTextInputEditText.text.toString() == "") {
                summaInvestmentTextInputEditText.error = getString(R.string.fillInAllFields)
            } else if (percentInvestmentTextInputEditText.text.toString() == "") {
                percentInvestmentTextInputEditText.error = getString(R.string.fillInAllFields)
            } else if (periodInvestmentTextInputEditText.text.toString() == "") {
                periodInvestmentTextInputEditText.error = getString(R.string.fillInAllFields)
            } else if (percentInvestmentTextInputEditText.error == null) {

                val currencyFormat =
                    DecimalFormat("#,##0.00 ${currencyInvestmentSpinner.selectedItem}")

                val initialAmount = summaInvestmentTextInputEditText.text.toString().toInt()
                val interestRate = percentInvestmentTextInputEditText.text.toString().toDouble()/100
                val investmentPeriod = periodInvestmentTextInputEditText.text.toString().toDouble()
                var mouth = 0.00


                when (monthYearInvestmentSpinner.selectedItemPosition) {

                    0 -> {
                        mouth = investmentPeriod / 12
                    }

                    1 -> {
                        mouth = investmentPeriod
                    }
                }

                var S = 0.0
                var I = 0.0

                when (interestAccrualModeInvestSpinner.selectedItemPosition) {
                    0 -> {
                        S = initialAmount * (1 + mouth * interestRate)
                        I = initialAmount * mouth * interestRate
                    }

                    1 -> {
                        S = initialAmount * Math.pow(1 + interestRate, mouth)
                        I = initialAmount * (Math.pow(1 + interestRate, mouth)-1)
                    }

                }
                view.findViewById<TextView>(R.id.investmentTextView1).text =
                    currencyFormat.format(initialAmount)
                view.findViewById<TextView>(R.id.investmentTextView2).text =
                    currencyFormat.format(I)
                view.findViewById<TextView>(R.id.investmentTextView3).text =
                    currencyFormat.format(S)


            }

        }

        summaInvestmentTextInputEditText.setDigitsAndMaxLength(7)
        percentInvestmentTextInputEditText.setPercentageInput(requireContext())
        periodInvestmentTextInputEditText.setDigitsAndMaxLength(2)
    }


}