package com.example.finans.category.subcategory

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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.category.BottomSheetCategoryFragment
import com.example.finans.category.Category
import com.example.finans.category.CategoryViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore


class BottomSheetSubcategoryFragment : BottomSheetDialogFragment(), OnItemClickListener {
    private lateinit var categoryRecyclerView: RecyclerView
    private lateinit var categoryArrayList: ArrayList<Category>
    private lateinit var dbref: FirebaseFirestore
    private lateinit var subcategoryAdapter: SubcategoryAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var categoryViewModel: CategoryViewModel
    var switchState: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        categoryViewModel = ViewModelProvider(requireActivity()).get(CategoryViewModel::class.java)
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
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_subcategory, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_subcategory, container, false)
        }

    }


    companion object {
        fun newInstance(
            category: Category): BottomSheetSubcategoryFragment {
            val args = Bundle()
            args.putParcelable("category", category)

            val fragment = BottomSheetSubcategoryFragment()
            fragment.arguments = args

            return fragment
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val subcategorySearch = view.findViewById<SearchView>(R.id.subcategorySearch)

        subcategorySearch.queryHint = getText(R.string.search)

        if(switchState) {
            val searchEditText = subcategorySearch.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.background3_dark))
            searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.background3_dark))
        }
        else{
            val searchEditText = subcategorySearch.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
            searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), R.color.background3))
            searchEditText.setTextColor(ContextCompat.getColor(requireContext(), R.color.background3))
        }

        categoryRecyclerView = view.findViewById(R.id.subcategoryRecyclerView)
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



        view.findViewById<TextView>(R.id.subcategoryExit).setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetCategoryFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetCategoryFragment()
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetCategoryFragment"
                )
                dismiss()
            }
            dismiss()
        }

        subcategorySearch.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                subcategoryAdapter.getFilter().filter(newText)
                return false
            }
        })
    }


    override fun onItemClick(category: Category) {
        categoryViewModel.selectCategory(category)
        dismiss()
    }


    private fun getCategoryData() {
        dbref = FirebaseFirestore.getInstance()

        val category = arguments?.getParcelable<Category>("category")


        dbref.collection("category").document(category?.NameEng!!).collection("subcategories")
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