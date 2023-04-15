package com.example.finans.operation

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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.util.*


interface OnItemClickListener {
    fun onItemClick(operation: Operation)

}

class OperationAdapter(private val operationList: ArrayList<Operation>) :
    RecyclerView.Adapter<OperationAdapter.ViewHolder>() {
    var selectedItem  = -1
    lateinit var sharedPreferences: SharedPreferences

    private var  operationListFiltered: ArrayList<Operation> =  operationList
    private var listener: OnItemClickListener? = null



    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                operationListFiltered = if (charSearch.isEmpty()) {
                    operationList
                } else {
                    val resultList = ArrayList<Operation>()
                    for (row in  operationList) {
                        if (row.type?.lowercase(Locale.ROOT)
                                ?.contains(charSearch.lowercase(Locale.ROOT)) == true
                        )
                        {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values =  operationListFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                operationListFiltered = results?.values as ArrayList<Operation>
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView = LayoutInflater.from(parent.context).inflate(
            R.layout.operation_item,
            parent, false)
        return ViewHolder(itemView)
    }

    fun setOnItemClickListener(listener: HomeActivity) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val operation: Operation = operationListFiltered[position]

        val storage = Firebase.storage

        if(operation.image != null) {
            val gsReference = storage.getReferenceFromUrl(operation.image!!)

            gsReference.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri.toString()).into(holder.image)
            }.addOnFailureListener {
                Picasso.get().load(R.drawable.category).into(holder.image)
            }
        }
//        val sharedPref =  sharedPreferences.getString("locale", "")
//        if (sharedPref == "ru"){
//            holder.Name.text = category.NameRus
//        } else {
//            holder.Name.text = category.NameEng
//        }


        holder.type.text = operation.type
        holder.time.text = operation.time
        holder.value.text = operation.value.toString()
        holder.category.text = operation.category

        holder.itemView.isSelected = selectedItem == position

        holder.itemView.setOnClickListener {
            selectedItem = position
            notifyDataSetChanged()
            listener?.onItemClick(operationListFiltered[position])
        }

    }

    fun setSharedPreferencesLocale(sharedPreferences: SharedPreferences) {
        this.sharedPreferences = sharedPreferences
    }

    override fun getItemCount(): Int {

        return operationListFiltered.size
    }


    class  ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image = itemView.findViewById<ImageView>(R.id.categoryIcon)
        val type = itemView.findViewById<TextView>(R.id.operationType)
        val time = itemView.findViewById<TextView>(R.id.operationDatetime)
        val value = itemView.findViewById<TextView>(R.id.operationValue)
        val category = itemView.findViewById<TextView>(R.id.operationCategory)



    }

}