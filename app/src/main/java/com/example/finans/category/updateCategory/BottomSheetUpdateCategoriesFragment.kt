package com.example.finans.category.updateCategory

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.category.Category
import com.example.finans.category.CategoryAdapter
import com.example.finans.category.OnItemClickListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.ktx.Firebase


class BottomSheetUpdateCategoriesFragment : BottomSheetDialogFragment(), OnItemClickListener {

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


         return if(switchState) {
             inflater.inflate(
                 R.layout.fragment_bottom_sheet_dark_update_categories,
                 container,
                 false
             )
         } else {
             return inflater.inflate(
                 R.layout.fragment_bottom_sheet_update_categories,
                 container,
                 false
             )
         }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<TextView>(R.id.categoriesEditExit).setOnClickListener {
            dismiss()
        }

        categoryRecyclerView = view.findViewById(R.id.updateCategoriesRecyclerView)
        categoryRecyclerView.layoutManager = LinearLayoutManager(context)

        categoryArrayList = arrayListOf()

        val prefs = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val prefs2 =  PreferenceManager.getDefaultSharedPreferences(requireContext())

        categoryAdapter = CategoryAdapter(categoryArrayList)
        categoryAdapter.setOnItemClickListener(this)

        if (prefs != null) {
            categoryAdapter.setSharedPreferencesLocale(prefs, prefs2, null)
        }

        categoryRecyclerView.adapter = categoryAdapter


        categoryAdapter.notifyDataSetChanged()

        getCategoryData()

    }


    override fun onItemClick(category: Category) {
        val existingFragment =
            requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetUpdateCategoryFragment")

        if (existingFragment == null) {
            val newFragment = BottomSheetUpdateCategoryFragment.newInstance(category)
            newFragment.setTargetFragment(this@BottomSheetUpdateCategoriesFragment, 0)

            newFragment.show(
                requireActivity().supportFragmentManager,
                "BottomSheetUpdateCategoryFragment"
            )

        }
    }

    override fun onItemsClick(category: java.util.ArrayList<Category>) {
        TODO("Not yet implemented")
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

                for (dc in value?.documentChanges!!) {
                    val category = dc.document.toObject(Category::class.java)
                    val index = categoryArrayList.indexOfFirst { it.name == category.name }
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            if (index == -1) {
                                categoryArrayList.add(category)
                            }
                        }

                        DocumentChange.Type.MODIFIED -> {
                            if (index != -1) {
                                categoryArrayList[index] = category
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            if (index != -1) {
                                categoryArrayList.removeAt(index)
                            }
                        }
                    }
                }

                categoryAdapter.notifyDataSetChanged()
            }
        })

    }

}