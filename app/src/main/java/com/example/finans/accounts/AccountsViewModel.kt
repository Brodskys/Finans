package com.example.finans.accounts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.ArrayList

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

class AccountsViewModel2 : ViewModel() {

    private val _selectedAccountsType = MutableLiveData<accountsType>()

    fun selectAccounts(accounts: accountsType) {
        _selectedAccountsType.value = accounts
    }

    fun getSelectedAccounts(): LiveData<accountsType> {
        return _selectedAccountsType
    }

    fun clearAccounts() {
        _selectedAccountsType.value = null
    }
}

class AccountsBudgetsViewModel : ViewModel() {

    private val _selectedAccountsBudgets = MutableLiveData<ArrayList<Accounts>>()

    fun selectAccountsBudgets(accounts: ArrayList<Accounts>) {
        _selectedAccountsBudgets.value = accounts
    }

    fun getSelectedAccountsBudgets(): LiveData<ArrayList<Accounts>> {
        return _selectedAccountsBudgets
    }

    fun clearAccountsBudgets() {
        _selectedAccountsBudgets.value = null
    }
}