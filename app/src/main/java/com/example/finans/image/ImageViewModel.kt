package com.example.finans.image

import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.finans.image.ImageInfo

class ImageViewModel : ViewModel() {

    private val _cameraImageUri = MutableLiveData<ImageInfo?>()
    val cameraImageUri: LiveData<ImageInfo?>
        get() = _cameraImageUri

    private val _galleryImageUri = MutableLiveData<ImageInfo?>()
    val galleryImageUri: LiveData<ImageInfo?>
        get() = _galleryImageUri

    fun setCameraImageUri(uri: Uri?, bitmap: Bitmap?,cameraGallery: String, type: String) {
        _cameraImageUri.value = ImageInfo(uri, bitmap, cameraGallery, type)
    }

    fun setGalleryImageUri(uri: Uri?, bitmap: Bitmap?,cameraGallery: String, type: String) {
        _galleryImageUri.value = ImageInfo(uri, bitmap, cameraGallery, type)
    }

    fun clearCameraImage() {
        _cameraImageUri.value = null
    }
    fun clearGalleryImage() {
        _galleryImageUri.value = null
    }
}