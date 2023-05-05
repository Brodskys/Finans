package com.example.finans

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.GeoPoint

class MapViewModel : ViewModel() {

    private val selectedMap = MutableLiveData<GeoPoint>()

    fun selectMap(map: GeoPoint) {
        selectedMap.value = map
    }

    fun getSelectedMap(): LiveData<GeoPoint> {
        return selectedMap
    }
}