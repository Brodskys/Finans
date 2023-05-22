package com.example.finans.category.updateCategory

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
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
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.util.Locale


class BottomSheetUpdateCategoryFragment : BottomSheetDialogFragment(), OnItemClickListener {

    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryArrayList: ArrayList<Category>
    private lateinit var dbref: FirebaseFirestore
    private lateinit var subcategoryAdapter: SubcategoryAdapter
    private lateinit var sharedPreferences: SharedPreferences
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
            it.behavior.peekHeight  = R.style.AppBottomSheetDialogTheme
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        switchState = sharedPreferences.getBoolean("modeSwitch", false)


        return if(switchState){
             inflater.inflate(R.layout.fragment_bottom_sheet_dark_update_category, container, false)
         } else{
        return inflater.inflate(R.layout.fragment_bottom_sheet_update_category, container, false)
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.categoryEditExit).setOnClickListener {
            dismiss()
        }

        val category = arguments?.getParcelable<Category>("category")

        val storage = Firebase.storage
        val gsReference = storage.getReferenceFromUrl(category?.image!!)

        gsReference.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(view.findViewById<ImageView>(R.id.categoryEditIcon))
        }.addOnFailureListener {
            Picasso.get().load(R.drawable.category).into(view.findViewById<ImageView>(R.id.categoryEditIcon))
        }


        val sharedPref = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)?.getString("locale", "")
        if (sharedPref == "ru"){
            view.findViewById<EditText>(R.id.updateCategoryNameEdit).setText(category.nameRus)
        } else {
            view.findViewById<EditText>(R.id.updateCategoryNameEdit).setText(category.nameEng)
        }

        categoryRecyclerView = view.findViewById(R.id.updateSubcategoryRecyclerView)
        categoryRecyclerView.layoutManager = LinearLayoutManager(context)
        categoryRecyclerView.setHasFixedSize(true)

        categoryArrayList = arrayListOf()

        val prefs = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val prefs2 =  PreferenceManager.getDefaultSharedPreferences(requireContext())


        subcategoryAdapter = SubcategoryAdapter(categoryArrayList)
        subcategoryAdapter.setOnItemClickListener(this)

        if (prefs != null) {
            subcategoryAdapter.setSharedPreferencesLocale(prefs,prefs2)
        }

        categoryRecyclerView.adapter = subcategoryAdapter


        subcategoryAdapter.notifyDataSetChanged()

        getCategoryData()


        view.findViewById<ImageView>(R.id.categoryUpdateIcon).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetUpdateIconCategoryFragment") as? BottomSheetUpdateIconCategoryFragment

            if (bottomSheetFragment == null)
                BottomSheetUpdateIconCategoryFragment().show(requireActivity().supportFragmentManager, "BottomSheetUpdateIconCategoryFragment")
        }


        view.findViewById<RelativeLayout>(R.id.addCategoryRelativeLayout).setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAddCategoryFragment")

            if (existingFragment == null) {
                val newFragment = BottomSheetAddCategoryFragment.newInstance(category)
                newFragment.setTargetFragment(this@BottomSheetUpdateCategoryFragment, 0)

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAddCategoryFragment"
                )

            }

        }


    }

    override fun onItemClick(category: Category) {

       dismiss()


    }


    private fun getCategoryData() {
        dbref = FirebaseFirestore.getInstance()

        val category = arguments?.getParcelable<Category>("category")


        dbref.collection("users").document(Firebase.auth.uid.toString()).collection("category").document(category?.nameEng!!.lowercase(Locale.ROOT)).collection("subcategories")
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