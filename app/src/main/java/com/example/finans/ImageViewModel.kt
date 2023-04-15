package com.example.finans

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ImageViewModel : ViewModel() {

    private val _cameraImageUri = MutableLiveData<Uri?>()
    val cameraImageUri: LiveData<Uri?>
        get() = _cameraImageUri

    private val _galleryImageUri = MutableLiveData<Uri?>()
    val galleryImageUri: LiveData<Uri?>
        get() = _galleryImageUri

    fun setCameraImageUri(uri: Uri?) {
        _cameraImageUri.value = uri
    }

    fun setGalleryImageUri(uri: Uri?) {
        _galleryImageUri.value = uri
    }

    fun clearCameraImage() {
        _cameraImageUri.value = null
    }
    fun clearGalleryImage() {
        _galleryImageUri.value = null
    }
}