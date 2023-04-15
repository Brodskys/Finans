package com.example.finans.category

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class CategoryViewModel : ViewModel() {

    private val selectedCategory = MutableLiveData<Category>()

    fun selectCategory(category: Category) {
        selectedCategory.value = category
    }

    fun getSelectedCategory(): LiveData<Category> {
        return selectedCategory
    }
}