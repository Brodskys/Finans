package com.example.finans.category

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.сurrency.CurrencyAdapter
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.properties.Delegates

interface OnItemClickListener {
    fun onItemClick(category: Category)

}

class CategoryAdapter(private val categoryList: ArrayList<Category>) :RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {
    var selectedItem  = -1
    lateinit var sharedPreferencesLanguage: SharedPreferences
    lateinit var sharedPreferencesTheme: SharedPreferences

    private var categoryListFiltered: ArrayList<Category> = categoryList
    private var listener: OnItemClickListener? = null

    var switchState:Boolean = false

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                categoryListFiltered = if (charSearch.isEmpty()) {
                    categoryList
                } else {
                    val resultList = ArrayList<Category>()
                    for (row in categoryList) {
                        if (row.NameRus?.lowercase(Locale.ROOT)
                                ?.contains(charSearch.lowercase(Locale.ROOT)) == true
                        )
                        {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = categoryListFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                categoryListFiltered = results?.values as ArrayList<Category>
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

         switchState = sharedPreferencesTheme.getBoolean("modeSwitch", false)

        return if(switchState) {
            val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.category_dark_item,
                parent, false
            )
            ViewHolder(itemView)
        } else{
            val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.category_item,
                parent, false
            )
            ViewHolder(itemView)
        }


    }

    fun setOnItemClickListener(listener: BottomSheetCategoryFragment) {
        this.listener = listener
    }

    @SuppressLint("ResourceAsColor")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val category: Category = categoryListFiltered[position]

        val storage = Firebase.storage


        val gsReference = storage.getReferenceFromUrl(category.Image!!)

        gsReference.downloadUrl.addOnSuccessListener { uri ->
            Picasso.get().load(uri.toString()).into(holder.Image)
        }.addOnFailureListener {
           Picasso.get().load(R.drawable.category).into(holder.Image)
        }



        val sharedPref =  sharedPreferencesLanguage.getString("locale", "")
        if (sharedPref == "ru"){
            holder.Name.text = category.NameRus
        } else {
            holder.Name.text = category.NameEng
        }

        holder.itemView.isSelected = selectedItem == position

        holder.itemView.setOnClickListener {
            selectedItem = position
            notifyDataSetChanged()
            listener?.onItemClick(categoryListFiltered[position])
        }

    }

    fun setSharedPreferencesLocale(sharedPreferencesLanguage: SharedPreferences, sharedPreferencesTheme: SharedPreferences) {
        this.sharedPreferencesLanguage = sharedPreferencesLanguage
        this.sharedPreferencesTheme = sharedPreferencesTheme
    }

    override fun getItemCount(): Int {

        return categoryListFiltered.size
    }


    class  ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val Image = itemView.findViewById<ImageView>(R.id.categoryIcon)
        val Name = itemView.findViewById<TextView>(R.id.categoryName)



    }

}