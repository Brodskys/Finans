package com.example.finans.language

import android.app.Activity
import android.content.Context
import android.content.res.Resources
import android.os.LocaleList
import java.util.*

fun setLocale(localeToSet: String, resources: Resources, activity: Activity) {
    val localeListToSet = LocaleList(Locale(localeToSet))
    LocaleList.setDefault(localeListToSet)

    resources.configuration.setLocales(localeListToSet)
    resources.updateConfiguration(resources.configuration, resources.displayMetrics)
}

fun loadLocale(resources: Resources, activity: Activity) {
    val sharedPref = activity.getSharedPreferences("Settings", Context.MODE_PRIVATE)
    val localeToSet: String = sharedPref?.getString("locale", "")!!
    setLocale(localeToSet,resources, activity)
}

