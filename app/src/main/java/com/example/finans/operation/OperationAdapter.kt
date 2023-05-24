package com.example.finans.operation

import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*


interface OnItemClickListener {
    fun onItemClick(operation: Operation)

}

class OperationAdapter(private val operationList: ArrayList<Operation>) :
    RecyclerView.Adapter<OperationAdapter.ViewHolder>() {
    var selectedItem  = -1
    var sharedPreferences: SharedPreferences? = null
    var switchState: Boolean? = null

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
                        if (row.typeEn?.lowercase(Locale.ROOT)
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

       val itemView = if(switchState!!){
            LayoutInflater.from(parent.context).inflate(
                R.layout.operation_dark_item,
                parent, false)
        } else{
            LayoutInflater.from(parent.context).inflate(
                R.layout.operation_item,
                parent, false)
        }


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
        val settings = sharedPreferences?.getString("locale", "")

        if (settings == "ru"){
            holder.type.text = operation.typeRu
            holder.category.text = operation.categoryRu
        } else {
            holder.type.text = operation.typeEn
            holder.category.text = operation.categoryEn
        }
        val date = operation.timestamp?.toDate()
        val pattern = "dd.MM.yyyy HH:mm"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())


        val dateString = date?.let { simpleDateFormat.format(it) }


        holder.time.text = dateString
        holder.value.text = operation.value.toString()



        holder.itemView.isSelected = selectedItem == position

        holder.itemView.setOnClickListener {
            selectedItem = position
            notifyDataSetChanged()
            listener?.onItemClick(operationListFiltered[position])
        }

    }

    fun setSharedPreferencesLocale(sharedPreferences: SharedPreferences, switchState: Boolean) {
        this.sharedPreferences = sharedPreferences
        this.switchState = switchState
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