package com.example.finans.plans.paymentPlanning

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.finans.R
import com.example.finans.authorization.authWithFacebook.AuthorizationPresenterFacebook
import com.example.finans.operation.BottomSheetNewOperationFragment
import com.example.finans.other.deletionWarning
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class BottomSheetPaymentPlanCard : BottomSheetDialogFragment() {
    private lateinit var paymentPlanning: PaymentPlanning
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences : SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_payment_plan_card, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_payment_plan_card, container, false)
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        paymentPlanning = arguments?.getParcelable("paymentPlanning")!!

        db = Firebase.firestore

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        db.firestoreSettings = settings

        val datePaymentPlanCard = view.findViewById<TextView>(R.id.datePaymentPlanCard)
        val dateToPaymentPlanCard = view.findViewById<TextView>(R.id.dateToPaymentPlanCard)

        val pattern = "dd MMMM yyyy HH:mm"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())
        val paymentDate = paymentPlanning.timestamp!!.toDate()

        datePaymentPlanCard.text = simpleDateFormat.format(paymentDate)

        val currentDate = LocalDate.now()

        val daysDifference = ChronoUnit.DAYS.between(currentDate, paymentDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate())

        if (daysDifference > 0) {
            dateToPaymentPlanCard.text = "${getString(R.string.date1)} $daysDifference ${getString(R.string.date11)}"
            dateToPaymentPlanCard.setTextColor(Color.rgb(76, 175, 80))
        } else if (daysDifference < 0) {
            dateToPaymentPlanCard.text = " ${-daysDifference} ${getString(R.string.date11)} ${getString(R.string.date2)}"
            dateToPaymentPlanCard.setTextColor(Color.rgb(244, 67, 54))
        } else {
            dateToPaymentPlanCard.text = getString(R.string.date3)
            dateToPaymentPlanCard.setTextColor(Color.rgb(128, 128, 128))
        }

        if(paymentPlanning.status == "paidFor"){
            dateToPaymentPlanCard.text = getString(R.string.paid)
            dateToPaymentPlanCard.setTextColor(Color.rgb(76, 175, 80))
            view.findViewById<RelativeLayout>(R.id.payPaymentPlanRelativeLayout).visibility = View.GONE
        }

        view.findViewById<RelativeLayout>(R.id.payPaymentPlanRelativeLayout).setOnClickListener {
            val newFragment = BottomSheetNewOperationFragment.newInstance(paymentPlanning)
            newFragment.setTargetFragment(this@BottomSheetPaymentPlanCard, 0)

            newFragment.show(
                requireActivity().supportFragmentManager,
                "BottomSheetNewOperationFragment"
            )
            dismiss()

            val prefs = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
            prefs.edit().remove("paymentPlanning").apply()
        }



        view.findViewById<RelativeLayout>(R.id.updatePaymentPlanRelativeLayout).setOnClickListener {
            val newFragment = BottomSheetPaymentPlan.newInstance(paymentPlanning)
            newFragment.setTargetFragment(this@BottomSheetPaymentPlanCard, 0)

            newFragment.show(
                requireActivity().supportFragmentManager,
                "BottomSheetPaymentPlan"
            )
            dismiss()
        }



        view.findViewById<TextView>(R.id.deletePaymentPlanTextView).setOnClickListener {
            deletionWarning(requireContext()) { result ->

                if (result) {

                    val intent = Intent(context, NotificationReceiver::class.java)

                    intent.putExtra("uid", paymentPlanning.idNotification!!.toInt())

                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        paymentPlanning.idNotification!!.toInt(),
                        intent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                    )

                    val alarmManager =
                        requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
                    alarmManager.cancel(pendingIntent)

                    val notificationManager = NotificationManagerCompat.from(requireContext())
                    notificationManager.cancel(paymentPlanning.idNotification!!.toInt())

                    val docRef = db.collection("users").document(Firebase.auth.uid.toString())
                        .collection("paymentPlanning")
                        .document(paymentPlanning.id!!)


                    docRef.delete()
                        .addOnSuccessListener {
                            Log.d(
                                AuthorizationPresenterFacebook.TAG,
                                "DocumentSnapshot successfully deleted!"
                            )
                            dismiss()
                        }
                        .addOnFailureListener { e ->
                            Log.w(AuthorizationPresenterFacebook.TAG, "Error deleting document", e)
                        }


                }
            }
        }

    }

    companion object {
        fun newInstance(
            paymentPlanning: PaymentPlanning
        ): BottomSheetPaymentPlanCard {
            val args = Bundle()
            args.putParcelable("paymentPlanning", paymentPlanning)

            val fragment = BottomSheetPaymentPlanCard()
            fragment.arguments = args

            return fragment
        }
    }
}