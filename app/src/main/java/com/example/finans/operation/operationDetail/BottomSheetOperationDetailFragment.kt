package com.example.finans.operation.operationDetail

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.finans.R
import com.example.finans.accounts.AccountsViewModel
import com.example.finans.accounts.BottomSheetAccounts
import com.example.finans.authorization.authWithFacebook.AuthorizationPresenterFacebook.Companion.TAG
import com.example.finans.category.BottomSheetCategoryFragment
import com.example.finans.category.Category
import com.example.finans.category.CategoryViewModel
import com.example.finans.image.BottomSheetPhotoFragment
import com.example.finans.image.ImageInfo
import com.example.finans.image.ImageViewModel
import com.example.finans.language.languageInit
import com.example.finans.map.BottomSheetMapFragment
import com.example.finans.map.MapViewModel
import com.example.finans.operation.Operation
import com.example.finans.other.deletionWarning
import com.example.finans.plans.budgets.Budgets
import com.example.finans.сurrency.BottomSheetCurrencyFragment
import com.example.finans.сurrency.CurrencyViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID


class BottomSheetOperationDetailFragment : BottomSheetDialogFragment() {
    private lateinit var sharedPreferences: SharedPreferences
    var switchState: Boolean = false
    private lateinit var currentDateTime: Calendar
    private lateinit var amount: EditText
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var mapViewModel: MapViewModel
    private var map: GeoPoint? = null
    private lateinit var imageViewModel: ImageViewModel
    private var photo: ByteArray? = null
    private var oldPhoto: Uri? = null
    private var category: Category? = null
    private lateinit var operation: Operation
    private lateinit var db: FirebaseFirestore
    private lateinit var pref: SharedPreferences
    private lateinit var accountsViewModel: AccountsViewModel
    private var accountName: String = ""
    private lateinit var fullScreenImage: Uri

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {
            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        switchState = sharedPreferences.getBoolean("modeSwitch", false)
        pref = requireActivity().getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)
        return if (switchState) {
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_operation_detail, container, false)
        } else {
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

    @SuppressLint("CutPasteId", "SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout: TabLayout = view.findViewById(R.id.operationDetail_tab_layout)
        operation = arguments?.getParcelable("operation")!!

        accountName = operation.account!!

        db = Firebase.firestore

        val setngs = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        db.firestoreSettings = setngs

        tabLayout.addTab(tabLayout.newTab().setText(R.string.expense))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.income))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.translation))

        if (switchState) {
            tabLayout.setTabTextColors(Color.WHITE, Color.rgb(211, 138, 6))
        } else {
            tabLayout.setTabTextColors(Color.BLACK, Color.rgb(150, 75, 0))
        }

        view.findViewById<TextView>(R.id.operationDetailExit).setOnClickListener {

            dismiss()

        }

        val operationDetail_tab_layout =
            view.findViewById<TabLayout>(R.id.operationDetail_tab_layout)
        val selectedTabPosition =
            operationDetail_tab_layout.selectedTabPosition
        val selectedTab =
            operationDetail_tab_layout.getTabAt(selectedTabPosition)

        val operationDetail_subcategory_txt =
            view.findViewById<TextView>(R.id.operationDetail_subcategory_txt)

        val note = view.findViewById<TextView>(R.id.operationDetail_note).text.toString()

        amount = view.findViewById(R.id.operationDetail_amount_field)


        val ss = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)

        val locale = ss.getString("locale", "")

        accountsViewModel = ViewModelProvider(requireActivity())[AccountsViewModel::class.java]
        accountsViewModel.getSelectedAccounts().observe(this) { acc ->
            if (acc != null) {

                if (locale == "ru") {

                    view.findViewById<TextView>(R.id.accountName_operationDetail).text = acc.nameRus

                } else {
                    view.findViewById<TextView>(R.id.accountName_operationDetail).text = acc.nameEng
                }

                val gsReference = Firebase.storage.getReferenceFromUrl(acc.icon!!)

                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString())
                        .into(view.findViewById<ImageView>(R.id.accountIcon_operationDetail))
                }.addOnFailureListener {
                    Picasso.get().load(R.drawable.coins)
                        .into(view.findViewById<ImageView>(R.id.accountIcon_operationDetail))
                }
                view.findViewById<Button>(R.id.operationDetail_currency_btn).text = acc.currency
                update()
                accountName = acc.name!!
                accountsViewModel.clearAccounts()
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0 -> {
                            amount.setTextColor(Color.rgb(255, 0, 0))
                            amount.setHintTextColor(Color.rgb(255, 0, 0))
                        }

                        1 -> {
                            amount.setTextColor(Color.rgb(0, 128, 0))
                            amount.setHintTextColor(Color.rgb(0, 128, 0))
                        }

                        2 -> {
                            amount.setTextColor(Color.rgb(0, 0, 255))
                            amount.setHintTextColor(Color.rgb(0, 0, 255))
                        }
                    }
                    update()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            if (tab?.text == operation.typeRu || tab?.text == operation.typeEn) {
                tab!!.select()
                when (tab.position) {
                    0 -> {
                        amount.setTextColor(Color.rgb(255, 0, 0))
                        amount.setHintTextColor(Color.rgb(255, 0, 0))
                    }

                    1 -> {
                        amount.setTextColor(Color.rgb(0, 128, 0))
                        amount.setHintTextColor(Color.rgb(0, 128, 0))
                    }

                    2 -> {
                        amount.setTextColor(Color.rgb(0, 0, 255))
                        amount.setHintTextColor(Color.rgb(0, 0, 255))
                    }
                }
                break
            }
        }

        var raz = false

        amount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }

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

                if (raz) {
                    update()
                } else raz = true
            }
        })



        view.findViewById<RelativeLayout>(R.id.operationDetailAccountsAddBtn).setOnClickListener {
            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAccounts")
            if (existingFragment == null) {
                val newFragment = BottomSheetAccounts.newInstance("newOper")
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAccounts"
                )
            }
        }


        amount.setText(operation.value.toString())

        val s = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val accounts = s.getString("accounts", "")

        val docRef = db.collection("users").document(Firebase.auth.uid.toString())
            .collection("accounts")
            .document(accounts!!)

        docRef.get()
            .addOnSuccessListener { snapshot ->

                val accountName = view.findViewById<TextView>(R.id.accountName_operationDetail)
                val accountIcon = view.findViewById<ImageView>(R.id.accountIcon_operationDetail)

                val locale = s.getString("locale", "")

                if (locale == "ru") {

                    accountName.text = snapshot!!.getString("nameRus")

                } else {
                    accountName.text = snapshot!!.getString("nameEng")
                }

                val gsReference = Firebase.storage.getReferenceFromUrl(snapshot.getString("icon")!!)

                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString()).into(accountIcon)
                }.addOnFailureListener {
                    Picasso.get().load(R.drawable.coins).into(accountIcon)
                }
                view.findViewById<Button>(R.id.operationDetail_currency_btn).text =
                    snapshot.getString("currency")

            }

        val documentRef = FirebaseFirestore.getInstance().document("users/${Firebase.auth.uid.toString()}${operation.category}")

        documentRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val documentData = documentSnapshot.data

                    val image = documentData!!["image"].toString()
                   val categoryEn = documentData["nameEng"].toString()
                    val categoryRu = documentData["nameRus"].toString()

                    val gsReference = Firebase.storage.getReferenceFromUrl(image)

                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri)
                            .placeholder(R.drawable.category)
                            .error(R.drawable.category)
                            .into(view.findViewById<ImageView>(R.id.operationDetail_categoryIcon))
                    }.addOnFailureListener {
                        view.findViewById<ImageView>(R.id.operationDetail_categoryIcon)
                            .setImageResource(R.drawable.category)
                    }

                    if (locale == "ru"){
                        view.findViewById<TextView>(R.id.operationDetail_subcategory_txt).text = categoryRu
                    } else {
                        view.findViewById<TextView>(R.id.operationDetail_subcategory_txt).text = categoryEn
                    }

                }
            }
            .addOnFailureListener { exception ->

            }


        try {
          val  imageRef =
                operation.photo?.let { FirebaseStorage.getInstance().getReferenceFromUrl(it) }

            imageRef?.downloadUrl?.addOnSuccessListener { uri ->
                oldPhoto = uri
                fullScreenImage = uri
                Picasso.get().load(uri)
                    .placeholder(R.drawable.photo)
                    .error(R.drawable.photo)
                    .into(view.findViewById<ImageView>(R.id.operationDetail_bill))
            }?.addOnFailureListener {
                view.findViewById<ImageView>(R.id.operationDetail_bill)
                    .setImageResource(R.drawable.photo)
            }
        } catch (e: IllegalArgumentException) {
            Log.d(TAG, e.message.toString())
        }



        view.findViewById<RelativeLayout>(R.id.operationDetail_relativeLayoutPhoto)
            .setOnClickListener {

                val existingFragment =
                    requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetPhotoFragment")

                if (existingFragment == null) {

                    val newFragment = BottomSheetPhotoFragment.newInstance(
                        "photo",
                        photo == null && oldPhoto == null
                    )
                    newFragment.setTargetFragment(this@BottomSheetOperationDetailFragment, 0)

                    newFragment.show(
                        requireActivity().supportFragmentManager,
                        "BottomSheetPhotoFragment"
                    )
                }
            }

        imageViewModel = ViewModelProvider(requireActivity())[ImageViewModel::class.java]

        imageViewModel.cameraImageUri.observe(viewLifecycleOwner) { uri ->

            if (uri != null) {
                if (uri.cameraGallery == "camera") {
                    imageLoad(uri)
                    update()

                } else if (uri.cameraGallery == "delete") {
                    photo = null
                    oldPhoto = null
                    view.findViewById<ImageView>(R.id.operationDetail_bill)
                        .setImageResource(R.drawable.photo)
                    update()

                }
            }

        }

        imageViewModel.galleryImageUri.observe(viewLifecycleOwner) { uri ->

            if (uri != null) {
                if (uri.cameraGallery == "gallery") {
                    imageLoad(uri)
                    update()

                } else if (uri.cameraGallery == "delete") {
                    photo = null
                    oldPhoto = null
                    view.findViewById<ImageView>(R.id.operationDetail_bill)
                        .setImageResource(R.drawable.photo)
                    update()

                }
            }

        }

        view.findViewById<ImageView>(R.id.operationDetail_bill).setOnClickListener{

            if(photo!= null || oldPhoto!= null){

                val dialog = Dialog(requireContext())
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window?.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT)

                val imageView = ImageView(requireContext())
                imageView.adjustViewBounds = true
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER

                Picasso.get().load(fullScreenImage).into(imageView)

                dialog.setContentView(imageView)
                dialog.show()

            }

        }

        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]
        categoryViewModel.getSelectedCategory().observe(this) { ctg ->
            if (ctg != null) {
                val storage = Firebase.storage

                val gsReference = storage.getReferenceFromUrl(ctg.image!!)

                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString())
                        .into(view.findViewById<ImageView>(R.id.operationDetail_categoryIcon))
                }.addOnFailureListener {
                    Picasso.get().load(R.drawable.category)
                        .into(view.findViewById<ImageView>(R.id.operationDetail_categoryIcon))
                }
                if (languageInit(requireActivity())) {
                    view.findViewById<TextView>(R.id.operationDetail_subcategory_txt).text =
                        ctg.nameRus

                } else
                    view.findViewById<TextView>(R.id.operationDetail_subcategory_txt).text =
                        ctg.nameEng

                category = ctg
                update()

                categoryViewModel.clearCategory()
            }
        }

        view.findViewById<RelativeLayout>(R.id.operationDetail_categoryRelativeLayout)
            .setOnClickListener {

                val existingFragment =
                    requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetCategoryFragment")
                if (existingFragment == null) {
                    val newFragment = BottomSheetCategoryFragment()
                    newFragment.show(
                        requireActivity().supportFragmentManager,
                        "BottomSheetCategoryFragment"
                    )
                }
            }

        val date = operation.timestamp?.toDate()
        val pattern = "dd.MM.yyyy HH:mm"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())


        val dateString = date?.let { simpleDateFormat.format(it) }

        val dateTimeTextView = view.findViewById<TextView>(R.id.operationDetail_dateTimeTextView)

        dateTimeTextView.text = dateString



        currentDateTime = Calendar.getInstance()
        currentDateTime.time = operation.timestamp?.toDate() ?: Date()

        val dateTimeFormat = SimpleDateFormat("dd.MM.y HH:mm", Locale.getDefault())

        view.findViewById<RelativeLayout>(R.id.operationDetail_dateTimeRelativeLayout)
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



                                dateTimeTextView.text = dateTimeFormat.format(currentDateTime.time)
                                update()

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


        view.findViewById<RelativeLayout>(R.id.operationDetail_noteRelativeLayout)
            .setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.note)

                val input = EditText(requireContext())
                input.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE

                if (view.findViewById<TextView>(R.id.operationDetail_note).text.isNotEmpty()) {
                    input.setText(view.findViewById<TextView>(R.id.operationDetail_note).text)
                    input.setSelection(view.findViewById<TextView>(R.id.operationDetail_note).text.length)
                }

                builder.setView(input)

                builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
                    val noteText = input.text.toString().trim()

                    view.findViewById<TextView>(R.id.operationDetail_note).text = noteText
                    update()

                    dialog.dismiss()
                }
                builder.setNegativeButton(android.R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }

                builder.show()
            }




        view.findViewById<TextView>(R.id.operationDetail_note).text = operation.note

        val geoPoint = operation.map

        if (!isGeoPointEmpty(geoPoint)) {

            val geocoder = Geocoder(requireContext(), Locale.getDefault())
            val addresses =
                geocoder.getFromLocation(geoPoint!!.latitude, geoPoint.longitude, 1)
            val address = addresses?.get(0)?.getAddressLine(0)

            view.findViewById<TextView>(R.id.operationDetail_location).text = address

            if (switchState) {
                view.findViewById<ImageView>(R.id.operationDetail_mapImg)
                    .setImageResource(R.drawable.delete_dark)
            } else {
                view.findViewById<ImageView>(R.id.operationDetail_mapImg)
                    .setImageResource(R.drawable.delete)
            }

        }

        mapViewModel = ViewModelProvider(requireActivity())[MapViewModel::class.java]
        mapViewModel.getSelectedMap().observe(this) { geo ->
            if (geo != null) {
                map = geo
                val geocoder = Geocoder(requireContext(), Locale.getDefault())

                val addresses = geocoder.getFromLocation(map!!.latitude, map!!.longitude, 1)
                val address = addresses?.get(0)?.getAddressLine(0)

                view.findViewById<TextView>(R.id.operationDetail_location).text = address

                if (switchState) {
                    view.findViewById<ImageView>(R.id.operationDetail_mapImg)
                        .setImageResource(R.drawable.delete_dark)
                } else {
                    view.findViewById<ImageView>(R.id.operationDetail_mapImg)
                        .setImageResource(R.drawable.delete)
                }

                mapViewModel.clearMap()
                update()

            }
        }


        view.findViewById<ImageView>(R.id.operationDetail_mapImg).setOnClickListener {

            if (map != GeoPoint(0.0, 0.0)) {
                map = GeoPoint(0.0, 0.0)

                view.findViewById<TextView>(R.id.operationDetail_location).text = null


                if (switchState) {
                    view.findViewById<ImageView>(R.id.operationDetail_mapImg)
                        .setImageResource(R.drawable.right_dark)
                } else {
                    view.findViewById<ImageView>(R.id.operationDetail_mapImg)
                        .setImageResource(R.drawable.right)
                }
                update()
            }
        }

        view.findViewById<RelativeLayout>(R.id.operationDetail_relativeLayoutLocation)
            .setOnClickListener {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.ACCESS_FINE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {

                    val isConnected = isInternetConnected(requireContext())
                    if (isConnected) {
                        val existingFragment =
                            requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetMapFragment")
                        if (existingFragment == null) {
                            val newFragment = BottomSheetMapFragment()
                            newFragment.show(
                                requireActivity().supportFragmentManager,
                                "BottomSheetMapFragment"
                            )
                        }
                    } else {
                        val builder = androidx.appcompat.app.AlertDialog.Builder(view.context)

                        builder.setTitle(R.string.error)

                        builder.setMessage(R.string.internetConnection)

                        builder.setNegativeButton(
                            "Ok"
                        ) { dialog, id ->
                        }
                        builder.show()
                    }


                } else {
                    ActivityCompat.requestPermissions(
                        requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        BottomSheetMapFragment.LOCATION_REQUEST_CODE
                    )
                }
            }



        view.findViewById<TextView>(R.id.operationDetailDone).setOnClickListener {

            if (amount.text.toString().isNotEmpty()
                && operationDetail_subcategory_txt.text.toString().isNotEmpty()
            ) {

                uploadData(
                    amount.text.toString().toDouble(),
                    Timestamp(currentDateTime.time),
                    note
                )
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


        view.findViewById<TextView>(R.id.delete_btn).setOnClickListener {

            deletionWarning(requireContext()) { result ->

                if (result) {
                    val accounts = pref.getString("accounts", "")

                    val docRef = operation.id?.let {
                        db.collection("users").document(Firebase.auth.uid.toString())
                            .collection("accounts")
                            .document(accounts!!)
                            .collection("operation").document(it)
                    }

                    docRef?.delete()
                        ?.addOnSuccessListener {
                            Log.d(TAG, "DocumentSnapshot successfully deleted!")
                        }
                        ?.addOnFailureListener { e ->
                            Log.w(TAG, "Error deleting document", e)
                        }

                    if (operation.photo != "") {
                        val photoRef =
                            FirebaseStorage.getInstance().getReferenceFromUrl(operation.photo!!)

                        photoRef.delete()
                            .addOnSuccessListener {
                            }
                            .addOnFailureListener {
                            }
                    }

                    if (isAdded) {
                        dismiss()
                    }
                }
            }
        }


        view.findViewById<TextView>(R.id.update_btn).setOnClickListener {

            if (amount.text.toString().isNotEmpty()
                && operationDetail_subcategory_txt.text.toString().isNotEmpty()
            ) {

                uploadData(

                    amount.text.toString().toDouble(),
                    Timestamp(currentDateTime.time),
                    note
                )
            } else {
                val builder = AlertDialog.Builder(requireContext())
                builder.setTitle(R.string.error)
                builder.setMessage("Заполните сумму и категорию")
                builder.setIcon(android.R.drawable.ic_dialog_alert)
                builder.setPositiveButton("OK", null)
                val dialog = builder.create()
                dialog.show()
            }
        }

    }


    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    fun isGeoPointEmpty(geoPoint: GeoPoint?): Boolean {
        return geoPoint == null || (geoPoint.latitude == 0.0 && geoPoint.longitude == 0.0)
    }

    private fun uploadData(
        value: Double,
        dateTime: Timestamp,
        note: String?
    ) {


        var path: String? = ""

        val mp = map ?: operation.map

        val uid = UUID.randomUUID().toString()
        path =
            photo?.let { "gs://finans-44544.appspot.com/images/${Firebase.auth.uid.toString()}/operation/${uid}.jpg" }
                ?: ""

        if (path != "") {

            val storageRef = Firebase.storage.reference
            val imagesRef =
                storageRef.child("images/${Firebase.auth.uid.toString()}/operation/${uid}.jpg")

            val uploadTask = imagesRef.putBytes(photo!!)
            uploadTask.addOnSuccessListener { taskSnapshot ->
                taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                }
            }
        } else if (oldPhoto != null) {
            path = operation.photo
        } else {
            if (operation.photo != "") {
                val photoRef =
                    FirebaseStorage.getInstance().getReferenceFromUrl(operation.photo!!)

                photoRef.delete()
                    .addOnSuccessListener {
                    }
                    .addOnFailureListener {
                    }
            }
            path = ""
        }

        var operationRu = ""
        var operationEng = ""

        val operationDetail_tab_layout =
            view?.findViewById<TabLayout>(R.id.operationDetail_tab_layout)
        val selectedTabPosition =
            operationDetail_tab_layout!!.selectedTabPosition
        val selectedTab =
            operationDetail_tab_layout.getTabAt(selectedTabPosition)



        when (selectedTab?.text.toString()) {
            getString(R.string.income) -> {
                operationEng = "Income"
                operationRu = "Доход"
            }

            getString(R.string.expense) -> {
                operationEng = "Expense"
                operationRu = "Расход"
            }

            getString(R.string.translation) -> {
                operationEng = "Translation"
                operationRu = "Перевод"
            }
        }

        val url = category?.url ?: operation.category

        val data = hashMapOf<String, Any>(
            "account" to accountName,
            "typeRu" to operationRu,
            "typeEn" to operationEng,
            "value" to value,
            "timestamp" to dateTime,
            "note" to note!!,
            "category" to url!!,
            "map" to mp!!,
            "photo" to path!!,
        )

        val accounts = pref.getString("accounts", "")

        val userId = FirebaseFirestore.getInstance().collection("users")
            .document(Firebase.auth.uid.toString())

        val sourceCollectionRef = userId.collection("accounts")
            .document(operation.account!!)
            .collection("operation")

        val targetCollectionRef = userId.collection("accounts")
            .document(accountName)
            .collection("operation")


        val docRef =
            operation.id?.let {
                db.collection("users").document(Firebase.auth.uid.toString()).collection("accounts")
                    .document(accounts!!)
                    .collection("operation")
                    .document(
                        it
                    )
            }

        docRef?.update(data)?.addOnSuccessListener {

            val isTypeChanged = operation.typeEn != operationEng
            val isAmountChanged = operation.value != value

            val budgetsCollectionRef =
                Firebase.firestore.collection("users")
                    .document(Firebase.auth.uid.toString())
                    .collection("budgets")

            val hashMap = hashMapOf<String, Any>(
                "id" to operation.id!!,
                "value" to value,
                "timestamp" to dateTime
            )




            budgetsCollectionRef
                .whereGreaterThan("timeEnd", Timestamp.now())
                .get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val data =
                            document.toObject(Budgets::class.java)

                        val accountsList = data!!.accounts

                        if (accountsList != null && (accountsList.isEmpty() || accounts in accountsList)) {

                            val categoriesList = data.categories

                            val parts = operation.category!!.split("/")
                            val categoryName = parts.getOrNull(2)

                            if (categoriesList != null && (categoriesList.isEmpty() || categoryName in categoriesList)) {

                                if(operation.timestamp != dateTime){
                                    budgetsCollectionRef.document(data.id!!)
                                        .collection("operation")
                                        .document(operation.id!!)
                                        .update(hashMap)
                                        .addOnSuccessListener {}
                                }


                                if (isTypeChanged) {
                                    if(operation.typeEn == "Income" && operationEng == "Expense") {
                                            budgetsCollectionRef.document(data.id!!)
                                                .update(
                                                    "valueNow",
                                                    FieldValue.increment(operation.value!!)
                                                )
                                                .addOnSuccessListener {
                                                    budgetsCollectionRef
                                                        .document(data.id!!)
                                                        .collection("operation").document(operation.id!!)
                                                        .set(hashMap)
                                                        .addOnSuccessListener { documentReference ->
                                                        }
                                                }
                                                .addOnFailureListener {}

                                    }
                                    if(operation.typeEn == "Expense" && (operationEng == "Income" || operationEng == "Translation")) {

                                        budgetsCollectionRef.document(data.id!!)
                                            .update(
                                                "valueNow",
                                                FieldValue.increment(-value)
                                            )
                                            .addOnSuccessListener {
                                                budgetsCollectionRef.document(data.id!!)
                                                    .collection("operation")
                                                    .document(operation.id!!)
                                                    .delete()
                                                    .addOnSuccessListener {}
                                            }
                                            .addOnFailureListener {}
                                    }
                                }
                                else{

                                    if(isAmountChanged){

                                       if(operationEng == "Expense") {

                                           budgetsCollectionRef.document(data.id!!)
                                               .collection("operation")
                                               .document(operation.id!!)
                                               .update(hashMap)
                                               .addOnSuccessListener {}

                                           budgetsCollectionRef.document(data.id!!)
                                               .update(
                                                   "valueNow",
                                                   FieldValue.increment(-operation.value!!)
                                               )
                                               .addOnSuccessListener {
                                                   budgetsCollectionRef.document(data.id!!)
                                                       .update(
                                                           "valueNow",
                                                           FieldValue.increment(value)
                                                       )
                                                       .addOnSuccessListener {

                                                       }
                                                       .addOnFailureListener {}
                                               }
                                               .addOnFailureListener {}
                                       }
                                    }

                                }

                            }
                        }
                    }
                }
                .addOnFailureListener { e ->

                }







            val userRef = userId.collection("user").document("information")


            userRef.get()
                .addOnSuccessListener { snapshot ->

                    var balance = snapshot.getDouble("total_balance")
                    if (balance != null) {

                        var money = if (operation.typeEn == "Income") -operation.value!!  else operation.value

                        balance += money!!

                        if (isTypeChanged || isAmountChanged) {

                             money = if (operationEng == "Income") value else -value

                             balance += money

                            userId
                                .collection("user").document("information")
                                .update("total_balance", balance)
                                .addOnSuccessListener {}
                                .addOnFailureListener {}
                        }
                    }
                }
            userId
                .collection("accounts").document(accountName).get()
                .addOnSuccessListener { snapshot ->

                    var balance = snapshot.getDouble("balance")
                    if (balance != null) {

                        var money = if (operation.typeEn == "Income") -operation.value!!  else operation.value

                        balance += money!!

                        if (isTypeChanged || isAmountChanged) {

                            money = if (operationEng == "Income") value else -value

                            balance += money!!

                            userId
                                .collection("accounts").document(accountName)
                                .update("balance", balance)
                                .addOnSuccessListener {}
                                .addOnFailureListener {}
                        }
                    }
                }

        }?.addOnFailureListener { e ->
            Log.w("Update", "Error updating document", e)
        }

        if (operation.account != accountName) {

            val sourceDocumentRef = sourceCollectionRef.document(operation.id!!)

            sourceDocumentRef.get().addOnSuccessListener { sourceDocumentSnapshot ->
                if (sourceDocumentSnapshot.exists()) {
                    val data = sourceDocumentSnapshot.data

                    val targetDocumentRef = targetCollectionRef.document(operation.id!!)
                    targetDocumentRef.set(data!!).addOnSuccessListener {

                        sourceDocumentRef.delete().addOnSuccessListener {
                            dismiss()

                        }.addOnFailureListener { exception ->
                        }
                    }.addOnFailureListener { exception ->
                    }
                } else {
                }
            }.addOnFailureListener { exception ->
            }
        } else {

            dismiss()
        }


    }

    fun update() {
        view?.findViewById<TextView>(R.id.operationDetailDone)?.isVisible = true
        view?.findViewById<Button>(R.id.delete_btn)?.isVisible = false
    }

    private fun imageLoad(uri: ImageInfo?) {
        if (uri?.uri != null) {
            if (uri.type == "photo") {
                fullScreenImage = uri.uri
                val inputStream = requireContext().contentResolver.openInputStream(uri.uri)
                var bitmap = BitmapFactory.decodeStream(inputStream)

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                photo = baos.toByteArray()

                bitmap = BitmapFactory.decodeByteArray(photo, 0, photo!!.size)

                oldPhoto = null

                requireView().findViewById<ImageView>(R.id.operationDetail_bill)
                    .setImageBitmap(bitmap)
            }

            imageViewModel.clearGalleryImage()
            imageViewModel.clearCameraImage()

        }

    }


}