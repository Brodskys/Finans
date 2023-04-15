package com.example.finans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapViewModel : ViewModel() {

    private val selectedMap = MutableLiveData<String>()

    fun selectMap(map: String) {
        selectedMap.value = map
    }

    fun getSelectedMap(): LiveData<String> {
        return selectedMap
    }
}