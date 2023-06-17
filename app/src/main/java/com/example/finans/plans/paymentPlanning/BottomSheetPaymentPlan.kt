package com.example.finans.plans.paymentPlanning

import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.finans.R
import com.example.finans.category.BottomSheetCategoryFragment
import com.example.finans.category.Category
import com.example.finans.category.CategoryViewModel
import com.example.finans.language.languageInit
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.Random
import java.util.UUID


class BottomSheetPaymentPlan : BottomSheetDialogFragment() {
    private lateinit var paymentPlanning: PaymentPlanning
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var currentDateTime: Calendar

    private lateinit var paymentPlanUpdateExit: TextView
    private lateinit var paymentPlanUpdateRelativeLayout: RelativeLayout

    private lateinit var paymentPlanUpdateCategoryIcon: ImageView
    private lateinit var paymentPlanUpdateNameEdit: EditText
    private lateinit var paymentPlanningUpdateValueEditText: EditText
    private lateinit var paymentPlanUpdateSpinner: Spinner
    private lateinit var paymentPlanUpdateSubcategoryTextView: TextView
    private lateinit var paymentPlanUpdateCategoryRelativeLayout: RelativeLayout
    private lateinit var paymentPlanUpdateDateTimeRelativeLayout: RelativeLayout
    private lateinit var paymentPlanUpdateDateTimeTextView: TextView
    private lateinit var sharedPreferences : SharedPreferences


    private var categUrl:String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight  = R.style.AppBottomSheetDialogTheme
        }
        dialog?.setCancelable(false)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_payment_plan, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_payment_plan, container, false)
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        paymentPlanning = arguments?.getParcelable("paymentPlanning")!!

        paymentPlanUpdateCategoryIcon = view.findViewById(R.id.paymentPlanUpdateCategoryIcon)
        paymentPlanUpdateNameEdit = view.findViewById(R.id.paymentPlanUpdateNameEdit)
        paymentPlanningUpdateValueEditText = view.findViewById(R.id.paymentPlanningUpdateValueEditText)
        paymentPlanUpdateSpinner = view.findViewById(R.id.paymentPlanUpdateSpinner)
        paymentPlanUpdateSubcategoryTextView = view.findViewById(R.id.paymentPlanUpdateSubcategoryTextView)
        paymentPlanUpdateCategoryRelativeLayout = view.findViewById(R.id.paymentPlanUpdateCategoryRelativeLayout)
        paymentPlanUpdateDateTimeRelativeLayout = view.findViewById(R.id.paymentPlanUpdateDateTimeRelativeLayout)
        paymentPlanUpdateDateTimeTextView = view.findViewById(R.id.paymentPlanUpdateDateTimeTextView)

        paymentPlanUpdateExit = view.findViewById(R.id.paymentPlanUpdateExit)
        paymentPlanUpdateRelativeLayout = view.findViewById(R.id.paymentPlanUpdateRelativeLayout)


        val storage = Firebase.storage



        paymentPlanUpdateNameEdit.setText(paymentPlanning.name!!)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        if(switchState){
            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.currency,
                R.layout.spinner_dark_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_dark_item)
                paymentPlanUpdateSpinner.adapter = adapter

                val currentPosition = adapter.getPosition(paymentPlanning.currency)
                paymentPlanUpdateSpinner.setSelection(currentPosition)
            }
        }
        else{
            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.currency,
                R.layout.spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item)
                paymentPlanUpdateSpinner.adapter = adapter

                val currentPosition = adapter.getPosition(paymentPlanning.currency)
                paymentPlanUpdateSpinner.setSelection(currentPosition)
            }
        }

        paymentPlanningUpdateValueEditText.setText(paymentPlanning.value.toString())

        val documentRef = FirebaseFirestore.getInstance().document("users/${Firebase.auth.uid.toString()}${paymentPlanning.category}")


        documentRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val categoryPaument = documentSnapshot.toObject(Category::class.java)

                    val gsReference = storage.getReferenceFromUrl(categoryPaument!!.image!!)
                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri.toString())
                            .into(paymentPlanUpdateCategoryIcon)
                    }.addOnFailureListener {
                        Picasso.get().load(R.drawable.category)
                            .into(paymentPlanUpdateCategoryIcon)
                    }

                    if (languageInit(requireActivity())) {
                        paymentPlanUpdateSubcategoryTextView.text =
                            categoryPaument.nameRus

                    } else
                        paymentPlanUpdateSubcategoryTextView.text =
                            categoryPaument.nameEng

                }
            }
            .addOnFailureListener { exception ->

            }




        val dateTimeFormat = SimpleDateFormat("dd.MM.y HH:mm", Locale.getDefault())

        paymentPlanUpdateDateTimeTextView.text = dateTimeFormat.format(paymentPlanning.timestamp!!.toDate())


        paymentPlanUpdateCategoryRelativeLayout.setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetCategoryFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetCategoryFragment()
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetCategoryFragment"
                )
            }

            view.findViewById<ImageView>(R.id.paymentPlanUpdateCategoryIcon)
                .setImageResource(R.drawable.category)
            paymentPlanUpdateSubcategoryTextView.text = ""

        }

        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]
        categoryViewModel.getSelectedCategory().observe(this) { ctg ->
            if (ctg != null) {
                categUrl = ctg.url
                val gsReference = storage.getReferenceFromUrl(ctg.image!!)
                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString())
                        .into(view.findViewById<ImageView>(R.id.paymentPlanUpdateCategoryIcon))
                }.addOnFailureListener {
                    Picasso.get().load(R.drawable.category)
                        .into(view.findViewById<ImageView>(R.id.paymentPlanUpdateCategoryIcon))
                }
                if (languageInit(requireActivity())) {
                    paymentPlanUpdateSubcategoryTextView.text =
                        ctg.nameRus

                } else
                    paymentPlanUpdateSubcategoryTextView.text =
                        ctg.nameEng
                categoryViewModel.clearCategory()
            }
        }

        currentDateTime = Calendar.getInstance()

        paymentPlanUpdateDateTimeRelativeLayout.setOnClickListener {

            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    currentDateTime.set(Calendar.YEAR, year)
                    currentDateTime.set(Calendar.MONTH, month)
                    currentDateTime.set(Calendar.DAY_OF_MONTH, dayOfMonth)

                    val timePickerDialog = TimePickerDialog(
                        requireContext(),
                        { _, hourOfDay, minute ->
                            currentDateTime.set(Calendar.HOUR_OF_DAY, hourOfDay)
                            currentDateTime.set(Calendar.MINUTE, minute)

                            paymentPlanUpdateDateTimeTextView.text =
                                dateTimeFormat.format(currentDateTime.time)
                        },
                        currentDateTime.get(Calendar.HOUR_OF_DAY),
                        currentDateTime.get(Calendar.MINUTE),
                        true
                    )

                    timePickerDialog.show()
                },
                currentDateTime.get(Calendar.YEAR),
                currentDateTime.get(Calendar.MONTH),
                currentDateTime.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.datePicker.minDate = Calendar.getInstance().timeInMillis
            datePickerDialog.show()

        }

        paymentPlanUpdateExit.setOnClickListener {

            dismiss()

        }

        paymentPlanUpdateRelativeLayout.setOnClickListener {

            if (paymentPlanUpdateNameEdit.text.toString() != "" && paymentPlanningUpdateValueEditText.text.toString() != ""
                &&  paymentPlanUpdateSubcategoryTextView.text.toString() != "") {


                val c = categUrl ?: paymentPlanning.category

                val dt = dateTimeFormat.format(paymentPlanning.timestamp!!.toDate())


                var status = if (paymentPlanning.status == "paidFor" && dt != paymentPlanUpdateDateTimeTextView.text) "InProgress" else paymentPlanning.status!!


                val hashMap = hashMapOf<String, Any>(
                    "value" to paymentPlanningUpdateValueEditText.text.toString().toDouble(),
                    "name" to paymentPlanUpdateNameEdit.text.toString(),
                    "category" to c!!,
                    "timestamp" to currentDateTime.time,
                    "id" to paymentPlanning.id!!,
                    "idNotification" to paymentPlanning.idNotification!!.toString(),
                    "currency" to paymentPlanUpdateSpinner.selectedItem,
                    "status" to status,
                )

                val documentRef =  FirebaseFirestore.getInstance().collection("users")
                    .document(Firebase.auth.uid.toString())
                    .collection("paymentPlanning").document(paymentPlanning.id!!)
                documentRef.update(hashMap as Map<String, Any>)
                    .addOnSuccessListener {

                        dismiss()
                    }
                    .addOnFailureListener { exception -> }


                val alarmManager =
                    requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

                val intent = Intent(context, NotificationReceiver::class.java)



                val payment = PaymentPlanning().apply {
                    value = hashMap["value"] as Double
                    category = hashMap["category"] as String
                    id = hashMap["id"] as String
                    idNotification = hashMap["idNotification"] as String
                    currency = hashMap["currency"] as String
                    name = hashMap["name"] as String
                    status = hashMap["status"] as String
                    timestamp = Timestamp((hashMap["timestamp"] as Date))
                }

                val paymentPlanningString = Gson().toJson(payment)

                intent.putExtra("paymentPlanning", paymentPlanningString)
                intent.putExtra("uid", paymentPlanning.idNotification)
                intent.putExtra("message", "${paymentPlanUpdateNameEdit.text.toString()} ${paymentPlanningUpdateValueEditText.text}${paymentPlanUpdateSpinner.selectedItem}")

                val pendingIntent = PendingIntent.getBroadcast(
                    context,
                    paymentPlanning.idNotification!!.toInt(),
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT
                )


                alarmManager.set(AlarmManager.RTC_WAKEUP, currentDateTime.timeInMillis, pendingIntent)

            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.error)
                builder.setMessage(R.string.fillInAllFields)
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setPositiveButton("OK", null)
                val dialog = builder.create()
                dialog.show()
            }

        }

    }

    companion object {
        fun newInstance(
            paymentPlanning: PaymentPlanning
        ): BottomSheetPaymentPlan {
            val args = Bundle()
            args.putParcelable("paymentPlanning", paymentPlanning)

            val fragment = BottomSheetPaymentPlan()
            fragment.arguments = args

            return fragment
        }
    }

}