package com.example.finans.plans.paymentPlanning

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.finans.R
import com.example.finans.authorization.AuthorizationActivity
import com.example.finans.plans.PlansActivity
import com.google.gson.Gson

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val message = intent.getStringExtra("message")
        val uid = intent.getIntExtra("uid",0)
        val paymentPlanningString = intent.getStringExtra("paymentPlanning")



        val activityIntent = Intent(context, AuthorizationActivity::class.java)
        activityIntent.putExtra("paymentPlanning", paymentPlanningString)
        activityIntent.putExtra("fragment", "BottomSheetNewOperationFragment")

        val sharedPref = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("paymentPlanning", paymentPlanningString)
        editor.apply()


        val pendingIntent = PendingIntent.getActivity(
            context,
            uid,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notificationBuilder = NotificationCompat.Builder(context, "my_channel_id")
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentText(message)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(uid, notificationBuilder.build())
    }

}