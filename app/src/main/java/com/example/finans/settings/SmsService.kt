package com.example.finans.settings

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.finans.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID

class SmsService : Service() {

    private lateinit var smsObserver: ContentObserver

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        if (intent == null) {
            return super.onStartCommand(intent, flags, startId)
        }

        smsObserver = object : ContentObserver(Handler()) {
            override fun onChange(selfChange: Boolean, uri: Uri?) {
                getLastSms()
            }
        }

        contentResolver.registerContentObserver(Uri.parse("content://sms"), true, smsObserver)

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, "SMS foregroundService")
            .setContentText(getString(R.string.isSms))
            .setSmallIcon(android.R.drawable.ic_dialog_info)

            .build()

        startForeground(1, notification)

        return START_STICKY
    }

    private fun createNotificationChannel() {

        val serviceChannel = NotificationChannel("SMS foregroundService", "Notification",
            NotificationManager.IMPORTANCE_DEFAULT)
        val manager = getSystemService(NotificationManager::class.java)
        manager!!.createNotificationChannel(serviceChannel)

    }

    override fun onDestroy() {
        super.onDestroy()
        contentResolver.unregisterContentObserver(smsObserver)
    }
    private var lastSmsTime = 0L

    @SuppressLint("Range", "SimpleDateFormat")
    private fun getLastSms() {

        val currentTime = System.currentTimeMillis()

        if (currentTime - lastSmsTime < 1000) {
            return
        }

        val cursor = contentResolver.query(Uri.parse("content://sms/inbox"),null, null, null, "date DESC limit 1")

        if (cursor != null && cursor.moveToFirst()) {

            lastSmsTime = currentTime

            val smsHeader = cursor.getString(cursor.getColumnIndex("address"))
            val smsBody = cursor.getString(cursor.getColumnIndex("body"))
            cursor.close()

            val db = Firebase.firestore

            var regex = Regex("^\\w+")
            var operation = regex.find(smsBody)?.value

            regex = Regex("\\d+\\.\\d+")
            val value = regex.find(smsBody)!!.value.toDouble()

            regex = Regex("\\d{2}\\.\\d{2}\\.\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}")
            val dateTime = regex.find(smsBody)?.value


            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(dateTime!!)
            val timestamp = Timestamp(date!!)
            val map = GeoPoint(0.0, 0.0)


            val id = UUID.randomUUID().toString()

            if(operation == "POPOLNENIE") operation = getText(R.string.income).toString()
            else operation = getText(R.string.expense).toString()

            val money = if (operation == getText(R.string.income)) value  else -value

            val sharedPref = getSharedPreferences("Settings", Context.MODE_PRIVATE)
            val accounts = sharedPref.getString("accounts", "")

            var operationRu = ""
            var operationEng = ""

            when (operation) {
                getString(R.string.income) ->{
                    operationEng = "Income"
                    operationRu = "Доход"
                }
                getString(R.string.expense) ->{
                    operationEng = "Expense"
                    operationRu = "Расход"
                }
                getString(R.string.translation) ->{
                    operationEng = "Translation"
                    operationRu = "Перевод"
                }
            }

            val sms = hashMapOf<String, Any>(
                "id" to id,
                "typeRu" to operationRu,
                "typeEn" to operationEng,
                "value" to value,
                "timestamp" to timestamp,
                "note" to smsHeader,
                "categoryRu" to "Отсутствует",
                "categoryEn" to "Absent",
                "image" to "gs://finans-44544.appspot.com/category/other.png",
                "map" to map,
                "photo" to "",
                "account" to accounts!!
            )

            val settings = FirebaseFirestoreSettings.Builder()
                .setPersistenceEnabled(true)
                .build()

            db.firestoreSettings = settings

            val userID = db.collection("users").document(Firebase.auth.uid.toString())

            userID
                .collection("user").document("information")
                .update("total_balance", FieldValue.increment(money))
                .addOnSuccessListener {}
                .addOnFailureListener {}

            userID
                .collection("accounts").document(accounts)
                .update("balance", FieldValue.increment(money))
                .addOnSuccessListener {}
                .addOnFailureListener {}


            userID.collection("accounts")
                .document(accounts)
                .collection("operation").document(id)
                .set(sms)
                .addOnSuccessListener { Log.d("d", "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w("d", "Error writing document", e) }
        }
    }
}