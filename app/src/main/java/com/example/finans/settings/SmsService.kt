package com.example.finans.settings

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
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
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.util.Locale

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


        val input = intent.getStringExtra("inputExtra")
        createNotificationChannel()
        val notificationIntent = Intent(this, SettingsActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0, notificationIntent, PendingIntent.FLAG_IMMUTABLE
        )
        val notification = NotificationCompat.Builder(this, "SMS foregroundService")
            .setContentTitle(getString(R.string.isSms))
            .setContentText(input)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .build()

        startForeground(1, notification)

        return START_STICKY
    }

    private fun createNotificationChannel() {

        val serviceChannel = NotificationChannel("SMS foregroundService", "Foreground Service Channel",
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
            val operation = regex.find(smsBody)?.value

            regex = Regex("\\d+\\.\\d+")
            val value = regex.find(smsBody)!!.value.toDouble()

            regex = Regex("\\d{2}\\.\\d{2}\\.\\d{4}\\s+\\d{2}:\\d{2}:\\d{2}")
            val dateTime = regex.find(smsBody)?.value


            val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
            val date = dateFormat.parse(dateTime!!)
            val timestamp = Timestamp(date!!)
            val map: GeoPoint? = null


            val fireStoreDatabase = FirebaseFirestore.getInstance()

            when (operation) {

                getText(R.string.expense) -> {
                    fireStoreDatabase.collection("users").document(Firebase.auth.uid.toString())
                        .collection("user").document("information")
                        .update("balance", FieldValue.increment(-value))
                        .addOnSuccessListener {
                            Log.w("expense", "Да")
                        }
                        .addOnFailureListener { e ->
                            Log.w("expense", "Ошибка при добавлении суммы к балансу", e)
                        }
                }
                getText(R.string.income) -> {
                    fireStoreDatabase.collection("users").document(Firebase.auth.uid.toString())
                        .collection("user").document("information")
                        .update("balance", FieldValue.increment(value))
                        .addOnSuccessListener {
                            Log.w("income", "Да")
                        }
                        .addOnFailureListener { e ->
                            Log.w("income", "Ошибка при добавлении суммы к балансу", e)
                        }
                }
                getText(R.string.translation) -> {
                    fireStoreDatabase.collection("users").document(Firebase.auth.uid.toString())
                        .collection("user").document("information")
                        .update("balance", FieldValue.increment(-value))
                        .addOnSuccessListener {
                            Log.w("translation", "Да")
                        }
                        .addOnFailureListener { e ->
                            Log.w("translation", "Ошибка при добавлении суммы к балансу", e)
                        }
                }

            }

            val sms = hashMapOf(
                "type" to operation,
                "value" to value,
                "timestamp" to timestamp,
                "note" to smsHeader,
                "category" to "Продукты",
                "image" to  "gs://finans-44544.appspot.com/category/question.png",
                "map" to map,
                "photo" to "",
            )

            db.collection("users").document(Firebase.auth.uid.toString())
                .collection("operation").document()
                .set(sms)
                .addOnSuccessListener { Log.d("d", "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w("d", "Error writing document", e) }
        }
    }
}