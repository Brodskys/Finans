package com.example.finans.category.updateCategory

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class IconViewModel : ViewModel() {

    private val _selectedIcon = MutableLiveData<IconInfo>()

    fun selectIcon(selectedIcon: String, type: String) {
        _selectedIcon.value = IconInfo(selectedIcon, type)
    }

    fun getSelectedIcon(): LiveData<IconInfo> {
        return _selectedIcon
    }

    fun clearIcon() {
        _selectedIcon.value = null
    }
}