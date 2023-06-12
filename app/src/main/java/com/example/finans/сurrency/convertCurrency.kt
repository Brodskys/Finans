package com.example.finans.сurrency

import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject

fun convertCurrency(scope: LifecycleCoroutineScope, client: OkHttpClient,
                            fromCurrency: String, toCurrency: String, onResult: (String) -> Unit) {

    scope.launch(Dispatchers.IO) {
        try {
            val apiKey = "e892dc771674d2889c591cd02353db18677911e0"
            val request = Request.Builder()
                .url("https://api.getgeoapi.com/v2/currency/convert?api_key=$apiKey&from=$fromCurrency&to=$toCurrency&amount=1&format=json")
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string()

            val jsonResponse = JSONObject(responseBody)

            val status = jsonResponse.getString("status")

            if (status == "success") {
                val rates = jsonResponse.getJSONObject("rates")
                val convertedAmount = rates.getJSONObject(toCurrency).getString("rate_for_amount")

                withContext(Dispatchers.Main) {
                    onResult(convertedAmount)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                onResult("0.0")
            }
        }
    }
}