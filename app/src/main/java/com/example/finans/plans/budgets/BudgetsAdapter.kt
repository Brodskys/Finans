package com.example.finans.plans.budgets

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.operation.Operation
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.text.DecimalFormat
import java.util.*


interface OnItemClickListener {
    fun onItemClick(budgets: Budgets)

}

class BudgetsAdapter(private val budgetsList: ArrayList<Budgets>) :
    RecyclerView.Adapter<BudgetsAdapter.ViewHolder>() {
    var selectedItem = -1
    var sharedPreferences: SharedPreferences? = null
    var switchState: Boolean? = null

    private var budgetsListFiltered: ArrayList<Budgets> = budgetsList
    private var listener: OnItemClickListener? = null

    fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val charSearch = constraint.toString()
                budgetsListFiltered = if (charSearch.isEmpty()) {
                    budgetsList
                } else {
                    val resultList = ArrayList<Budgets>()
                    for (row in  budgetsList) {
                        if (row.name!!.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT)) || row.typeEn!!.lowercase(Locale.ROOT)
                                .contains(charSearch.lowercase(Locale.ROOT)) || row.typeRu?.lowercase(Locale.ROOT)
                                ?.contains(charSearch.lowercase(Locale.ROOT)) == true
                        )
                        {
                            resultList.add(row)
                        }
                    }
                    resultList
                }
                val filterResults = FilterResults()
                filterResults.values =  budgetsListFiltered
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                budgetsListFiltered = results?.values as ArrayList<Budgets>
                notifyDataSetChanged()
            }
        }
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val itemView =
            LayoutInflater.from(parent.context).inflate(
                R.layout.budgets_item,
                parent, false
            )


        return ViewHolder(itemView)
    }

    fun setOnItemClickListener(listener: BottomSheetBudgets) {
        this.listener = listener
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val budgets: Budgets = budgetsListFiltered[position]





        val settings = sharedPreferences?.getString("locale", "")


        if (settings == "ru"){
            holder.budgetsNameTextView.text = budgets.typeRu
        } else {
            holder.budgetsNameTextView.text = budgets.typeEn
        }

        holder.budgetsNamePeriodTextView.text = budgets.name

        val format =
            DecimalFormat("#,##0.00")

        val max = budgets.maxValue
        val now = budgets.valueNow

        holder.budgetsSummaTextView.text = format.format(max)


        holder.progress.max = max!!.toInt()
        holder.progress.progress = now!!.toInt()

        holder.budgetsValueTextView.text = format.format(now)

        holder.budgetsValueNowTextView.text = format.format(max-now)

        holder.budgetsProcentTextView.text = ("${(now*100/max).toInt()}%").toString()

        holder.itemView.isSelected = selectedItem == position

        holder.itemView.setOnClickListener {
            selectedItem = position
            listener?.onItemClick(budgetsListFiltered[position])
        }

    }

    fun setSharedPreferencesLocale(sharedPreferences: SharedPreferences, switchState: Boolean) {
        this.sharedPreferences = sharedPreferences
        this.switchState = switchState
    }

    override fun getItemCount(): Int {

        return budgetsListFiltered.size
    }


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val budgetsNameTextView = itemView.findViewById<TextView>(R.id.budgetsNameTextView)
        val budgetsNamePeriodTextView = itemView.findViewById<TextView>(R.id.budgetsNamePeriodTextView)
        val budgetsSummaTextView = itemView.findViewById<TextView>(R.id.budgetsSummaTextView)
        val budgetsValueTextView = itemView.findViewById<TextView>(R.id.budgetsValueTextView)
        val budgetsProcentTextView = itemView.findViewById<TextView>(R.id.budgetsProcentTextView)
        val budgetsValueNowTextView = itemView.findViewById<TextView>(R.id.budgetsValueNowTextView)

        val progress = itemView.findViewById<ProgressBar>(R.id.budgetsProgressBar)


    }

}