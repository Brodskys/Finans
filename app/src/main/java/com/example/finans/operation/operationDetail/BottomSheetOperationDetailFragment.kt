package com.example.finans.operation.operationDetail

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.location.Geocoder
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import com.example.finans.R
import com.example.finans.authorization.authWithFacebook.AuthorizationPresenterFacebook.Companion.TAG
import com.example.finans.operation.Operation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class BottomSheetOperationDetailFragment : BottomSheetDialogFragment(){
    private lateinit var sharedPreferences: SharedPreferences
    var switchState: Boolean = false
    private lateinit var dbref: FirebaseFirestore
    private lateinit var currentDateTime: Calendar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {
            it.behavior.peekHeight  = R.style.AppBottomSheetDialogTheme
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_operation_detail, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_operation_detail, container, false)
        }

    }

    companion object {
        fun newInstance(operation: Operation): BottomSheetOperationDetailFragment {
            val args = Bundle()
            args.putParcelable("operation", operation)

            val fragment = BottomSheetOperationDetailFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout: TabLayout = view.findViewById(R.id.operationDetail_tab_layout)

        if(switchState) {
            tabLayout.setTabTextColors(Color.WHITE, Color.rgb(211, 138, 6))
        }
        else{
            tabLayout.setTabTextColors(Color.BLACK, Color.rgb(150, 75, 0))
        }

        view.findViewById<TextView>(R.id.operationDetailExit).setOnClickListener {

            dismiss()

        }

        view.findViewById<TextView>(R.id.operationDetailDone).setOnClickListener {

            if(view.findViewById<EditText>(R.id.operationDetail_amount_field).text.toString().isNotEmpty()
                && view.findViewById<TextView>(R.id.operationDetail_subcategory_txt).text.toString().isNotEmpty()) {
                val value =
                    view.findViewById<EditText>(R.id.operationDetail_amount_field).text.toString().toDouble()
                val date = view.findViewById<TextView>(R.id.operationDetail_dateTimeTextView).text.toString()

                val note = view.findViewById<TextView>(R.id.operationDetail_note).text.toString()

                val category =
                    view.findViewById<TextView>(R.id.operationDetail_subcategory_txt).text.toString()

                val map = view.findViewById<TextView>(R.id.operationDetail_location).text.toString()


                val selectedTabPosition =
                    view.findViewById<TabLayout>(R.id.operationDetail_tab_layout).selectedTabPosition
                val selectedTab =
                    view.findViewById<TabLayout>(R.id.operationDetail_tab_layout).getTabAt(selectedTabPosition)

                uploadData(selectedTab?.text.toString(), value, Timestamp(currentDateTime.time), note, category, null,null,null)
            }


            dismiss()

        }

        tabLayout.addTab(tabLayout.newTab().setText(R.string.expense))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.income))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.translation))

        val operation = arguments?.getParcelable<Operation>("operation")

        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            if (tab?.text?.toString() == operation?.type) {
                tabLayout.selectTab(tab)
                break
            }
        }

        view.findViewById<EditText>(R.id.operationDetail_amount_field).text =Editable.Factory.getInstance().newEditable(operation?.value.toString())

        val sharedPref = context?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val selectedCurrency = sharedPref?.getString("currency", "")
        view.findViewById<Button>(R.id.currency_btn).text = selectedCurrency

        Picasso.get().load(operation?.image)
            .into(view.findViewById<ImageView>(R.id.operationDetail_categoryIcon))

        var imageRef = operation?.image?.let { FirebaseStorage.getInstance().getReferenceFromUrl(it) }

        imageRef?.downloadUrl?.addOnSuccessListener { uri ->
            Picasso.get().load(uri)
                .placeholder(R.drawable.category)
                .error(R.drawable.category)
                .into(view.findViewById<ImageView>(R.id.operationDetail_categoryIcon))
        }?.addOnFailureListener {
            view.findViewById<ImageView>(R.id.operationDetail_categoryIcon).setImageResource(R.drawable.category)
        }

        imageRef = operation?.photo?.let { FirebaseStorage.getInstance().getReferenceFromUrl(it) }

        imageRef?.downloadUrl?.addOnSuccessListener { uri ->
            Picasso.get().load(uri)
                .placeholder(R.drawable.photo)
                .error(R.drawable.photo)
                .into(view.findViewById<ImageView>(R.id.operationDetail_bill))
        }?.addOnFailureListener {
            view.findViewById<ImageView>(R.id.operationDetail_bill).setImageResource(R.drawable.photo)
        }

        view.findViewById<TextView>(R.id.operationDetail_subcategory_txt).text = operation?.category


        val date = operation?.timestamp?.toDate()
        val pattern = "dd.MM.yyyy HH:mm"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())


        val dateString = date?.let { simpleDateFormat.format(it) }

        val dateTimeTextView =  view.findViewById<TextView>(R.id.operationDetail_dateTimeTextView)

        dateTimeTextView.text = dateString



        currentDateTime = Calendar.getInstance()
        currentDateTime.time = operation?.timestamp?.toDate() ?: Date()

        val dateTimeFormat = SimpleDateFormat("dd/MM/y HH:mm", Locale.getDefault())

        view.findViewById<RelativeLayout>(R.id.operationDetail_dateTimeRelativeLayout).setOnClickListener {


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



                            dateTimeTextView.text = dateTimeFormat.format(currentDateTime.time)

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
            datePickerDialog.datePicker.maxDate = Calendar.getInstance().timeInMillis

            datePickerDialog.show()


        }







        view.findViewById<TextView>(R.id.operationDetail_note).text = operation?.note

        if(operation?.map!=null) {
            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses = geocoder.getFromLocation(operation.map!!.latitude, operation.map!!.longitude, 1)
            val address = addresses?.get(0)?.getAddressLine(0)

            view.findViewById<TextView>(R.id.operationDetail_location).text = address
        }



    }

    private fun uploadData(operation: String, value: Double, dateTime: Timestamp, note: String?, category: String, image:String?, map:GeoPoint?, photo:String?) {

        val db = Firebase.firestore

        val oper = arguments?.getParcelable<Operation>("operation")

        val docRef =
            oper?.id?.let {
                db.collection("users").document(Firebase.auth.uid.toString()).collection("operation").document(
                    it
                )
            }

       val note = note ?: oper?.note
       val image = image ?: oper?.image
       val map = map ?: oper?.map
       val photo = photo ?: oper?.photo

        val data = hashMapOf<String, Any>(
            "type" to operation,
            "value" to value,
            "date" to  dateTime,
            "note" to note!!,
            "category" to category,
            "image" to image!!,
            "map" to map!!,
            "photo" to photo!!,
        )




        docRef?.update(data)?.addOnSuccessListener {
            Log.d(TAG, "DocumentSnapshot successfully updated!")
        }?.addOnFailureListener { e ->
            Log.w(TAG, "Error updating document", e)
        }

    }


    private fun getCategoryData() {
        dbref = FirebaseFirestore.getInstance()

        val operation = arguments?.getParcelable<Operation>("operation")


    }










}