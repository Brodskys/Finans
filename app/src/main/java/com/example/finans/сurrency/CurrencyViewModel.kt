package com.example.finans.сurrency

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CurrencyViewModel : ViewModel() {

    private val _selectedCurrency = MutableLiveData<String>()

    fun selectedCurrency(currency: String) {
        _selectedCurrency.value = currency
    }

    fun getSelectedCurrency(): LiveData<String> {
        return _selectedCurrency
    }

    fun clearCurrency() {
        _selectedCurrency.value = null
    }
}