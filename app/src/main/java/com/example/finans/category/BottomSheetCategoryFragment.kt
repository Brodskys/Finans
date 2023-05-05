package com.example.finans.category

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.category.subcategory.BottomSheetSubcategoryFragment
import com.example.finans.category.updateCategory.BottomSheetUpdateCategoriesFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase

class BottomSheetCategoryFragment : BottomSheetDialogFragment(), OnItemClickListener{
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryArrayList: ArrayList<Category>
    private lateinit var categoryAdapter: CategoryAdapter
    private lateinit var dbref: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    var switchState: Boolean = false

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
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_category, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_category, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val categorySearch = view.findViewById<SearchView>(R.id.categorySearch)

        categorySearch.queryHint = getText(R.string.search)

        if(switchState) {
            val searchEditText = categorySearch.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.background3_dark))
            searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.background3_dark))
        }
        else{
            val searchEditText = categorySearch.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.background3))
            searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.background3))
        }

        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView)
        categoryRecyclerView.layoutManager = LinearLayoutManager(context)
        categoryRecyclerView.setHasFixedSize(true)

        categoryArrayList = arrayListOf()

        val prefs = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val prefs2 =  PreferenceManager.getDefaultSharedPreferences(requireContext())

        categoryAdapter = CategoryAdapter(categoryArrayList)
        categoryAdapter.setOnItemClickListener(this)

        if (prefs != null) {
            categoryAdapter.setSharedPreferencesLocale(prefs,prefs2)
        }

        categoryRecyclerView.adapter = categoryAdapter


        categoryAdapter.notifyDataSetChanged()

        getCategoryData()



        view.findViewById<TextView>(R.id.categoryExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.categoryUpdate).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetUpdateCategoriesFragment") as? BottomSheetUpdateCategoriesFragment

            if (bottomSheetFragment == null)
                BottomSheetUpdateCategoriesFragment().show(requireActivity().supportFragmentManager, "BottomSheetUpdateCategoriesFragment")

        }

        categorySearch.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                categoryAdapter.getFilter().filter(newText)
                return false
            }

        })

    }


    override fun onItemClick(category: Category) {
        val existingFragment =
            requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetSubcategoryFragment")

        if (existingFragment == null) {
            val newFragment = BottomSheetSubcategoryFragment.newInstance(category)
            newFragment.setTargetFragment(this@BottomSheetCategoryFragment, 0)

            newFragment.show(
                requireActivity().supportFragmentManager,
                "BottomSheetSubcategoryFragment"
            )

            dismiss()
        }
    }


    private fun getCategoryData() {
        dbref = FirebaseFirestore.getInstance()


        dbref.collection("users").document(Firebase.auth.uid.toString()).collection("category").
        addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?
            ) {
                if (error != null) {
                    return
                }

                for (dc: DocumentChange in value?.documentChanges!!){
                    if (dc.type == DocumentChange.Type.ADDED){
                        categoryArrayList.add(dc.document.toObject(Category::class.java))
                    }
                }
                categoryAdapter.notifyDataSetChanged()
            }
        })

    }


}