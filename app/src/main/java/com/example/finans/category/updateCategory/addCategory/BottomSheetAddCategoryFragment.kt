package com.example.finans.category.updateCategory.addCategory

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
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import com.example.finans.R
import com.example.finans.category.Category
import com.example.finans.category.updateCategory.BottomSheetUpdateCategoryFragment
import com.example.finans.category.updateCategory.BottomSheetUpdateIcon
import com.example.finans.category.updateCategory.IconViewModel
import com.example.finans.other.deletionWarning
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class BottomSheetAddCategoryFragment : BottomSheetDialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences
    var switchState: Boolean = false
    private lateinit var iconViewModel: IconViewModel
    private lateinit var gsReference: StorageReference
    private lateinit var category: Category
    private lateinit var type: String
    var update: Boolean = false
    private lateinit var icn: String
    private lateinit var nameCategory: EditText
    companion object {
        fun newInstance(
            category: Category,
            type: String,
            categoryName: String
        ): BottomSheetAddCategoryFragment {
            val args = Bundle()
            args.putParcelable("category", category)
            args.putString("type", type)
            args.putString("categoryName", categoryName)
            val fragment = BottomSheetAddCategoryFragment()
            fragment.arguments = args

            return fragment
        }


    }

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
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_add_category, container, false)
        } else{
            return inflater.inflate(R.layout.fragment_bottom_sheet_add_category, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
            ?.getString("locale", "")
        category = arguments?.getParcelable("category")!!
        type = arguments?.getString("type")!!

        var categ = ""

        if (sharedPref == "ru") {
            view.findViewById<TextView>(R.id.addCategoryName).text = category.nameRus
            categ = category.nameRus!!
        } else {
            view.findViewById<TextView>(R.id.addCategoryName).text = category.nameEng
            categ = category.nameEng!!
        }


        if(type == "subcategories"){
            view.findViewById<LinearLayout>(R.id.categoryAdd_LinearLayout).visibility = View.GONE
            view.findViewById<EditText>(R.id.updateCategoryNameEdit).setText(categ)

            view.findViewById<Button>(R.id.addCategory).visibility = View.GONE
            view.findViewById<Button>(R.id.updateCategory).visibility  = View.GONE


            if(category.new.toBoolean()) {
                view.findViewById<Button>(R.id.deleteCategory).visibility  = View.VISIBLE
            }
            else{
                view.findViewById<Button>(R.id.updateCategory).visibility  = View.VISIBLE
               // update = true
            }
        }

        view.findViewById<EditText>(R.id.updateCategoryNameEdit).addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {

                if (view.findViewById<EditText>(R.id.updateCategoryNameEdit).text.toString() != categ && type == "subcategories") {
                   view.findViewById<Button>(R.id.updateCategory).visibility  = View.VISIBLE

                    view.findViewById<Button>(R.id.addCategory).visibility = View.GONE
                    view.findViewById<Button>(R.id.deleteCategory).visibility  = View.GONE

                //    update = true
                }
            }
        })

        view.findViewById<EditText>(R.id.updateCategoryNameEdit).setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(view.findViewById<EditText>(R.id.updateCategoryNameEdit).windowToken, 0)
                true
            } else {
                false
            }
        }

        icn = category.image!!
        val storage = Firebase.storage

        nameCategory = view.findViewById(R.id.updateCategoryNameEdit)

        iconViewModel = ViewModelProvider(requireActivity())[IconViewModel::class.java]
        iconViewModel.getSelectedIcon().observe(this) { icon ->
            if (icon != null) {
                println(icon.type)

                if (icon.type == "addCategory") {

                   // update = true
                    icn = icon.icon
                    gsReference = storage.getReferenceFromUrl(icon.icon)
                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri)
                            .into(view.findViewById<ImageView>(R.id.addCategoryEditIcon))
                    }.addOnFailureListener {
                        Picasso.get().load(R.drawable.categories)
                            .into(view.findViewById<ImageView>(R.id.addCategoryEditIcon))
                    }

                    iconViewModel.clearIcon()
                }
                if (type == "subcategories") {
                    view.findViewById<Button>(R.id.updateCategory).visibility  = View.VISIBLE
                    println(type)
                    view.findViewById<Button>(R.id.addCategory).visibility = View.GONE
                    view.findViewById<Button>(R.id.deleteCategory).visibility  = View.GONE
                }
            }
        }

        gsReference = storage.getReferenceFromUrl(category.image!!)

        gsReference.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(view.findViewById<ImageView>(R.id.addCategoryEditIcon))
        }.addOnFailureListener {
            Picasso.get().load(R.drawable.categories)
                .into(view.findViewById<ImageView>(R.id.addCategoryEditIcon))
        }



        view.findViewById<ImageView>(R.id.categoryUpdateIcon).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetUpdateIcon") as? BottomSheetUpdateIcon

            if (bottomSheetFragment == null) {

                val newFragment = BottomSheetUpdateIcon.newInstance("addCategory")
                newFragment.setTargetFragment(this@BottomSheetAddCategoryFragment, 0)

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetUpdateIcon"
                )

            }
        }

        view.findViewById<TextView>(R.id.addCategoryExit).setOnClickListener {
            dismiss()
            iconViewModel.clearIcon()
        }

        val categoryName = arguments?.getString("categoryName")!!



        view.findViewById<Button>(R.id.addCategory).setOnClickListener {
            if (!nameCategory.text.isNullOrEmpty()) {

                val firestore = FirebaseFirestore.getInstance()

                val documentRef =
                    firestore.document("users/${Firebase.auth.uid.toString()}/category/${category.name}")
                documentRef.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val subcollectionRef = documentRef.collection("subcategories")
                        val newDocumentData = hashMapOf(
                            "image" to icn,
                            "url" to "/category/${category.name}/subcategories/${nameCategory.text}",
                            "name" to nameCategory.text.toString(),
                            "nameEng" to nameCategory.text.toString(),
                            "nameRus" to nameCategory.text.toString(),
                            "new" to "true",
                        )
                        subcollectionRef.document(nameCategory.text.toString())
                            .set(newDocumentData)
                            .addOnSuccessListener { documentReference ->
                                val existingFragment =
                                    requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetUpdateCategoryFragment")

                                if (existingFragment == null) {
                                    val newFragment = BottomSheetUpdateCategoryFragment.newInstance(category)
                                    newFragment.setTargetFragment(this@BottomSheetAddCategoryFragment, 0)

                                    newFragment.show(
                                        requireActivity().supportFragmentManager,
                                        "BottomSheetUpdateCategoryFragment"
                                    )
                                    dismiss()

                                }

                            }
                            .addOnFailureListener { exception ->
                                println("Ошибка при добавлении нового документа: $exception")
                            }
                    } else {
                        println("Документ не найден.")
                    }
                }.addOnFailureListener { exception ->
                    println("Ошибка при получении документа: $exception")
                }

            } else {
                Toast.makeText(requireActivity(), getString(R.string.categoryName), Toast.LENGTH_LONG).show()
            }
        }

        view.findViewById<Button>(R.id.updateCategory).setOnClickListener {
            if (!nameCategory.text.isNullOrEmpty()) {
            val newDocumentData = hashMapOf(
                "image" to icn,
                "nameEng" to view.findViewById<EditText>(R.id.updateCategoryNameEdit).text.toString(),
                "nameRus" to view.findViewById<EditText>(R.id.updateCategoryNameEdit).text.toString()
            )

            category.image = icn
            category.nameEng =
                view.findViewById<EditText>(R.id.updateCategoryNameEdit).text.toString()
            category.nameRus =
                view.findViewById<EditText>(R.id.updateCategoryNameEdit).text.toString()

            val documentRef = FirebaseFirestore.getInstance()
                .document("users/${Firebase.auth.uid.toString()}/category/${categoryName}/subcategories/${category.name!!}")
            documentRef.update(newDocumentData as Map<String, Any>)
                .addOnSuccessListener {

                    dismiss()
                }
                .addOnFailureListener { exception -> }
            } else {
                Toast.makeText(requireActivity(), getString(R.string.categoryName), Toast.LENGTH_LONG).show()
            }
        }

        view.findViewById<Button>(R.id.deleteCategory).setOnClickListener {

            deletionWarning(requireContext()) { result ->

                if(result){

                    val firestore = FirebaseFirestore.getInstance()

                    val documentPath =
                        "users/${Firebase.auth.uid.toString()}/category/${categoryName}/subcategories/${category.name!!}"
                    val documentRef = firestore.document(documentPath)

                    documentRef.delete()
                        .addOnSuccessListener {
                            println("Документ успешно удален.")

                            dismiss()

                        }
                        .addOnFailureListener { exception ->
                            println("Ошибка при удалении документа: $exception")
                        }

                }

            }

        }


    }
}