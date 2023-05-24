package com.example.finans.category.updateCategory

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.category.Category
import com.example.finans.category.CategoryViewModel
import com.example.finans.category.subcategory.BottomSheetSubcategoryFragment
import com.example.finans.category.subcategory.OnItemClickListener
import com.example.finans.category.subcategory.SubcategoryAdapter
import com.example.finans.category.updateCategory.addCategory.BottomSheetAddCategoryFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.util.Locale


class BottomSheetUpdateCategoryFragment : BottomSheetDialogFragment(), OnItemClickListener {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryArrayList: ArrayList<Category>
    private lateinit var dbref: FirebaseFirestore
    private lateinit var subcategoryAdapter: SubcategoryAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var iconViewModel: IconViewModel
    private lateinit var gsReference: StorageReference
    private lateinit var categoryName: EditText
    private var update: Boolean = false
    private lateinit var addCategoryLinearLayout: LinearLayout
    private lateinit var addUpdateTextView: TextView
    private lateinit var icn: String
    private lateinit var category: Category

    var switchState: Boolean = false


    companion object {
        fun newInstance(
            category: Category
        ): BottomSheetUpdateCategoryFragment {
            val args = Bundle()
            args.putParcelable("category", category)

            val fragment = BottomSheetUpdateCategoryFragment()
            fragment.arguments = args

            return fragment
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (dialog as? BottomSheetDialog)?.let {
            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        switchState = sharedPreferences.getBoolean("modeSwitch", false)


        return if (switchState) {
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_update_category, container, false)
        } else {
            return inflater.inflate(
                R.layout.fragment_bottom_sheet_update_category,
                container,
                false
            )
        }

    }

    @SuppressLint("SuspiciousIndentation")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categoryName = view.findViewById(R.id.updateCategoryNameEdit)
        addCategoryLinearLayout = view.findViewById(R.id.addCategoryLinearLayout)
        addUpdateTextView = view.findViewById(R.id.addUpdateTextView)

        category = arguments?.getParcelable<Category>("category")!!

        val sharedPref = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            ?.getString("locale", "")

        var categ = ""
        if (sharedPref == "ru") {
            categoryName.setText(category!!.nameRus)
            categ = category.nameRus!!
        } else {
            categoryName.setText(category!!.nameEng)
            categ = category.nameEng!!
        }

        categoryName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {

                if (categoryName.text.toString() != categ) {
                    updateCategory()
                }


            }
        })

        categoryName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(categoryName.windowToken, 0)
                true
            } else {
                false
            }
        }

        view.findViewById<TextView>(R.id.categoryEditExit).setOnClickListener {
            iconViewModel.clearIcon()

            dismiss()
        }


        icn = category.image!!

        val storage = Firebase.storage

        iconViewModel = ViewModelProvider(requireActivity())[IconViewModel::class.java]
        iconViewModel.getSelectedIcon().observe(this) { icon ->
            if (icon != null) {
                if (icon.type == "updateCategory") {
                    icn = icon.icon
                    gsReference = storage.getReferenceFromUrl(icon.icon)
                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri)
                            .into(view.findViewById<ImageView>(R.id.categoryEditIcon))
                    }.addOnFailureListener {
                        Picasso.get().load(R.drawable.categories)
                            .into(view.findViewById<ImageView>(R.id.categoryEditIcon))
                    }
                    println("2")
                    updateCategory()
                    iconViewModel.clearIcon()
                }
            }
        }
        gsReference = storage.getReferenceFromUrl(category.image!!)

        gsReference.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(view.findViewById<ImageView>(R.id.categoryEditIcon))
        }.addOnFailureListener {
            Picasso.get().load(R.drawable.categories)
                .into(view.findViewById<ImageView>(R.id.categoryEditIcon))
        }





        categoryRecyclerView = view.findViewById(R.id.updateSubcategoryRecyclerView)
        categoryRecyclerView.layoutManager = LinearLayoutManager(context)
        categoryRecyclerView.setHasFixedSize(true)

        categoryArrayList = arrayListOf()

        val prefs = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val prefs2 = PreferenceManager.getDefaultSharedPreferences(requireContext())


        subcategoryAdapter = SubcategoryAdapter(categoryArrayList)
        subcategoryAdapter.setOnItemClickListener(this)

        if (prefs != null) {
            subcategoryAdapter.setSharedPreferencesLocale(prefs, prefs2)
        }

        categoryRecyclerView.adapter = subcategoryAdapter


        subcategoryAdapter.notifyDataSetChanged()

        getCategoryData()


        view.findViewById<ImageView>(R.id.categoryUpdateIcon).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetUpdateIconCategoryFragment") as? BottomSheetUpdateIconCategoryFragment

            if (bottomSheetFragment == null) {
                val newFragment =
                    BottomSheetUpdateIconCategoryFragment.newInstance("updateCategory")
                newFragment.setTargetFragment(this@BottomSheetUpdateCategoryFragment, 0)

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetUpdateIconCategoryFragment"
                )
            }
        }


        addCategoryLinearLayout.setOnClickListener {

            if (!update) {


                val existingFragment =
                    requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAddCategoryFragment")

                if (existingFragment == null) {
                    val newFragment = BottomSheetAddCategoryFragment.newInstance(category, "addCategory", "")
                    newFragment.setTargetFragment(this@BottomSheetUpdateCategoryFragment, 0)

                    newFragment.show(
                        requireActivity().supportFragmentManager,
                        "BottomSheetAddCategoryFragment"
                    )
                    dismiss()
                }
            } else {
                if (!categoryName.text.isNullOrEmpty()) {
                    update = false

                    addUpdateTextView.setText(R.string.addCategory)
                    val newDocumentData = hashMapOf(
                        "image" to icn,
                        "nameEng" to categoryName.text.toString(),
                        "nameRus" to categoryName.text.toString()
                    )

                    category.image = icn
                    category.nameEng = categoryName.text.toString()
                    category.nameRus = categoryName.text.toString()

                    val documentRef = FirebaseFirestore.getInstance()
                        .document("users/${Firebase.auth.uid.toString()}/category/${category.name!!}")
                    documentRef.update(newDocumentData as Map<String, Any>)
                        .addOnSuccessListener {}
                        .addOnFailureListener { exception -> }


                } else {
                    Toast.makeText(
                        requireActivity(),
                        getString(R.string.categoryName),
                        Toast.LENGTH_LONG
                    ).show()
                }

            }
        }

    }

    fun updateCategory() {
        update = true
        addUpdateTextView.setText(R.string.update)
    }


    override fun onItemClick(category: Category) {


        val existingFragment =
            requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAddCategoryFragment")

        if (existingFragment == null) {
            val newFragment = BottomSheetAddCategoryFragment.newInstance(category, "subcategories", this.category.name!!)
            newFragment.setTargetFragment(this@BottomSheetUpdateCategoryFragment, 0)

            newFragment.show(
                requireActivity().supportFragmentManager,
                "BottomSheetAddCategoryFragment"
            )

            dismiss()
        }
    }


    private fun getCategoryData() {
        dbref = FirebaseFirestore.getInstance()

        val category = arguments?.getParcelable<Category>("category")


        dbref.collection("users").document(Firebase.auth.uid.toString()).collection("category")
            .document(category?.name!!).collection("subcategories")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                for (dc in value?.documentChanges!!) {
                    if (dc.type == DocumentChange.Type.ADDED) {
                        categoryArrayList.add(dc.document.toObject(Category::class.java))
                    }
                }

                subcategoryAdapter.notifyDataSetChanged()
            }

    }

}