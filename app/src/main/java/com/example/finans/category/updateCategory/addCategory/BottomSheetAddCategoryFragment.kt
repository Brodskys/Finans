package com.example.finans.category.updateCategory.addCategory

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.finans.R
import com.example.finans.category.Category
import com.example.finans.category.updateCategory.BottomSheetUpdateCategoryFragment
import com.example.finans.category.updateCategory.BottomSheetUpdateIconCategoryFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

class BottomSheetAddCategoryFragment : BottomSheetDialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences
    var switchState: Boolean = false

    companion object {
        fun newInstance(
            category: Category
        ): BottomSheetAddCategoryFragment {
            val args = Bundle()
            args.putParcelable("category", category)

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

        val sharedPref = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)?.getString("locale", "")

        val category = arguments?.getParcelable<Category>("category")

        val storage = Firebase.storage
        val gsReference = storage.getReferenceFromUrl(category?.image!!)

        gsReference.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri).into(view.findViewById<ImageView>(R.id.addCategoryIcon))
        }.addOnFailureListener {
            Picasso.get().load(R.drawable.category)
                .into(view.findViewById<ImageView>(R.id.addCategoryIcon))
        }

        if (sharedPref == "ru"){
            view.findViewById<TextView>(R.id.addCategoryName).text = category.nameRus
        } else {
            view.findViewById<TextView>(R.id.addCategoryName).text = category.nameEng
        }

        view.findViewById<ImageView>(R.id.categoryUpdateIcon).setOnClickListener {
            val bottomSheetFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetUpdateIconCategoryFragment") as? BottomSheetUpdateIconCategoryFragment

            if (bottomSheetFragment == null)
                BottomSheetUpdateIconCategoryFragment().show(requireActivity().supportFragmentManager, "BottomSheetUpdateIconCategoryFragment")
        }

        view.findViewById<TextView>(R.id.addCategoryExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<Button>(R.id.saveAddCategory).setOnClickListener {
            dismiss()
        }

        }


}