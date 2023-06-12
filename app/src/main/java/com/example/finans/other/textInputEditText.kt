package com.example.finans.other

import android.annotation.SuppressLint
import android.content.Context
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import com.example.finans.R
import com.google.android.material.textfield.TextInputEditText

fun TextInputEditText.setDigitsAndMaxLength(maxLength: Int) {
    filters = arrayOf(InputFilter.LengthFilter(maxLength))
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.let {
                val regex = Regex("[^\\d]")
                val onlyDigits = it.replace(regex, "")
                if (it.toString() != onlyDigits) {
                    setText(onlyDigits)
                    setSelection(onlyDigits.length)
                }
            }
        }
    })
}

fun TextInputEditText.setPercentageInput(context: Context) {
    val maxDigitsBeforeDecimal = 3
    val maxDigitsAfterDecimal = 2
    val maxValue = 100.0

    val maxLength = maxDigitsBeforeDecimal + 1 + maxDigitsAfterDecimal
    filters = arrayOf(InputFilter.LengthFilter(maxLength))

    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {}

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        @SuppressLint("SetTextI18n")
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            s?.let {
                val percentageInput = it.toString()
                val parts = percentageInput.split(".")
                if (parts.size <= 2) {
                    val digitsBeforeDecimal = parts[0].length
                    val digitsAfterDecimal = if (parts.size == 2) parts[1].length else 0

                    if (digitsBeforeDecimal > maxDigitsBeforeDecimal || digitsAfterDecimal > maxDigitsAfterDecimal) {
                        setText(
                            percentageInput.substring(0, start) + percentageInput.substring(
                                start + count
                            )
                        )
                        setSelection(start)
                    }

                    val value =
                        if (percentageInput.isEmpty()) 0.0 else percentageInput.toDouble()
                    if (value > maxValue) {
                        error = context.getString(R.string.loan11)
                    } else {
                        error = null
                    }
                } else {
                    setText(
                        percentageInput.substring(0, start) + percentageInput.substring(
                            start + count
                        )
                    )
                    setSelection(start)
                }
            }
        }
    })
}