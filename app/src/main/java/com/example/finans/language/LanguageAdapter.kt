package com.example.finans.language

import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import java.util.*
import kotlin.collections.ArrayList

interface OnItemClickListener {
    fun onItemClick(language: Language)
}

class LanguageAdapter(private val languageList: ArrayList<Language>) :RecyclerView.Adapter<LanguageAdapter.ViewHolder>() {
    var selectedItem  = -1

    private var currencyListFiltered: ArrayList<Language> = languageList
    private var listener: OnItemClickListener? = null
    lateinit var sharedPreferencesLanguage: SharedPreferences
    lateinit var sharedPreferencesTheme: SharedPreferences

     fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                currencyListFiltered = if (charSearch.isEmpty()) {
                    languageList
                } else {
                    val resultList = ArrayList<Language>()
                    for (row in languageList) {
                        if (row.FullName?.lowercase(Locale.ROOT)
                                ?.contains(charSearch.lowercase(Locale.ROOT)) == true
                            ||
                            row.Name?.lowercase(Locale.ROOT)
                                ?.contains(charSearch.lowercase(Locale.ROOT)) == true
                        )
                        {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = currencyListFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                currencyListFiltered = results?.values as ArrayList<Language>
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.language_item,
        parent, false)
        return ViewHolder(itemView)
    }

    fun setOnItemClickListener(listener: BottomSheetLanguageFragment) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val language: Language = currencyListFiltered[position]

        holder.fullName.text = language.FullName


        holder.name.text = language.Name

        val switchState = sharedPreferencesTheme.getBoolean("modeSwitch", false)

        if(switchState) {
            holder.fullName.setTextColor(Color.rgb(211, 138, 6))
            holder.name.setTextColor(Color.rgb(211, 138, 6))

            if (selectedItem == position) {

                    holder.itemView.setBackgroundColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.background2_dark
                        )
                    )

            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }

        }
        else{
            holder.fullName.setTextColor(Color.rgb(150, 75, 0))
            holder.name.setTextColor(Color.rgb(150, 75, 0))

            if (selectedItem == position) {

                    holder.itemView.setBackgroundColor(
                        ContextCompat.getColor(
                            holder.itemView.context,
                            R.color.background2
                        )
                    )

            } else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }

        }




        holder.itemView.isSelected = selectedItem == position

        holder.itemView.setOnClickListener {

            selectedItem = position // устанавливаем выбранный элемент
            notifyDataSetChanged() // обновляем адаптер, чтобы перерисовать список
            listener?.onItemClick(currencyListFiltered[position])
        }

    }

    fun setSharedPreferencesLocale(sharedPreferencesLanguage: SharedPreferences, sharedPreferencesTheme: SharedPreferences) {
        this.sharedPreferencesLanguage = sharedPreferencesLanguage
        this.sharedPreferencesTheme = sharedPreferencesTheme
    }

    override fun getItemCount(): Int {

        return currencyListFiltered.size
    }


    class  ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val fullName = itemView.findViewById<TextView>(R.id.languageFullName)
        val name = itemView.findViewById<TextView>(R.id.languageName)



    }

}