package com.example.finans.category.updateCategory

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.category.subcategory.SubcategoryAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class BottomSheetUpdateIconCategoryFragment : BottomSheetDialogFragment() {

    private lateinit var imagesRecyclerView: RecyclerView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        (dialog as? BottomSheetDialog)?.let {
            it.behavior.peekHeight  = R.style.AppBottomSheetDialogTheme
        }

        return inflater.inflate(
            R.layout.fragment_bottom_sheet_update_icon_category,
            container,
            false
        )
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        imagesRecyclerView = view.findViewById(R.id.updateIconRecyclerView)
        imagesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        imagesRecyclerView.setHasFixedSize(true)


        view.findViewById<TextView>(R.id.categoryIconEditExit).setOnClickListener {
            dismiss()
        }

        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("category")

        storageRef.listAll().addOnSuccessListener { listResult ->
            val images = mutableListOf<String>()

            // Перебираем все файлы и добавляем ссылки на изображения в список
            listResult.items.forEach { imageRef ->
                images.add(imageRef.toString())
            }

            // Создаем и устанавливаем адаптер для RecyclerView
            val adapter = ImageAdapter(images)
            imagesRecyclerView.adapter = adapter
        }


    }

}