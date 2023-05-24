package com.example.finans.category.updateCategory

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso

interface OnItemClickListener {
    fun onItemClick(icon: String)

}
class ImageAdapter(private val images: List<String>) : RecyclerView.Adapter<ImageAdapter.ViewHolder>() {

    private var imageClickListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.icon_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val startIndex = position * 5
        val endIndex = minOf(startIndex + 5, images.size)

        if (startIndex >= images.size) {
            holder.itemView.visibility = View.GONE
            return
        } else {
            holder.itemView.visibility = View.VISIBLE
        }

        holder.clearImageViews()

        val storage = Firebase.storage

        for (i in startIndex until endIndex) {
            val imageUrl = images[i]
            val gsReference = storage.getReferenceFromUrl(imageUrl)

            val imageView = ImageView(holder.itemView.context)
            val desiredWidthInDp = 60
            val desiredHeightInDp = 60

            val density = holder.itemView.context.resources.displayMetrics.density
            val desiredWidth = (desiredWidthInDp * density).toInt()
            val desiredHeight = (desiredHeightInDp * density).toInt()

            val layoutParams = LinearLayout.LayoutParams(
                desiredWidth,

                desiredHeight
            )
            layoutParams.rightMargin = 32

            imageView.layoutParams = layoutParams

            holder.linearLayout.addView(imageView)

            gsReference.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri.toString()).into(imageView)
            }

            imageView.setOnClickListener {
                val clickedImage = images[i]
                imageClickListener?.onItemClick(clickedImage)
            }
        }
    }

    override fun getItemCount(): Int {
        return (images.size + 4) / 5
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.imageClickListener = listener
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val linearLayout: LinearLayout = itemView.findViewById(R.id.linearLayout)

        fun clearImageViews() {
            linearLayout.removeAllViews()
        }
    }
}
