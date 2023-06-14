package com.example.finans.сurrency

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
import com.example.finans.language.languageConvert
import java.util.*

interface OnItemClickListener {
    fun onItemClick(currency: Currency)

}

class CurrencyAdapter(private val currencyList: ArrayList<Currency>) :RecyclerView.Adapter<CurrencyAdapter.ViewHolder>() {
    var selectedItem  = -1
    lateinit var sharedPreferencesLanguage: SharedPreferences
    lateinit var sharedPreferencesTheme: SharedPreferences

    private var currencyListFiltered: ArrayList<Currency> = currencyList
    private var listener: OnItemClickListener? = null

     fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                currencyListFiltered = if (charSearch.isEmpty()) {
                    currencyList
                } else {
                    val resultList = ArrayList<Currency>()
                    for (row in currencyList) {
                        if (row.FullNameRus?.lowercase(Locale.ROOT)
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
                currencyListFiltered = results?.values as ArrayList<Currency>
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

            val itemView = LayoutInflater.from(parent.context).inflate(
                R.layout.currency_item,
                parent, false
            )
            return  ViewHolder(itemView)

    }

    fun setOnItemClickListener(listener: BottomSheetCurrencyFragment) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val currency: Currency = currencyListFiltered[position]

        if (languageConvert(sharedPreferencesLanguage)){
            holder.fullName.text = currency.FullNameRus
        }
        else
            holder.fullName.text = currency.FullNameEng

        holder.name.text = currency.Name

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

            }
            else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }

        }
        else {

            holder.fullName.setTextColor(Color.rgb(150, 75, 0))
            holder.name.setTextColor(Color.rgb(150, 75, 0))
            if (selectedItem == position) {

                holder.itemView.setBackgroundColor(
                    ContextCompat.getColor(
                        holder.itemView.context,
                        R.color.background2
                    )
                )

            }
            else {
                holder.itemView.setBackgroundColor(Color.TRANSPARENT)
            }

        }

        holder.itemView.isSelected = selectedItem == position

        holder.itemView.setOnClickListener {

            selectedItem = position
            notifyDataSetChanged()
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

        val fullName = itemView.findViewById<TextView>(R.id.currencyFullName)
        val name = itemView.findViewById<TextView>(R.id.currencyName)



    }

}