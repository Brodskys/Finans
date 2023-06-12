package com.example.finans.category.updateCategory

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

class BottomSheetUpdateIcon : BottomSheetDialogFragment(), OnItemClickListener{

    private lateinit var imagesRecyclerView: RecyclerView
    private lateinit var iconViewModel: IconViewModel
    private lateinit var type: String
    var switchState: Boolean = false
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var storageRef: StorageReference

    companion object {
        fun newInstance(
            type: String
        ): BottomSheetUpdateIcon {
            val args = Bundle()
            args.putString("type", type)

            val fragment = BottomSheetUpdateIcon()
            fragment.arguments = args
            return fragment
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iconViewModel = ViewModelProvider(requireActivity())[IconViewModel::class.java]
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

        return if (switchState) {
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_update_icon_category, container, false)
        } else {
            return inflater.inflate(
                R.layout.fragment_bottom_sheet_update_icon_category,
                container,
                false
            )
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        type = arguments?.getString("type")!!


        imagesRecyclerView = view.findViewById(R.id.updateIconRecyclerView)
        imagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        imagesRecyclerView.setHasFixedSize(true)


        view.findViewById<TextView>(R.id.categoryIconEditExit).setOnClickListener {
            dismiss()
        }

        val storage = FirebaseStorage.getInstance()


        storageRef = if(type == "addAccounts")
            storage.reference.child("accounts")
        else if(type == "addGoals" || type == "goals" )
            storage.reference.child("purposes")
        else
            storage.reference.child("category")

        storageRef.listAll().addOnSuccessListener { listResult ->
            val images = mutableListOf<String>()

            listResult.items.forEach { imageRef ->
                images.add(imageRef.toString())
            }

            val adapter = ImageAdapter(images)
            adapter.setOnItemClickListener(this)
            imagesRecyclerView.adapter = adapter

        }


    }

    override fun onItemClick(icon: String) {
         iconViewModel.selectIcon(icon,type)
         dismiss()
    }


}