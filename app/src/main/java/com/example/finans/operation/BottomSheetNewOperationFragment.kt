package com.example.finans.operation

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.location.Geocoder
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.finans.R
import com.example.finans.accounts.Accounts
import com.example.finans.category.BottomSheetCategoryFragment
import com.example.finans.category.Category
import com.example.finans.category.CategoryViewModel
import com.example.finans.image.BottomSheetPhotoFragment
import com.example.finans.image.ImageInfo
import com.example.finans.image.ImageViewModel
import com.example.finans.image.ocr
import com.example.finans.image.parsing
import com.example.finans.language.languageInit
import com.example.finans.map.BottomSheetMapFragment
import com.example.finans.map.MapViewModel
import com.example.finans.accounts.AccountsViewModel
import com.example.finans.accounts.AccountsViewModel2
import com.example.finans.accounts.BottomSheetAccounts
import com.example.finans.operation.qr.BottomSheetQR
import com.example.finans.operation.qr.QrViewModel
import com.example.finans.plans.budgets.Budgets
import com.example.finans.plans.paymentPlanning.NotificationReceiver
import com.example.finans.plans.paymentPlanning.PaymentPlanning
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
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.util.*
import java.util.regex.Pattern

class BottomSheetNewOperationFragment : BottomSheetDialogFragment() {

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var mapViewModel: MapViewModel
    private lateinit var imageViewModel: ImageViewModel
    private lateinit var accountsViewModel: AccountsViewModel2
    private var accountName: Accounts? = null
    private lateinit var sharedPreferences: SharedPreferences
    private var switchState: Boolean = true
    private var photo: ByteArray? = null
    private lateinit var currentDateTime: Calendar
    private var map = GeoPoint(0.0, 0.0)
    private lateinit var db: FirebaseFirestore
    private var category: Category? = null
    private val requestCodeCameraPermission = 1001
    private var paymentPlanning: PaymentPlanning? = null
    private var toAccounts: Accounts? = null
    private lateinit var fullScreenImage: Uri

    private lateinit var dt: Timestamp

    private lateinit var amount: EditText
    private lateinit var dateTimeTextView: TextView
    private lateinit var qrViewModel: QrViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }
        dialog?.setCancelable(false)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        switchState = sharedPreferences.getBoolean("modeSwitch", false)


        return if (switchState) {
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_new_operation, container, false)
        } else {
            inflater.inflate(R.layout.fragment_bottom_sheet_new_operation, container, false)
        }

    }

    @SuppressLint("ResourceType", "CutPasteId")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        paymentPlanning = arguments?.getParcelable("paymentPlanning")
        dateTimeTextView = view.findViewById(R.id.dateTimeTextView)

        val dateTimeFormat = SimpleDateFormat("dd/MM/y HH:mm", Locale.getDefault())
        dateTimeTextView.text = dateTimeFormat.format(Date())

        val tabLayout: TabLayout = view.findViewById(R.id.tab_layout)

        db = Firebase.firestore

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        db.firestoreSettings = settings

        val s = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val accounts = s.getString("accounts", "")

        val docRef = db.collection("users").document(Firebase.auth.uid.toString())
            .collection("accounts")
            .document(accounts!!)

        val accountN = view.findViewById<TextView>(R.id.accountName_NewOperation)
        val accountIcon = view.findViewById<ImageView>(R.id.accountIcon_NewOperation)
        val locale = s.getString("locale", "")



        docRef.get()
            .addOnSuccessListener { snapshot ->

                accountName = snapshot.toObject(Accounts::class.java)

                if (locale == "ru") {

                    accountN.text = snapshot!!.getString("nameRus")

                } else {
                    accountN.text = snapshot!!.getString("nameEng")
                }

                val gsReference = Firebase.storage.getReferenceFromUrl(snapshot.getString("icon")!!)

                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString()).into(accountIcon)
                }.addOnFailureListener {
                    Picasso.get().load(R.drawable.coins).into(accountIcon)
                }
                view.findViewById<Button>(R.id.currency_btn).text = snapshot.getString("currency")

            }


        accountsViewModel = ViewModelProvider(requireActivity())[AccountsViewModel2::class.java]
        accountsViewModel.getSelectedAccounts().observe(this) { acc ->
            if (acc != null) {

                if (acc.type == "ac1") {

                    if (toAccounts?.name == acc.accounts!!.name) {
                        toAccounts = null
                        view.findViewById<ImageView>(R.id.toAcountIcon_NewOperation)
                            .setImageResource(R.drawable.towallet)
                        view.findViewById<TextView>(R.id.toAcountName_NewOperation).text = ""
                    }

                    if (locale == "ru") {

                        accountN.text = acc.accounts!!.nameRus

                    } else {
                        accountN.text = acc.accounts!!.nameEng
                    }

                    val gsReference = Firebase.storage.getReferenceFromUrl(acc.accounts.icon!!)

                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri.toString()).into(accountIcon)
                    }.addOnFailureListener {
                        Picasso.get().load(R.drawable.coins).into(accountIcon)
                    }
                    view.findViewById<Button>(R.id.currency_btn).text = acc.accounts.currency
                    accountName = acc.accounts

                } else if (acc.type == "ac2") {
                    if (locale == "ru") {

                        view.findViewById<TextView>(R.id.toAcountName_NewOperation).text =
                            acc.accounts!!.nameRus

                    } else {
                        view.findViewById<TextView>(R.id.toAcountName_NewOperation).text =
                            acc.accounts!!.nameEng
                    }

                    val gsReference = Firebase.storage.getReferenceFromUrl(acc.accounts.icon!!)

                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri.toString())
                            .into(view.findViewById<ImageView>(R.id.toAcountIcon_NewOperation))
                    }.addOnFailureListener {
                        Picasso.get().load(R.drawable.towallet)
                            .into(view.findViewById<ImageView>(R.id.toAcountIcon_NewOperation))
                    }
                    toAccounts = acc.accounts
                }
                accountsViewModel.clearAccounts()
            }
        }

        view.findViewById<RelativeLayout>(R.id.accountsRelativeLayout).setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAccounts")
            if (existingFragment == null) {
                val newFragment = BottomSheetAccounts.newInstance2(null, "ac1")
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAccounts"
                )
            }
        }

        view.findViewById<RelativeLayout>(R.id.toAccountsRelativeLayout).setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAccounts")
            if (existingFragment == null) {
                val newFragment = BottomSheetAccounts.newInstance2(accountName, "ac2")
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAccounts"
                )
            }

        }


        if (switchState) {
            tabLayout.setTabTextColors(Color.WHITE, Color.rgb(211, 138, 6))
        } else {
            tabLayout.setTabTextColors(Color.BLACK, Color.rgb(150, 75, 0))
        }
        tabLayout.addTab(tabLayout.newTab().setText(R.string.expense))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.income))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.translation))

        amount = view.findViewById(R.id.amount_field)


        amount.setTextColor(Color.rgb(255, 0, 0))
        amount.setHintTextColor(Color.rgb(255, 0, 0))

        amount.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(amount.windowToken, 0)
                true
            } else {
                false
            }
        }

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.let {
                    when (it.position) {
                        0 -> {
                            amount.setTextColor(Color.rgb(255, 0, 0))
                            amount.setHintTextColor(Color.rgb(255, 0, 0))

                            view.findViewById<RelativeLayout>(R.id.toAccountsRelativeLayout).visibility =
                                View.GONE
                            category = null
                            view.findViewById<ImageView>(R.id.categoryIcon)
                                .setImageResource(R.drawable.category)
                            view.findViewById<TextView>(R.id.subcategory_txt).text = ""
                        }

                        1 -> {
                            amount.setTextColor(Color.rgb(0, 128, 0))
                            amount.setHintTextColor(Color.rgb(0, 128, 0))

                            view.findViewById<RelativeLayout>(R.id.toAccountsRelativeLayout).visibility =
                                View.GONE
                            category = null
                            view.findViewById<ImageView>(R.id.categoryIcon)
                                .setImageResource(R.drawable.category)
                            view.findViewById<TextView>(R.id.subcategory_txt).text = ""
                        }

                        2 -> {
                            amount.setTextColor(Color.rgb(0, 0, 255))
                            amount.setHintTextColor(Color.rgb(0, 0, 255))

                            view.findViewById<RelativeLayout>(R.id.toAccountsRelativeLayout).visibility =
                                View.VISIBLE

                            val ref = FirebaseFirestore.getInstance()
                                .document("/users/${Firebase.auth.uid.toString()}/category/other/subcategories/transfer")

                            ref.get()
                                .addOnSuccessListener { snapshot ->
                                    if (snapshot.exists()) {
                                        val ctg = snapshot.toObject(Category::class.java)

                                        val gsReference =
                                            Firebase.storage.getReferenceFromUrl(ctg!!.image!!)
                                        gsReference.downloadUrl.addOnSuccessListener { uri ->
                                            Picasso.get().load(uri.toString())
                                                .into(view.findViewById<ImageView>(R.id.categoryIcon))
                                        }.addOnFailureListener {
                                            Picasso.get().load(R.drawable.category)
                                                .into(view.findViewById<ImageView>(R.id.categoryIcon))
                                        }
                                        if (languageInit(requireActivity())) {
                                            view.findViewById<TextView>(R.id.subcategory_txt).text =
                                                ctg.nameRus

                                        } else
                                            view.findViewById<TextView>(R.id.subcategory_txt).text =
                                                ctg.nameEng

                                        category = ctg

                                    }
                                }
                                .addOnFailureListener { exception ->
                                }

                        }

                        else -> {}
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })


        currentDateTime = Calendar.getInstance()


        mapViewModel = ViewModelProvider(requireActivity())[MapViewModel::class.java]
        mapViewModel.getSelectedMap().observe(this) { geo ->
            if (geo != null) {
                map = geo
                val geocoder = Geocoder(requireContext(), Locale.getDefault())

                val addresses = geocoder.getFromLocation(map.latitude, map.longitude, 1)
                val address = addresses?.get(0)?.getAddressLine(0)

                view.findViewById<TextView>(R.id.locationTextView).text = address

                if (switchState) {
                    view.findViewById<ImageView>(R.id.Imgright33)
                        .setImageResource(R.drawable.delete_dark)
                } else {
                    view.findViewById<ImageView>(R.id.Imgright33)
                        .setImageResource(R.drawable.delete)
                }

                mapViewModel.clearMap()
            }
        }

        if (paymentPlanning != null) {

            amount.setText(paymentPlanning!!.value.toString())

            val date = paymentPlanning!!.timestamp?.toDate()
            val pattern = "dd.MM.yyyy HH:mm"
            val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())


            val dateString = date?.let { simpleDateFormat.format(it) }
            dateTimeTextView.text = dateString

            val storage = Firebase.storage

            val gsReference = storage.getReferenceFromUrl(paymentPlanning!!.icon!!)

            gsReference.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri.toString())
                    .into(view.findViewById<ImageView>(R.id.categoryIcon))
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.category)
                    .into(view.findViewById<ImageView>(R.id.categoryIcon))
            }
            if (languageInit(requireActivity())) {
                view.findViewById<TextView>(R.id.subcategory_txt).text =
                    paymentPlanning!!.categoryRu

            } else
                view.findViewById<TextView>(R.id.subcategory_txt).text =
                    paymentPlanning!!.categoryEn

            category = Category(
                paymentPlanning!!.categoryEn,
                null,
                paymentPlanning!!.categoryRu,
                paymentPlanning!!.icon,
                null
            )
        }

        categoryViewModel = ViewModelProvider(requireActivity())[CategoryViewModel::class.java]
        categoryViewModel.getSelectedCategory().observe(this) { ctg ->
            if (ctg != null) {
                val storage = Firebase.storage

                val gsReference = storage.getReferenceFromUrl(ctg.image!!)

                gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString())
                        .into(view.findViewById<ImageView>(R.id.categoryIcon))
                }.addOnFailureListener {
                    Picasso.get().load(R.drawable.category)
                        .into(view.findViewById<ImageView>(R.id.categoryIcon))
                }
                if (languageInit(requireActivity())) {
                    view.findViewById<TextView>(R.id.subcategory_txt).text = ctg.nameRus

                } else
                    view.findViewById<TextView>(R.id.subcategory_txt).text = ctg.nameEng

                category = ctg
                categoryViewModel.clearCategory()
            }
        }
        qrViewModel = ViewModelProvider(requireActivity())[QrViewModel::class.java]

        qrViewModel.getSelectedQr().observe(viewLifecycleOwner) { qr ->

            if (qr != null) {
                qrText(qr)
            }
        }

        imageViewModel = ViewModelProvider(requireActivity())[ImageViewModel::class.java]

        imageViewModel.cameraImageUri.observe(viewLifecycleOwner) { uri ->

            if (uri != null) {
                if (uri.cameraGallery == "camera") {
                    imageLoad(uri)
                } else if (uri.cameraGallery == "delete") {
                    photo = null
                    view.findViewById<ImageView>(R.id.photoBillImageView)
                        .setImageResource(R.drawable.photo)
                }
            }
        }

        imageViewModel.galleryImageUri.observe(viewLifecycleOwner) { uri ->

            if (uri != null) {
                if (uri.cameraGallery == "gallery") {
                    imageLoad(uri)
                } else if (uri.cameraGallery == "delete") {
                    photo = null
                    view.findViewById<ImageView>(R.id.photoBillImageView)
                        .setImageResource(R.drawable.photo)
                }
            }

        }


        val linearLayout = tabLayout.getChildAt(0) as LinearLayout
        linearLayout.showDividers = LinearLayout.SHOW_DIVIDER_MIDDLE
        val drawable = GradientDrawable()
        drawable.setColor(Color.BLACK)
        drawable.setSize(2, 2)
        linearLayout.dividerPadding = 20
        linearLayout.dividerDrawable = drawable


        amount.addTextChangedListener(object : TextWatcher {
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

        view.findViewById<Button>(R.id.good_btn).setOnClickListener {

            if (view.findViewById<EditText>(R.id.amount_field).text.toString().isNotEmpty()
                && view.findViewById<TextView>(R.id.subcategory_txt).text.toString().isNotEmpty()
            ) {
                val value =
                    view.findViewById<EditText>(R.id.amount_field).text.toString()
                        .toDouble()

                val note = view.findViewById<TextView>(R.id.noteTextView).text.toString()

                val selectedTabPosition =
                    view.findViewById<TabLayout>(R.id.tab_layout).selectedTabPosition
                val selectedTab =
                    view.findViewById<TabLayout>(R.id.tab_layout)
                        .getTabAt(selectedTabPosition)


                if (view.findViewById<RelativeLayout>(R.id.toAccountsRelativeLayout).isVisible) {
                    if (view.findViewById<TextView>(R.id.toAcountName_NewOperation).text.toString()
                            .isNotEmpty()
                    ) {
                        dt = Timestamp(currentDateTime.time)
                        uploadData(
                            selectedTab?.text.toString(),
                            value,
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
                } else {
                    dt = Timestamp(currentDateTime.time)
                    uploadData(
                        selectedTab?.text.toString(),
                        value,
                        note
                    )
                }
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

        view.findViewById<TextView>(R.id.newOperationExit).setOnClickListener {
            dismiss()
        }








        view.findViewById<TextView>(R.id.newOperationDone).setOnClickListener {
            if (view.findViewById<EditText>(R.id.amount_field).text.toString().isNotEmpty()
                && view.findViewById<TextView>(R.id.subcategory_txt).text.toString().isNotEmpty()
            ) {
                if (toAccounts != null && view.findViewById<TextView>(R.id.toAcountName_NewOperation).text.toString()
                        .isNotEmpty()
                ) {


                    val value =
                        view.findViewById<EditText>(R.id.amount_field).text.toString().toDouble()

                    val note = view.findViewById<TextView>(R.id.noteTextView).text.toString()

                    val selectedTabPosition =
                        view.findViewById<TabLayout>(R.id.tab_layout).selectedTabPosition
                    val selectedTab =
                        view.findViewById<TabLayout>(R.id.tab_layout).getTabAt(selectedTabPosition)

                    dt = Timestamp(currentDateTime.time)
                    uploadData(
                        selectedTab?.text.toString(),
                        value,
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



        view.findViewById<RelativeLayout>(R.id.dateTimeRelativeLayout).setOnClickListener {


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



        view.findViewById<ImageView>(R.id.Imgright33).setOnClickListener {

            if (map != GeoPoint(0.0, 0.0)) {
                map = GeoPoint(0.0, 0.0)

                view.findViewById<TextView>(R.id.locationTextView).text = null


                if (switchState) {
                    view.findViewById<ImageView>(R.id.Imgright33)
                        .setImageResource(R.drawable.right_dark)
                } else {
                    view.findViewById<ImageView>(R.id.Imgright33).setImageResource(R.drawable.right)
                }
            }
        }

        view.findViewById<RelativeLayout>(R.id.relativeLayoutLocation).setOnClickListener {
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

        view.findViewById<ImageView>(R.id.photoBillImageView).setOnClickListener {

            if (photo != null) {

                val dialog = Dialog(requireContext())
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.window?.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT
                )

                val imageView = ImageView(requireContext())
                imageView.adjustViewBounds = true
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER

                Picasso.get().load(fullScreenImage).into(imageView)

                dialog.setContentView(imageView)
                dialog.show()

            }

        }


        view.findViewById<RelativeLayout>(R.id.relativeLayoutPhoto).setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetPhotoFragment")

            if (existingFragment == null) {
                val newFragment = BottomSheetPhotoFragment.newInstance("photo", photo == null)
                newFragment.setTargetFragment(this@BottomSheetNewOperationFragment, 0)

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetPhotoFragment"
                )
            }
        }

        view.findViewById<RelativeLayout>(R.id.relativeLayoutQR).setOnClickListener {


            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    requestCodeCameraPermission
                )
            } else {
                val existingFragment =
                    requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetQR")
                if (existingFragment == null) {
                    val newFragment = BottomSheetQR()
                    newFragment.show(
                        requireActivity().supportFragmentManager,
                        "BottomSheetQR"
                    )
                }
            }

        }


        view.findViewById<RelativeLayout>(R.id.relativeLayoutOCR).setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetPhotoFragment")

            if (existingFragment == null) {
                val newFragment = BottomSheetPhotoFragment.newInstance("ocr", photo == null)
                newFragment.setTargetFragment(this@BottomSheetNewOperationFragment, 0)

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetPhotoFragment"
                )
            }

        }

        view.findViewById<RelativeLayout>(R.id.categoryRelativeLayout).setOnClickListener {

            if (!view.findViewById<RelativeLayout>(R.id.toAccountsRelativeLayout).isVisible) {

                val existingFragment =
                    requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetCategoryFragment")
                if (existingFragment == null) {
                    val newFragment = BottomSheetCategoryFragment()
                    newFragment.show(
                        requireActivity().supportFragmentManager,
                        "BottomSheetCategoryFragment"
                    )
                }
                category = Category()


                view.findViewById<ImageView>(R.id.categoryIcon)
                    .setImageResource(R.drawable.category)
                view.findViewById<TextView>(R.id.subcategory_txt).text = ""
            }
        }

    }

    private fun qrText(qr: String) {

        if (qr != "") {

            val pattern = Pattern.compile("s=([0-9]+\\.?[0-9]*)")
            val matcher = pattern.matcher(qr)
            var value = ""
            while (matcher.find()) {
                value = matcher.group(1)
            }
            if (!value.isNullOrEmpty()) {
                amount.setText(value)
            }
        }
        qrViewModel.clearQr()
    }

    fun isInternetConnected(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)

        return capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
    }

    @SuppressLint("SetTextI18n", "SimpleDateFormat")
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

                view?.findViewById<ImageView>(R.id.photoBillImageView)!!.setImageBitmap(bitmap)

            } else {
                val swipeRefreshLayout =
                    requireView().findViewById<ProgressBar>(R.id.swipeNewOperation_refreshLayout)
                var str = ""
                var result: List<String>? = null


                CoroutineScope(Dispatchers.Main).launch {
                    swipeRefreshLayout.visibility = View.VISIBLE
                    str = withContext(Dispatchers.Default) {
                        delay(100)
                        ocr(requireActivity(), uri.bitmap!!)
                    }

                    result = parsing(str)
                    val date: String? = result?.get(0)
                    val time: String? = result?.get(1)
                    val dateAndTime: String? = result?.get(2)
                    val money: String? = result?.get(3)

                    if (date != "" && time != "") {
                        dateTimeTextView.text = "${date} ${time}"

                        val formatter = DateTimeFormatterBuilder()
                            .appendPattern("[dd.MM.yy][dd MM yy] HH:mm")
                            .toFormatter()

                        val dateTime = LocalDateTime.parse("${date} ${time}", formatter)

                        val outputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")
                        val formattedDateTime = dateTime.format(outputFormatter)

                    } else if (dateAndTime != "") {

                        val format = SimpleDateFormat("dd/MM/yyyy HH:mm")
                        val date = format.parse(dateAndTime)
                        dt = Timestamp(date.time / 1000, 0)

                        dateTimeTextView.text = dateAndTime
                    }

                    if (money != "") {
                        amount.setText(money)
                    }
                    Timestamp(currentDateTime.time)

                    swipeRefreshLayout.visibility = View.GONE

                    if (isAdded) {
                        val allEmpty = result!!.all { it == "" }
                        if (allEmpty) {
                            Toast.makeText(context, getString(R.string.imageRecognized), Toast.LENGTH_SHORT).show()
                        }
                    }
                }


            }

            imageViewModel.clearGalleryImage()
            imageViewModel.clearCameraImage()

        }

    }

    @SuppressLint("SuspiciousIndentation", "StringFormatInvalid")
    private fun uploadData(
        operation: String,
        value: Double,
        note: String
    ) {

        val uid = UUID.randomUUID().toString()
        val path =
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

        }

        var operationRu = ""
        var operationEng = ""
        when (operation) {
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
        val money = if (operation == getText(R.string.income)) value else -value

        val id = UUID.randomUUID().toString()

        val sharedPref = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val accounts = sharedPref.getString("accounts", "")

        val ac = accountName?.name ?: accounts

        val uri = category!!.url!!

        val hashMap = hashMapOf<String, Any>(
            "id" to id,
            "typeRu" to operationRu,
            "typeEn" to operationEng,
            "value" to value,
            "timestamp" to dt,
            "note" to note,
            "category" to uri,
            "map" to map,
            "photo" to path,
            "account" to ac!!
        )


        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()

        db.firestoreSettings = settings

        val userID = db.collection("users").document(Firebase.auth.uid.toString())

        if (operation == getString(R.string.expense))
            updateBudgets(id, dt, ac, uri, value)

        if (toAccounts == null) {
            userID
                .collection("user").document("information")
                .update("total_balance", FieldValue.increment(money))
                .addOnSuccessListener {}
                .addOnFailureListener {}

            userID
                .collection("accounts").document(ac)
                .update("balance", FieldValue.increment(money))
                .addOnSuccessListener {}
                .addOnFailureListener {}
        } else {
            userID
                .collection("accounts").document(ac)
                .update("balance", FieldValue.increment(money))
                .addOnSuccessListener {}
                .addOnFailureListener { exception -> }

            userID
                .collection("accounts").document(toAccounts!!.name!!)
                .update("balance", FieldValue.increment(-money))
                .addOnSuccessListener {}
                .addOnFailureListener { exception -> }
        }
        var error = false

        val cont = requireActivity()

        userID
            .collection("accounts")
            .document(ac)
            .collection("operation").document(id)
            .set(hashMap)
            .addOnSuccessListener { documentReference ->
                if (paymentPlanning != null) {

                    val status = hashMapOf<String, Any>(
                        "status" to "paidFor",
                    )

                    FirebaseFirestore.getInstance().collection("users")
                        .document(Firebase.auth.uid.toString())
                        .collection("paymentPlanning").document(paymentPlanning!!.id!!)
                        .update(status)
                        .addOnSuccessListener {

                            val intent = Intent(cont, NotificationReceiver::class.java)

                            intent.putExtra("uid", paymentPlanning!!.idNotification!!.toInt())

                            val pendingIntent = PendingIntent.getBroadcast(
                                cont,
                                paymentPlanning!!.idNotification!!.toInt(),
                                intent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                            )

                            val alarmManager =
                                cont.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                            alarmManager.cancel(pendingIntent)

                            val notificationManager = NotificationManagerCompat.from(cont)
                            notificationManager.cancel(paymentPlanning!!.idNotification!!.toInt())

                            dismiss()
                        }
                        .addOnFailureListener { e ->
                        }
                }

                dismiss()
            }
            .addOnFailureListener { error = true }

        if (!error) {
            dismiss()
        }
    }

    private fun updateBudgets(id: String, date:Timestamp, account: String, category: String, value: Double) {
        val budgetsCollectionRef = db.collection("users")
            .document(Firebase.auth.uid.toString())
            .collection("budgets")

        budgetsCollectionRef
            .whereGreaterThan("timeEnd", Timestamp.now())
            .get()
            .addOnSuccessListener { querySnapshot ->
                for (document in querySnapshot.documents) {
                    val data = document.toObject(Budgets::class.java)

                    val accountsList = data!!.accounts

                    if (accountsList != null && (accountsList.isEmpty() || account in accountsList)) {

                        val categoriesList = data.categories

                        val parts = category.split("/")
                        val categoryName = parts.getOrNull(2)

                        if (categoriesList != null && (categoriesList.isEmpty() || categoryName in categoriesList)) {


                            val operationUrl = FirebaseFirestore.getInstance().collection("users").document(Firebase.auth.uid.toString()).collection("accounts")
                                .document(account)
                                .collection("operation").document(id)

                            val hashMap = hashMapOf<String, Any>(
                                "id" to id,
                                "value" to value,
                                "timestamp" to date,
                                "operationUrl" to operationUrl.path
                            )

                            budgetsCollectionRef
                                .document(data.id!!)
                                .collection("operation").document(id)
                                .set(hashMap)
                                .addOnSuccessListener { documentReference ->
                                }

                            budgetsCollectionRef.document(data.id!!)
                                .update("valueNow", FieldValue.increment(value))
                                .addOnSuccessListener {

                                    if(data.notification80Is.toBoolean() && data.notification80=="") {
                                        val percent =
                                            (data.valueNow!! * 100 / data.maxValue!!).toInt()

                                        if (percent > 80) {

                                            val random = Random()
                                            val uid = random.nextInt(Int.MAX_VALUE)

                                            val notificationBuilder = NotificationCompat.Builder(
                                                requireContext(),
                                                "default_channel"
                                            )
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setContentText("${getString(R.string.budget20)} ${data.name}")
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)

                                            budgetsCollectionRef.document(data.id!!)
                                                .update("notification80", "yes")
                                                .addOnSuccessListener {}

                                            val notificationManager =
                                                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                            notificationManager.notify(
                                                uid,
                                                notificationBuilder.build()
                                            )
                                        }
                                    }
                                    if(data.notificationOverrunsIs.toBoolean() && data.notificationOverruns=="") {
                                        if (data.valueNow!! > data.maxValue!!) {

                                            val random = Random()
                                            val uid = random.nextInt(Int.MAX_VALUE)

                                            val notificationBuilder = NotificationCompat.Builder(
                                                requireContext(),
                                                "default_channel"
                                            )
                                                .setSmallIcon(R.mipmap.ic_launcher)
                                                .setContentText("${getString(R.string.budgetOverruns)} ${data.name}")
                                                .setPriority(NotificationCompat.PRIORITY_HIGH)

                                            budgetsCollectionRef.document(data.id!!)
                                                .update("notificationOverruns", "yes")
                                                .addOnSuccessListener {}

                                            val notificationManager =
                                                requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                            notificationManager.notify(
                                                uid,
                                                notificationBuilder.build()
                                            )
                                        }
                                    }
                                }
                                .addOnFailureListener {}
                        }
                    }
                }
            }
            .addOnFailureListener { e ->

            }

    }

    companion object {
        fun newInstance(
            paymentPlanning: PaymentPlanning
        ): BottomSheetNewOperationFragment {
            val args = Bundle()
            args.putParcelable("paymentPlanning", paymentPlanning)

            val fragment = BottomSheetNewOperationFragment()
            fragment.arguments = args

            return fragment
        }
    }

}