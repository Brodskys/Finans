package com.example.finans.operation.qr

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class QrViewModel : ViewModel() {

    private val _qrText = MutableLiveData<String>()

    fun selectQr(qrText: String) {
        _qrText.value = qrText
    }

    fun getSelectedQr(): LiveData<String> {
        return _qrText
    }

    fun clearQr() {
        _qrText.value = null
    }
}