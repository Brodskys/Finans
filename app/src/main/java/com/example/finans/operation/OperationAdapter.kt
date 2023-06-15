package com.example.finans.operation

import android.content.SharedPreferences
import android.graphics.Region.Op
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.CountDownLatch


interface OnItemClickListener {
    fun onItemClick(operation: Operation)

}

class OperationAdapter(private val operationList: ArrayList<Operation>) :
    RecyclerView.Adapter<OperationAdapter.ViewHolder>(),
    ItemTouchHelperAdapter {
    var selectedItem  = -1
    var sharedPreferences: SharedPreferences? = null
    var switchState: Boolean? = null

    private var  operationListFiltered: ArrayList<Operation> =  operationList
    private var listener: OnItemClickListener? = null
    private lateinit var categoryRu: String
    private lateinit var categoryEn: String

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                operationListFiltered = if (charSearch.isEmpty()) {
                    operationList
                } else {
                    val resultList = ArrayList<Operation>()
                    val latch = CountDownLatch(operationList.size)

                    for (row in operationList) {
                        val documentRef = FirebaseFirestore.getInstance().document("users/${Firebase.auth.uid.toString()}${row.category}")

                        documentRef.get()
                            .addOnSuccessListener { documentSnapshot ->
                                if (documentSnapshot.exists()) {
                                    val documentData = documentSnapshot.data

                                    categoryEn = documentData!!["nameEng"].toString()
                                    categoryRu = documentData["nameRus"].toString()

                                    if (categoryRu.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))
                                        || categoryEn.lowercase(Locale.ROOT).contains(charSearch.lowercase(Locale.ROOT))
                                        || row.typeEn?.lowercase(Locale.ROOT)?.contains(charSearch.lowercase(Locale.ROOT)) == true
                                        || row.typeRu?.lowercase(Locale.ROOT)?.contains(charSearch.lowercase(Locale.ROOT)) == true
                                    ) {
                                        resultList.add(row)
                                    }
                                }
                                latch.countDown()
                            }
                    }

                    latch.await()

                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values = operationListFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                operationListFiltered = results?.values as ArrayList<Operation>
                notifyDataSetChanged()
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if(switchState!!){
            val itemView =
                LayoutInflater.from(parent.context).inflate(
                    R.layout.operation_dark_item,
                    parent, false)


            return ViewHolder(itemView)
        } else{
            val itemView =
                LayoutInflater.from(parent.context).inflate(
                    R.layout.operation_item,
                    parent, false)

            return ViewHolder(itemView)
        }


    }

    fun setOnItemClickListener(listener: HomeActivity) {
        this.listener = listener
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {


        val operation: Operation = operationListFiltered[position]

        val storage = Firebase.storage




        val documentRef = FirebaseFirestore.getInstance().document("users/${Firebase.auth.uid.toString()}${operation.category}")
        val settings = sharedPreferences?.getString("locale", "")

        documentRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val documentData = documentSnapshot.data

                    val image = documentData!!["image"].toString()
                    categoryEn = documentData["nameEng"].toString()
                    categoryRu = documentData["nameRus"].toString()

                    val gsReference = storage.getReferenceFromUrl(image)

                    gsReference.downloadUrl.addOnSuccessListener { uri ->
                        Picasso.get().load(uri.toString()).into(holder.image)
                    }.addOnFailureListener {
                        Picasso.get().load(R.drawable.category).into(holder.image)
                    }

                    if (settings == "ru"){
                        holder.type.text = operation.typeRu
                        holder.category.text = categoryRu
                    } else {
                        holder.type.text = operation.typeEn
                        holder.category.text = categoryEn
                    }

                }
            }
            .addOnFailureListener { exception ->

            }


        val date = operation.timestamp?.toDate()
        val pattern = "dd MMMM HH:mm"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())

        val dateString = date?.let { simpleDateFormat.format(it) }

        val decimalFormat = DecimalFormat("#,##0.00")

        holder.time.text = dateString
        holder.value.text =  decimalFormat.format(operation.value).toString()


        holder.itemView.isSelected = selectedItem == position

        holder.itemView.setOnClickListener {
            selectedItem = position

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


    inner class ViewHolder internal constructor(view: View) : RecyclerView.ViewHolder(view) {

        val image = itemView.findViewById<ImageView>(R.id.categoryIcon)
        val type = itemView.findViewById<TextView>(R.id.operationType)
        val time = itemView.findViewById<TextView>(R.id.operationDatetime)
        val value = itemView.findViewById<TextView>(R.id.operationValue)
        val category = itemView.findViewById<TextView>(R.id.operationCategory)

        val categoryViewForeground = itemView.findViewById<ConstraintLayout>(R.id.categoryViewForeground)


    }
    fun removeItem(position: Int) {
        operationList.removeAt(position)
        notifyItemRemoved(position)
    }
    override fun onItemMove(fromPosition: Int, toPosition: Int): Boolean {
        return true
    }

    override fun onItemDismiss(position: Int) {
        operationList.removeAt(position)
        notifyItemRemoved(position)
    }

}