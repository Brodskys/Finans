package com.example.finans.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.ArrayList

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

class CategoriesBudgetsViewModel : ViewModel() {

    private val _categoriesBudgets = MutableLiveData<ArrayList<Category>>()

    fun selectCategoriesBudgets(category: ArrayList<Category>) {
        _categoriesBudgets.value = category
    }

    fun getSelectedCategoriesBudgets(): LiveData<ArrayList<Category>> {
        return _categoriesBudgets
    }

    fun clearCategoriesBudgets() {
        _categoriesBudgets.value = null
    }
}