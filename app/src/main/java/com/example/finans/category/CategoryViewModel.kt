package com.example.finans.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CategoryViewModel : ViewModel() {

    private val _selectedCategory = MutableLiveData<Category>()

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
    }

    fun getSelectedCategory(): LiveData<Category> {
        return _selectedCategory
    }

    fun clearCategory() {
        _selectedCategory.value = null
    }
}