package com.example.finans.plans.paymentPlanning

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
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

class BottomSheetPaymentPlanningAdd : BottomSheetDialogFragment() {
    private lateinit var currentDateTime: Calendar
    private lateinit var categoryViewModel: CategoryViewModel

    private lateinit var paymentPlanningTextInputEditText: EditText
    private lateinit var paymentPlanningNameEdit: EditText
    private lateinit var paymentPlanningSubcategoryTextView: TextView
    private lateinit var paymentPlanningDateTimeTextView: TextView
    private lateinit var categ: Category
    private lateinit var sharedPreferences : SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }
        dialog?.setCancelable(false)


        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_payment_planning_add, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_payment_planning_add, container, false)
        }

    }

    @SuppressLint("CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        val channel = NotificationChannel(
            "my_channel_id",
            "My Channel",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        channel.description = "My Notification Channel"
        val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)




        paymentPlanningTextInputEditText =
            view.findViewById(R.id.paymentPlanningAddValueEditText)

        paymentPlanningNameEdit =
            view.findViewById(R.id.paymentPlanningAddNameEdit)

        paymentPlanningSubcategoryTextView =
            view.findViewById(R.id.paymentPlanningAddSubcategoryTextView)

        paymentPlanningDateTimeTextView =
            view.findViewById(R.id.paymentPlanningAddDateTimeTextView)

        val currencyPaymentPlanningSpinner =
            view.findViewById<Spinner>(R.id.currencyPaymentPlanningAddSpinner)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        if(switchState){
            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.currency,
                R.layout.spinner_dark_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_dark_item)
                currencyPaymentPlanningSpinner.adapter = adapter

            }
        }
        else{
            ArrayAdapter.createFromResource(
                requireContext(),
                R.array.currency,
                R.layout.spinner_item
            ).also { adapter ->
                adapter.setDropDownViewResource(R.layout.spinner_item)
                currencyPaymentPlanningSpinner.adapter = adapter
            }
        }



        paymentPlanningTextInputEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString().contains(".")) {
                    val digitsAfterPoint = s.toString().substring(s.toString().indexOf(".") + 1)
                    if (digitsAfterPoint.length > 2) {
                        s?.replace(s.length - 1, s.length, "")
                    }
                }

                val decimalRegex =
                    "^\\\$?([1-9]{1}[0-9]{0,2}(\\,[0-9]{3})*(\\.[0-9]{0,2})?|[1-9]{1}[0-9]{0,}(\\.[0-9]{0,2})?|0(\\.[0-9]{0,2})?|(\\.[0-9]{1,2})?)\$"
                val match = s.toString().matches(decimalRegex.toRegex())
                if (!match) {
                    s?.replace(s.length - 1, s.length, "")
                }
            }
        })


        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]
        categoryViewModel.getSelectedCategory().observe(this) { ctg ->
            if (ctg != null) {
                val storage = Firebase.storage
                categ = ctg
                val gsReference = storage.getReferenceFromUrl(ctg.image!!)
                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString())
                        .into(view.findViewById<ImageView>(R.id.paymentPlanningAddCategoryIcon))
                }.addOnFailureListener {
                    Picasso.get().load(R.drawable.category)
                        .into(view.findViewById<ImageView>(R.id.paymentPlanningAddCategoryIcon))
                }
                if (languageInit(requireActivity())) {
                    paymentPlanningSubcategoryTextView.text =
                        ctg.nameRus

                } else
                    paymentPlanningSubcategoryTextView.text =
                        ctg.nameEng
                categoryViewModel.clearCategory()
            }
        }


        view.findViewById<RelativeLayout>(R.id.paymentPlanningAddCategoryRelativeLayout).setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetCategoryFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetCategoryFragment()
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetCategoryFragment"
                )
            }

            view.findViewById<ImageView>(R.id.paymentPlanningAddCategoryIcon)
                .setImageResource(R.drawable.category)
            paymentPlanningSubcategoryTextView.text = ""
        }

        currentDateTime = Calendar.getInstance()
        val dateTimeFormat = SimpleDateFormat("dd.MM.y HH:mm", Locale.getDefault())

        view.findViewById<RelativeLayout>(R.id.paymentPlanningAddDateTimeRelativeLayout)
            .setOnClickListener {

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

                                paymentPlanningDateTimeTextView.text =
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

        view.findViewById<TextView>(R.id.paymentPlanningAddExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<RelativeLayout>(R.id.addPaymentPlanningRelativeLayout)
            .setOnClickListener {

                if (paymentPlanningTextInputEditText.text.toString() != "" && paymentPlanningNameEdit.text.toString() != ""
                    && paymentPlanningSubcategoryTextView.text.toString() != "" && paymentPlanningDateTimeTextView.text.toString() != ""
                ) {


                    var id = UUID.randomUUID().toString()

                    val random = Random()
                    val uid = random.nextInt(Int.MAX_VALUE)


                    val hashMap = hashMapOf<String, Any>(
                        "value" to paymentPlanningTextInputEditText.text.toString().toDouble(),
                        "name" to paymentPlanningNameEdit.text.toString(),
                        "categoryRu" to categ.nameRus!!,
                        "categoryEn" to categ.nameEng!!,
                        "icon" to categ.image!!,
                        "timestamp" to currentDateTime.time,
                        "id" to id,
                        "idNotification" to uid.toString(),
                        "currency" to currencyPaymentPlanningSpinner.selectedItem,

                        "status" to "InProgress",
                    )





                    val documentRef = FirebaseFirestore.getInstance().collection("users")
                        .document(Firebase.auth.uid.toString())
                        .collection("paymentPlanning").document(id)
                    documentRef.set(hashMap as Map<String, Any>)
                        .addOnSuccessListener {

                            dismiss()
                        }
                        .addOnFailureListener { exception -> }


                    val alarmManager =
                        requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    val intent = Intent(context, NotificationReceiver::class.java)


                    val payment = PaymentPlanning().apply {
                        value = hashMap["value"] as Double
                        categoryRu = hashMap["categoryRu"] as String
                        categoryEn = hashMap["categoryEn"] as String
                        id = hashMap["id"] as String
                        idNotification = hashMap["idNotification"] as String
                        icon = hashMap["icon"] as String
                        currency = hashMap["currency"] as String
                        name = hashMap["name"] as String
                        status = hashMap["status"] as String
                        timestamp = Timestamp((hashMap["timestamp"] as Date))
                    }

                    val paymentPlanningString = Gson().toJson(payment)

                    intent.putExtra("paymentPlanning", paymentPlanningString)
                    intent.putExtra("uid", uid)
                    intent.putExtra("message", paymentPlanningNameEdit.text.toString())


                    val pendingIntent = PendingIntent.getBroadcast(
                        context,
                        uid,
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


}