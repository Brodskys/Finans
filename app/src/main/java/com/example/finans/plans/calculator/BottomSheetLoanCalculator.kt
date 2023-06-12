package com.example.finans.plans.calcula

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.widget.DatePicker
import java.util.Calendar
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow


class BottomSheetLoanCalculator : BottomSheetDialogFragment() {
    private lateinit var startDate: Date


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }

        dialog?.setCancelable(false)
        return inflater.inflate(R.layout.fragment_bottom_sheet_loan_calculator, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        view.findViewById<TextView>(R.id.loanCalculatorExit).setOnClickListener {
            dismiss()
        }

        val summaLoanTextInputLayout =
            view.findViewById<TextInputEditText>(R.id.summaLoanTextInputEditText)
        val percentLoanTextInputLayout =
            view.findViewById<TextInputEditText>(R.id.percentLoanTextInputEditText)
        val periodLoanTextInputLayout =
            view.findViewById<TextInputEditText>(R.id.periodLoanTextInputEditText)
        val currencySpinner = view.findViewById<Spinner>(R.id.currencySpinner)
        val monthYearSpinner = view.findViewById<Spinner>(R.id.monthYearSpinner)
        val typeLoanSpinner = view.findViewById<Spinner>(R.id.typeLoanSpinner)
        val dateStartLoanTextInputEditText =
            view.findViewById<TextInputEditText>(R.id.dateStartLoanTextInputEditText)


        val pref = requireActivity().getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)

        val locale = pref.getString("locale", "")

        val lang = locale == "ru"


        val currentDate = Calendar.getInstance().time
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
        startDate = currentDate
        val formattedDate = dateFormat.format(currentDate)

        dateStartLoanTextInputEditText.setText(formattedDate)

        summaLoanTextInputLayout.setDigitsAndMaxLength(6)
        percentLoanTextInputLayout.setPercentageInput(requireContext())
        periodLoanTextInputLayout.setDigitsAndMaxLength(2)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.currency,
            R.layout.spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(R.layout.spinner_item)
            currencySpinner.adapter = adapter
        }

       if(lang) {
           ArrayAdapter.createFromResource(
               requireContext(),
               R.array.month_year_array_ru,
               R.layout.spinner_item
           ).also { adapter ->
               adapter.setDropDownViewResource(R.layout.spinner_item)
               monthYearSpinner.adapter = adapter
           }

           ArrayAdapter.createFromResource(
               requireContext(),
               R.array.repayment_procedure_ru,
               R.layout.spinner_item
           ).also { adapter ->
               adapter.setDropDownViewResource(R.layout.spinner_item)
               typeLoanSpinner.adapter = adapter
           }
       }else{
           ArrayAdapter.createFromResource(
               requireContext(),
               R.array.month_year_array_en,
               R.layout.spinner_item
           ).also { adapter ->
               adapter.setDropDownViewResource(R.layout.spinner_item)
               monthYearSpinner.adapter = adapter
           }

           ArrayAdapter.createFromResource(
               requireContext(),
               R.array.repayment_procedure_en,
               R.layout.spinner_item
           ).also { adapter ->
               adapter.setDropDownViewResource(R.layout.spinner_item)
               typeLoanSpinner.adapter = adapter
           }
       }

        dateStartLoanTextInputEditText.isFocusable = false
        dateStartLoanTextInputEditText.isClickable = true

        dateStartLoanTextInputEditText.setOnClickListener {
            showDatePickerDialog(dateStartLoanTextInputEditText)
        }



        view.findViewById<Button>(R.id.calculateLoanBtn).setOnClickListener {
            if (summaLoanTextInputLayout.text.toString() == "") {
                summaLoanTextInputLayout.error = getString(R.string.fillInAllFields)
            } else if (percentLoanTextInputLayout.text.toString() == "") {
                percentLoanTextInputLayout.error = getString(R.string.fillInAllFields)
            } else if (periodLoanTextInputLayout.text.toString() == "") {
                periodLoanTextInputLayout.error = getString(R.string.fillInAllFields)
            } else if (percentLoanTextInputLayout.error == null){
                var mouth = 0

                when (monthYearSpinner.selectedItemPosition) {

                    0 -> {
                        mouth = periodLoanTextInputLayout.text.toString().toInt()
                    }

                    1 -> {
                        mouth = periodLoanTextInputLayout.text.toString().toInt() * 12
                    }
                }

                val loanAmount = summaLoanTextInputLayout.text.toString().toInt()
                val loanTermMonths = mouth
                val interestRate = percentLoanTextInputLayout.text.toString().toDouble()/100
                val date = Calendar.getInstance()

                val monthlyInterestRate = interestRate / 12

                val decimalFormat = DecimalFormat("#,##0.00")
                val currencyFormat = DecimalFormat("#,##0.00 ${currencySpinner.selectedItem}")
                val currencyFormat2 = DecimalFormat("#,##0.00")

                when (typeLoanSpinner.selectedItemPosition) {

                    0 -> {


                        val monthlyPayments = mutableListOf<Double>()
                        date.time = startDate



                        var totalPayments = 0.0
                        var totalInterest = 0.0

                        for (i in 1..loanTermMonths) {
                            val monthlyPayment = (loanAmount / loanTermMonths) + ((loanAmount - (i - 1) * (loanAmount / loanTermMonths)) * monthlyInterestRate)
                            monthlyPayments.add(monthlyPayment)
                            totalPayments += monthlyPayment
                            totalInterest += monthlyPayment - loanAmount / loanTermMonths

                            date.add(Calendar.MONTH, 1)
                        }

                        val firstMonthlyPayment = monthlyPayments.first()
                        val lastMonthlyPayment = monthlyPayments.last()
                        val overpayment = totalPayments - loanAmount
                        val totalAmountPaid = loanAmount + overpayment
                        val overpaymentPercentage = (overpayment / loanAmount) * 100
                        val endDate = date.time

                        val formattedLoanAmount = currencyFormat.format(loanAmount)
                        val formattedFirstPayment = currencyFormat2.format(firstMonthlyPayment)
                        val formattedLastPayment = currencyFormat.format(lastMonthlyPayment)
                        val formattedOverpayment = currencyFormat.format(overpayment)
                        val formattedTotalAmountPaid = currencyFormat.format(totalAmountPaid)
                        val formattedOverpaymentPercentage = decimalFormat.format(overpaymentPercentage)
                        val formattedEndDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(endDate)


                        view.findViewById<TextView>(R.id.loanTextView1).text = formattedLoanAmount.toString()
                        view.findViewById<TextView>(R.id.loanTextView2).text = "${formattedFirstPayment}->${formattedLastPayment}"
                        view.findViewById<TextView>(R.id.loanTextView3).text = formattedOverpayment.toString()
                        view.findViewById<TextView>(R.id.loanTextView4).text = formattedTotalAmountPaid.toString()
                        view.findViewById<TextView>(R.id.loanTextView5).text =
                            "$formattedOverpaymentPercentage%"
                        view.findViewById<TextView>(R.id.loanTextView6).text = formattedEndDate.toString()
                    }

                    1 -> {
                        val monthlyPayment = (loanAmount * monthlyInterestRate) / (1 - (1 + monthlyInterestRate).pow(-loanTermMonths.toDouble()))
                        val totalPayments = monthlyPayment * loanTermMonths
                        val totalInterest = totalPayments - loanAmount

                        val firstMonthlyPayment = monthlyPayment
                        val overpayment = totalInterest
                        val totalAmountPaid = loanAmount + overpayment
                        val overpaymentPercentage = (overpayment / loanAmount) * 100

                        date.time = startDate
                        date.add(Calendar.MONTH, loanTermMonths)

                        val endDate = date.time


                        val formattedLoanAmount = currencyFormat.format(loanAmount)
                        val formattedFirstPayment = currencyFormat2.format(firstMonthlyPayment)
                        val formattedOverpayment = currencyFormat.format(overpayment)
                        val formattedTotalAmountPaid = currencyFormat.format(totalAmountPaid)
                        val formattedOverpaymentPercentage = decimalFormat.format(overpaymentPercentage)
                        val formattedEndDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault()).format(endDate)

                        view.findViewById<TextView>(R.id.loanTextView1).text = formattedLoanAmount.toString()
                        view.findViewById<TextView>(R.id.loanTextView2).text = "$formattedFirstPayment ${currencySpinner.selectedItem}"
                        view.findViewById<TextView>(R.id.loanTextView3).text = formattedOverpayment.toString()
                        view.findViewById<TextView>(R.id.loanTextView4).text = formattedTotalAmountPaid.toString()
                        view.findViewById<TextView>(R.id.loanTextView5).text =
                            "$formattedOverpaymentPercentage%"
                        view.findViewById<TextView>(R.id.loanTextView6).text = formattedEndDate.toString()


                    }
                }
            }
        }

    }

    fun showDatePickerDialog(textInputEditText: TextInputEditText) {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _: DatePicker, year: Int, month: Int, day: Int ->

                val locale = Locale.getDefault()
                val dateFormat = SimpleDateFormat.getDateInstance(SimpleDateFormat.SHORT, locale)

                val selectedDate = String.format("%02d.%02d.%04d", day, month + 1, year)
                startDate = dateFormat.parse(selectedDate)!!
                textInputEditText.setText(selectedDate)
            },
            currentYear,
            currentMonth,
            currentDay
        )

        datePickerDialog.datePicker.minDate = calendar.timeInMillis
        datePickerDialog.show()
    }

}