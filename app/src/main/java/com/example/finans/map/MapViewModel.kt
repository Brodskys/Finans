package com.example.finans.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint

class MapViewModel : ViewModel() {

    private val _selectedMap = MutableLiveData<GeoPoint>()

    fun selectMap(map: GeoPoint) {
        _selectedMap.value = map
    }

    fun getSelectedMap(): LiveData<GeoPoint> {
        return _selectedMap
    }
    fun clearMap() {
        _selectedMap.value = null
    }
}