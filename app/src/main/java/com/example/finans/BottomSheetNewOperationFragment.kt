package com.example.finans

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.finans.category.BottomSheetCategoryFragment
import com.example.finans.category.Category
import com.example.finans.category.CategoryViewModel
import com.example.finans.language.languageInit
import com.example.finans.сurrency.BottomSheetCurrencyFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*

class BottomSheetNewOperationFragment : BottomSheetDialogFragment(){

    private val REQUEST_IMAGE_CAPTURE = 1
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var mapViewModel: MapViewModel
    private lateinit var imageViewModel: ImageViewModel
    private var image : String= ""
    private lateinit var sharedPreferences: SharedPreferences
    private var switchState: Boolean = true
    private var photo: ByteArray? = null

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
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_new_operation, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_new_operation, container, false)
        }


    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)

        if(switchState) {
            tabLayout.setTabTextColors(Color.WHITE, Color.rgb(211, 138, 6))
        }
        else{
            tabLayout.setTabTextColors(Color.BLACK, Color.rgb(150, 75, 0))
        }
        tabLayout.addTab(tabLayout.newTab().setText(R.string.expense))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.income))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.translation))


        mapViewModel = ViewModelProvider(requireActivity())[MapViewModel::class.java]
        mapViewModel.getSelectedMap().observe(this) { map ->

            view.findViewById<TextView>(R.id.locationTextView).text = map
        }

        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]
        categoryViewModel.getSelectedCategory().observe(this) { category ->

            val storage = Firebase.storage
            image = category.Image!!

            val gsReference = storage.getReferenceFromUrl(image)

            gsReference.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri.toString()).into(view.findViewById<ImageView>(R.id.categoryIcon))
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.category).into(view.findViewById<ImageView>(R.id.categoryIcon))
            }
            if (languageInit(requireActivity())) {
                view.findViewById<TextView>(R.id.subcategory_txt).text = category.NameRus

            } else
                view.findViewById<TextView>(R.id.subcategory_txt).text = category.NameEng

        }



        imageViewModel = ViewModelProvider(requireActivity())[ImageViewModel::class.java]

        imageViewModel.cameraImageUri.observe(viewLifecycleOwner) { uri ->

            imageLoad(uri)

        }

        imageViewModel.galleryImageUri.observe(viewLifecycleOwner) { uri ->

            imageLoad(uri)

        }


        val linearLayout = tabLayout.getChildAt(0) as LinearLayout
        linearLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        val drawable = GradientDrawable()
        drawable.setColor(Color.BLACK)
        drawable.setSize(2, 2)
        linearLayout.dividerPadding = 20
        linearLayout.dividerDrawable = drawable


        view.findViewById<EditText>(R.id.amount_field).addTextChangedListener(object : TextWatcher {
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
                    view.findViewById<EditText>(R.id.amount_field).error = "Incorrect input format"
                    s?.replace(s.length - 1, s.length, "")
                }
            }
        })

        val sharedPref = context?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val selectedCurrency = sharedPref?.getString("currency", "")
        view.findViewById<Button>(R.id.currency_btn).text = selectedCurrency


        view.findViewById<Button>(R.id.currency_btn).setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetCurrencyFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetCurrencyFragment()
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetCurrencyFragment"
                )
            }
            dismiss()
        }








        view.findViewById<Button>(R.id.good_btn).setOnClickListener {
            if(view.findViewById<EditText>(R.id.amount_field).text.toString().isNotEmpty()) {
                val value =
                    view.findViewById<EditText>(R.id.amount_field).text.toString().toDouble()
                val date = view.findViewById<TextView>(R.id.dateTimeTextView).text.toString()

                val note = view.findViewById<TextView>(R.id.noteTextView).text.toString()

                val category =
                    view.findViewById<TextView>(R.id.subcategory_txt).text.toString()

                val map = view.findViewById<TextView>(R.id.locationTextView).text.toString()


                val selectedTabPosition =
                    view.findViewById<TabLayout>(R.id.tab_layout).selectedTabPosition
                val selectedTab =
                    view.findViewById<TabLayout>(R.id.tab_layout).getTabAt(selectedTabPosition)

                uploadData(selectedTab?.text.toString(), value, date, note, category,image,map)
            }
        }

        view.findViewById<TextView>(R.id.newOperationExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.newOperationDone).setOnClickListener {
            if (view.findViewById<EditText>(R.id.amount_field).text.toString().isNotEmpty()) {
                val value =
                    view.findViewById<EditText>(R.id.amount_field).text.toString().toDouble()
                val date = view.findViewById<TextView>(R.id.dateTimeTextView).text.toString()

                val note = view.findViewById<TextView>(R.id.noteTextView).text.toString()

                val category = view.findViewById<TextView>(R.id.subcategory_txt).text.toString()

                val map = view.findViewById<TextView>(R.id.locationTextView).text.toString()


                val selectedTabPosition =
                    view.findViewById<TabLayout>(R.id.tab_layout).selectedTabPosition
                val selectedTab =
                    view.findViewById<TabLayout>(R.id.tab_layout).getTabAt(selectedTabPosition)

                uploadData(selectedTab?.text.toString(), value, date, note,category,image,map)
            }
        }

        val dateTimeTextView =  view.findViewById<TextView>(R.id.dateTimeTextView)
        val dateTimeFormat = SimpleDateFormat("dd/MM/y HH:mm", Locale.getDefault())
        dateTimeTextView.text = dateTimeFormat.format(Date())

        view.findViewById<RelativeLayout>(R.id.dateTimeRelativeLayout).setOnClickListener {
                val currentDateTime = Calendar.getInstance()

            val maxDateTime = Calendar.getInstance()

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
                datePickerDialog.datePicker.maxDate = maxDateTime.timeInMillis

                datePickerDialog.show()
            }


        view.findViewById<RelativeLayout>(R.id.noteRelativeLayout).setOnClickListener {
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle(R.string.note)

            val input = EditText(requireContext())
            input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

            if (view.findViewById<TextView>(R.id.noteTextView).text.isNotEmpty()) {
                input.setText(view.findViewById<TextView>(R.id.noteTextView).text)
                input.setSelection(view.findViewById<TextView>(R.id.noteTextView).text.length)
            }

            builder.setView(input)

            builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                val noteText = input.text.toString().trim()

                view.findViewById<TextView>(R.id.noteTextView).text = noteText

                dialog.dismiss()
            }
            builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
        }


        view.findViewById<RelativeLayout>(R.id.relativeLayoutLocation).setOnClickListener{
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                val existingFragment =
                    requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetMapFragment")
                if (existingFragment == null) {
                    val newFragment = BottomSheetMapFragment()
                    newFragment.show(
                        requireActivity().supportFragmentManager,
                        "BottomSheetMapFragment"
                    )
                }
            }else {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    BottomSheetMapFragment.LOCATION_REQUEST_CODE
                )
            }
        }

        view.findViewById<RelativeLayout>(R.id.relativeLayoutPhoto).setOnClickListener{

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetPhotoFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetPhotoFragment()
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetPhotoFragment"
                )
            }

        }

        view.findViewById<RelativeLayout>(R.id.categoryRelativeLayout).setOnClickListener{


            val existingFragment = requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetCategoryFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetCategoryFragment()
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetCategoryFragment"
                )
            }
        }

    }

    private fun imageLoad(uri: Uri?){
        if(uri != null) {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            var bitmap = BitmapFactory.decodeStream(inputStream)

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            photo = baos.toByteArray()

            bitmap = BitmapFactory.decodeByteArray(photo, 0, photo!!.size)

            requireView().findViewById<ImageView>(R.id.currencyIcon4).setImageBitmap(bitmap)

            imageViewModel.clearGalleryImage()

        }
    }

    private fun addNewOperationToFirestore(operation: String, value: Double, dateTime: String, note: String, category: String, image: String, map: String, photo: String) {

        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        val time = Calendar.getInstance()
        time.time = format.parse(dateTime) as Date

        val hours = time.get(Calendar.HOUR_OF_DAY)
        val minutes = time.get(Calendar.MINUTE)

        val date = Calendar.getInstance()
        date.time = format.parse(dateTime) as Date

        val day = date.get(Calendar.DAY_OF_MONTH)
        val month = date.get(Calendar.MONTH) + 1
        val year = date.get(Calendar.YEAR)


        val hashMap = hashMapOf<String, Any>(
            "type" to operation,
            "value" to value,
            "date" to "$day/$month/$year",
            "time" to "$hours:$minutes",
            "note" to note,
            "category" to category,
            "image" to image,
            "map" to map,
            "photo" to photo,
        )


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



        fireStoreDatabase.collection("users").document(Firebase.auth.uid.toString())
            .collection("operation").document()
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val docId = document.id
                    hashMap["id"] = docId
                    fireStoreDatabase.collection("users").document(Firebase.auth.uid.toString())
                        .collection("operation").document(docId)
                        .set(hashMap)
                        .addOnSuccessListener {
                            Log.d("NewOperation", "Added document with ID $docId")
                            dismiss()
                        }
                        .addOnFailureListener { exception ->
                            Log.d("NewOperation", "Error adding document $exception")
                        }
                } else {
                    Log.d("NewOperation", "Document not found")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("NewOperation", "Error getting document: $exception")
            }

    }



    private fun uploadData(operation: String, value: Double, dateTime: String, note: String, category: String, image: String, map: String) {

        if (photo != null) {
            val storageRef = Firebase.storage.reference
            val imagesRef =
                storageRef.child("images/${Firebase.auth.uid.toString()}/operation/${UUID.randomUUID()}/photo.jpg")

            val uploadTask = imagesRef.putBytes(photo!!)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->

                    addNewOperationToFirestore(
                        operation,
                        value,
                        dateTime,
                        note,
                        category,
                        image,
                        map,
                        uri.toString()
                    )
                }
            }
        } else {
            addNewOperationToFirestore(operation, value, dateTime, note, category, image, map, "")
        }


    }




}