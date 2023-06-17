package com.example.finans.plans.paymentPlanning

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.category.Category
import com.example.finans.language.languageInit
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.*
import java.util.concurrent.TimeUnit


interface OnItemClickListener {
    fun onItemClick(paymentPlanning: PaymentPlanning)

}

class PaymentPlanningAdapter(private val paymentPlanningList: ArrayList<PaymentPlanning>) :
    RecyclerView.Adapter<PaymentPlanningAdapter.ViewHolder>() {
    var selectedItem = -1
    var sharedPreferences: SharedPreferences? = null
    var context: Context? = null
    var switchState: Boolean? = null

    private var paymentPlanningListFiltered: ArrayList<PaymentPlanning> = paymentPlanningList
    private var listener: OnItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        if(switchState!!){
            val itemView =
                LayoutInflater.from(parent.context).inflate(
                    R.layout.payment_planning_dark_item,
                    parent, false)


            return ViewHolder(itemView)
        } else{
            val itemView =
                LayoutInflater.from(parent.context).inflate(
                    R.layout.payment_planning_item,
                    parent, false)

            return ViewHolder(itemView)
        }

    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val paymentPlanning: PaymentPlanning = paymentPlanningListFiltered[position]

        val storage = Firebase.storage
        val settings = sharedPreferences?.getString("locale", "")

        val documentRef = FirebaseFirestore.getInstance().document("users/${Firebase.auth.uid.toString()}${paymentPlanning!!.category}")


        documentRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val categoryPaument = documentSnapshot.toObject(Category::class.java)

                        val gsReference = storage.getReferenceFromUrl(categoryPaument!!.image!!)

                        gsReference.downloadUrl.addOnSuccessListener { uri ->
                            Picasso.get().load(uri.toString()).into(holder.paymentPlanningIcon)
                        }.addOnFailureListener {

                        }


                    if (settings == "ru"){
                        holder.paymentPlanningNameCategory.text = categoryPaument.nameRus
                    } else {
                        holder.paymentPlanningNameCategory.text = categoryPaument.nameEng
                    }

                }
            }
            .addOnFailureListener { exception ->

            }


        holder.paymentPlanningName.text = paymentPlanning.name


        val decimalFormat = DecimalFormat("#,##0.00")

        holder.paymentPlanningValue.text =  decimalFormat.format(paymentPlanning.value)


        holder.paymentPlanningCurrency.text = paymentPlanning.currency



        val date = paymentPlanning.timestamp?.toDate()
        val pattern = "dd MMMM HH:mm"
        val simpleDateFormat = SimpleDateFormat(pattern, Locale.getDefault())


        val dateString = date?.let { simpleDateFormat.format(it) }


        holder.paymentPlanningDate.text = dateString

        val currentDate = LocalDate.now()

        val daysDifference = ChronoUnit.DAYS.between(currentDate, date!!.toInstant().atZone(
            ZoneId.systemDefault()).toLocalDate())

        if (daysDifference > 0) {
            holder.paymentPlanningDate.setTextColor(Color.rgb(76,175,80))
        } else if (daysDifference < 0) {
            holder.paymentPlanningDate.setTextColor(Color.rgb(244,67,54))
        } else {
            holder.paymentPlanningDate.setTextColor(Color.rgb(128, 128, 128))
        }

        if(paymentPlanning.status == "InProgress"){
            Picasso.get().load(R.drawable.cancellation).into(holder.paymentPlanningIconStatus)
        }else{
            Picasso.get().load(R.drawable.done).into(holder.paymentPlanningIconStatus)
        }


        holder.itemView.isSelected = selectedItem == position

        holder.itemView.setOnClickListener {
            selectedItem = position
            notifyDataSetChanged()
            listener?.onItemClick(paymentPlanningListFiltered[position])
        }

    }

    fun setSharedPreferencesLocale(sharedPreferences: SharedPreferences, switchState: Boolean, context: Context) {
        this.sharedPreferences = sharedPreferences
        this.switchState = switchState
        this.context = context
    }

    override fun getItemCount(): Int {

        return paymentPlanningListFiltered.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val paymentPlanningIcon: ImageView = itemView.findViewById(R.id.paymentPlanningIcon)
        val paymentPlanningIconStatus: ImageView = itemView.findViewById(R.id.paymentPlanningIconStatus)

        val paymentPlanningName: TextView = itemView.findViewById(R.id.paymentPlanningName)
        val paymentPlanningNameCategory: TextView = itemView.findViewById(R.id.paymentPlanningNameCategory)
        val paymentPlanningValue: TextView = itemView.findViewById(R.id.paymentPlanningValue)
        val paymentPlanningCurrency: TextView = itemView.findViewById(R.id.paymentPlanningCurrency)
        val paymentPlanningDate: TextView = itemView.findViewById(R.id.paymentPlanningDate)


    }

}