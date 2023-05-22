package com.example.finans.operation.operationDetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CurrencyViewModel : ViewModel() {

    private val selectedCurrency = MutableLiveData<String>()

    fun selectedCurrency(currency: String) {
        selectedCurrency.value = currency
    }

    fun getSelectedCCurrency(): LiveData<String> {
        return selectedCurrency
    }
}