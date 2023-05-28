package com.example.finans.accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountsViewModel : ViewModel() {

    private val _selectedAccounts = MutableLiveData<Accounts>()

    fun selectAccounts(accounts: Accounts) {
        _selectedAccounts.value = accounts
    }

    fun getSelectedAccounts(): LiveData<Accounts> {
        return _selectedAccounts
    }

    fun clearAccounts() {
        _selectedAccounts.value = null
    }
}