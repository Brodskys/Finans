package com.example.finans.plans.goals

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*


interface OnItemClickListener {
    fun onItemClick(goal: Goals)

}

class GoalsAdapter(private val goalsList: ArrayList<Goals>) :
    RecyclerView.Adapter<GoalsAdapter.ViewHolder>() {
    var selectedItem = -1
    var sharedPreferences: SharedPreferences? = null
    var switchState: Boolean? = null

    private var goalsListFiltered: ArrayList<Goals> = goalsList
    private var listener: OnItemClickListener? = null


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView =
            LayoutInflater.from(parent.context).inflate(
                R.layout.goals_item,
                parent, false
            )


        return ViewHolder(itemView)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val goals: Goals = goalsListFiltered[position]

        val storage = Firebase.storage

        if (goals.icon != "") {
            val gsReference = storage.getReferenceFromUrl(goals.icon!!)

            gsReference.downloadUrl.addOnSuccessListener { uri ->
                Picasso.get().load(uri.toString()).into(holder.image)
            }.addOnFailureListener {

            }
        } else {
            holder.image.setImageResource(R.drawable.question)
        }



        holder.name.text = goals.name



        holder.value.text =
            "${goals.valueNow} ${goals.currency} – ${goals.value} ${goals.currency}"


        if(goals.valueNow!!.toInt() == 0) {
            Picasso.get().load(R.drawable.cancellation).into(holder.imageStatus)
        }
        else if(goals.valueNow!!.toInt() < goals.value!!.toInt()){
            Picasso.get().load(R.drawable.expectation).into(holder.imageStatus)
        }
        else if(goals.valueNow!!.toInt() >= goals.value!!.toInt()){
            Picasso.get().load(R.drawable.done).into(holder.imageStatus)
        }

        holder.progress.max = goals.value!!.toInt()
        holder.progress.progress = goals.valueNow!!.toInt()

        holder.account.text = goals.account

        holder.itemView.isSelected = selectedItem == position

        holder.itemView.setOnClickListener {
            selectedItem = position
            notifyDataSetChanged()
            listener?.onItemClick(goalsListFiltered[position])
        }

    }

    fun setSharedPreferencesLocale(sharedPreferences: SharedPreferences, switchState: Boolean) {
        this.sharedPreferences = sharedPreferences
        this.switchState = switchState
    }

    override fun getItemCount(): Int {

        return goalsListFiltered.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val image = itemView.findViewById<ImageView>(R.id.goalsIconRecyclerView)
        val imageStatus = itemView.findViewById<ImageView>(R.id.goalsIconStatusRecyclerView)
        val name = itemView.findViewById<TextView>(R.id.goalsNameRecyclerView)
        val value = itemView.findViewById<TextView>(R.id.goalsNameValueCurrency)
        val progress = itemView.findViewById<ProgressBar>(R.id.goalsProgressBar)

        val account = itemView.findViewById<TextView>(R.id.goalsAccountRecyclerView)

    }

}