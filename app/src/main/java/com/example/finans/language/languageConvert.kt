package com.example.finans.language

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences

fun languageInit(activity: Activity):Boolean{
    val prefs = activity.getSharedPreferences("Settings", Context.MODE_PRIVATE)

    return languageConvert(prefs)
}

fun languageConvert(sharedPreferences: SharedPreferences):Boolean {
    val sharedPref =  sharedPreferences.getString("locale", "")

    return sharedPref == "ru"
}