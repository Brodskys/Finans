package com.example.finans.category.updateCategory

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso



class ImageAdapter(private val images: List<String>) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.icon_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val startIndex = position * 5 // начальный индекс для загрузки 4 изображений
        val endIndex = minOf(startIndex + 5, images.size) // конечный индекс для загрузки 4 изображений

        if (startIndex >= images.size) {
            holder.itemView.visibility = View.GONE
            return
        } else {
            holder.itemView.visibility = View.VISIBLE
        }

        val storage = Firebase.storage


        for (i in startIndex until endIndex) {
            val imageUrl = images[i]

            val gsReference = storage.getReferenceFromUrl(imageUrl)

            when(i-startIndex) {
                0 -> {
                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri.toString()).into(holder.imageView1)
                    }
                }
                1 -> {
                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString()).into(holder.imageView2)
                }}
                2 -> {
                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString()).into(holder.imageView3)
                }}
                3 -> {
                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri.toString()).into(holder.imageView4)
                }}
                4 -> {
                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri.toString()).into(holder.imageView5)
                    }}
            }
        }
    }

    override fun getItemCount(): Int {
        return (images.size + 4) / 5
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView1: ImageView = itemView.findViewById(R.id.imageView1)
        val imageView2: ImageView = itemView.findViewById(R.id.imageView2)
        val imageView3: ImageView = itemView.findViewById(R.id.imageView3)
        val imageView4: ImageView = itemView.findViewById(R.id.imageView4)
        val imageView5: ImageView = itemView.findViewById(R.id.imageView5)

    }
}