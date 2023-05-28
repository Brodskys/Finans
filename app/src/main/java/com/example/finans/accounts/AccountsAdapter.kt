package com.example.finans.accounts

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.util.*


interface OnItemClickListener {
    fun onItemClick(accounts: Accounts)

}

class AccountsAdapter(private val accountsList: ArrayList<Accounts>) :
    RecyclerView.Adapter<AccountsAdapter.ViewHolder>() {
    var selectedItem  = -1
    var sharedPreferences: SharedPreferences? = null
    var switchState: Boolean? = null

    private var  accountsListFiltered: ArrayList<Accounts> = accountsList
    private var listener: OnItemClickListener? = null



    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                accountsListFiltered = if (charSearch.isEmpty()) {
                    accountsList
                } else {
                    val resultList = ArrayList<Accounts>()
                    for (row in  accountsList) {
                        if (row.nameEng?.lowercase(Locale.ROOT)
                                ?.contains(charSearch.lowercase(Locale.ROOT)) == true ||
                            row.nameRus?.lowercase(Locale.ROOT)
                                ?.contains(charSearch.lowercase(Locale.ROOT)) == true
                        )
                        {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values =  accountsListFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                accountsListFiltered = results?.values as ArrayList<Accounts>
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

       val itemView =
            LayoutInflater.from(parent.context).inflate(
                R.layout.accounts_item,
                parent, false)


        return ViewHolder(itemView)
    }

    fun setOnItemClickListener(listener: BottomSheetAccounts) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val accounts: Accounts = accountsListFiltered[position]

        val storage = Firebase.storage

        if(accounts.icon != null) {
            val gsReference = storage.getReferenceFromUrl(accounts.icon!!)

            gsReference.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri.toString()).into(holder.image)
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.coins).into(holder.image)
            }
        }
        val settings = sharedPreferences?.getString("locale", "")

        if (settings == "ru"){
            holder.name.text = accounts.nameRus
        } else {
            holder.name.text = accounts.nameEng
        }

        holder.itemView.isSelected = selectedItem == position

        holder.itemView.setOnClickListener {
            selectedItem = position
            notifyDataSetChanged()
            listener?.onItemClick(accountsListFiltered[position])
        }

    }

    fun setSharedPreferencesLocale(sharedPreferences: SharedPreferences) {
        this.sharedPreferences = sharedPreferences
    }

    override fun getItemCount(): Int {

        return accountsListFiltered.size
    }


    class  ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image = itemView.findViewById<ImageView>(R.id.accounts_itemIcon)
        val name = itemView.findViewById<TextView>(R.id.accounts_itemName)


    }

}