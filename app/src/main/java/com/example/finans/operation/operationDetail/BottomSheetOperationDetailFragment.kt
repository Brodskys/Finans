package com.example.finans.operation.operationDetail

import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.example.finans.R
import com.example.finans.category.Category
import com.example.finans.category.subcategory.SubcategoryAdapter
import com.example.finans.operation.Operation
import com.example.finans.сurrency.BottomSheetCurrencyFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso


class BottomSheetOperationDetailFragment : BottomSheetDialogFragment(){
    private lateinit var sharedPreferences: SharedPreferences
    var switchState: Boolean = false
    private lateinit var dbref: FirebaseFirestore


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
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_operation_detail, container, false)
        } else{
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tabLayout: TabLayout = view.findViewById(R.id.operationDetail_tab_layout)

        if(switchState) {
            tabLayout.setTabTextColors(Color.WHITE, Color.rgb(211, 138, 6))
        }
        else{
            tabLayout.setTabTextColors(Color.BLACK, Color.rgb(150, 75, 0))
        }

        view.findViewById<TextView>(R.id.operationDetailExit).setOnClickListener {

            dismiss()

        }

        view.findViewById<TextView>(R.id.operationDetailDone).setOnClickListener {

            dismiss()

        }

        tabLayout.addTab(tabLayout.newTab().setText(R.string.expense))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.income))
        tabLayout.addTab(tabLayout.newTab().setText(R.string.translation))

        val operation = arguments?.getParcelable<Operation>("operation")

        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            if (tab?.text?.toString() == operation?.type) {
                tabLayout.selectTab(tab)
                break
            }
        }

        view.findViewById<EditText>(R.id.operationDetail_amount_field).text =Editable.Factory.getInstance().newEditable(operation?.value.toString())

        Picasso.get().load(operation?.image)
            .into(view.findViewById<ImageView>(R.id.operationDetail_categoryIcon))

        val imageRef = operation?.image?.let { FirebaseStorage.getInstance().getReferenceFromUrl(it) }

        imageRef?.downloadUrl?.addOnSuccessListener { uri ->
            Picasso.get().load(uri)
                .placeholder(R.drawable.category)
                .error(R.drawable.category)
                .into(view.findViewById<ImageView>(R.id.operationDetail_categoryIcon))
        }?.addOnFailureListener {
            view.findViewById<ImageView>(R.id.operationDetail_categoryIcon).setImageResource(R.drawable.category)
        }

        view.findViewById<TextView>(R.id.operationDetail_subcategory_txt).text = operation?.category

        view.findViewById<TextView>(R.id.operationDetail_dateTimeTextView).text = "${operation?.date} ${operation?.time}"

        view.findViewById<TextView>(R.id.operationDetail_note).text = operation?.note
        view.findViewById<TextView>(R.id.operationDetail_location).text = operation?.note



    }


    private fun getCategoryData() {
        dbref = FirebaseFirestore.getInstance()

        val operation = arguments?.getParcelable<Operation>("operation")


    }










}