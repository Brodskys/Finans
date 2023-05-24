package com.example.finans.other

import android.content.Context
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.finans.R

fun deletionWarning(context: Context, callback: (Boolean) -> Unit){
    val builder = AlertDialog.Builder(context)

    builder.setTitle(context.getString(R.string.warning))

    builder.setMessage(context.getString(R.string.areYouSure))

    builder.setPositiveButton(
        context.getString(R.string.yes)) { dialog, id ->
        callback(true)
    }

    builder.setNegativeButton(
        context.getString(R.string.no)) { dialog, id ->
        callback(false)
    }

    builder.show()

}