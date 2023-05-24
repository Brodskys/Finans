package com.example.finans.image

import android.graphics.Bitmap
import android.net.Uri

data class ImageInfo(val uri: Uri?, val bitmap: Bitmap?, val cameraGallery: String, val type: String)
