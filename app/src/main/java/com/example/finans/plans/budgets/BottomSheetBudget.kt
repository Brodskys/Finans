package com.example.finans.plans.budgets

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anychart.AnyChart
import com.anychart.AnyChartView
import com.anychart.chart.common.dataentry.DataEntry
import com.anychart.chart.common.dataentry.ValueDataEntry
import com.anychart.charts.Cartesian
import com.anychart.core.annotations.Line
import com.anychart.data.Mapping
import com.anychart.enums.Anchor
import com.anychart.enums.MarkerType
import com.anychart.enums.TooltipPositionMode
import com.anychart.graphics.vector.Stroke
import com.example.finans.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.anychart.data.Set
import com.example.finans.operation.Operation
import com.example.finans.operation.OperationAdapter
import com.example.finans.pla.BottomSheetBudgetUpdate
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.Collections
import java.util.Locale

class BottomSheetBudget : BottomSheetDialogFragment() {
    private lateinit var budgets: Budgets
    private lateinit var cartesian: Cartesian
    private lateinit var operationRecyclerView: RecyclerView
    private lateinit var operationArrayList: ArrayList<Operation>
    private lateinit var operationAdapter: OperationAdapter

    private lateinit var chart: AnyChartView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }
        dialog?.setCancelable(false)

        return inflater.inflate(R.layout.fragment_bottom_sheet_budget, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)
        budgets = arguments?.getParcelable("budgets")!!

        operationRecyclerView = view.findViewById(R.id.staticBudgetsOperationRecyclerView)
        operationRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        operationArrayList = arrayListOf()

        operationAdapter = OperationAdapter(operationArrayList)
        val loc = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)
        operationAdapter.setSharedPreferencesLocale(loc, switchState)
        operationRecyclerView.adapter = operationAdapter
        operationAdapter.notifyDataSetChanged()



        view.findViewById<EditText>(R.id.budgetsBalanceNameEdit)
            .setText(budgets.valueNow.toString())

        chart = view.findViewById(R.id.lineChartBudgets)


        cartesian = AnyChart.line()
        chart.setChart(cartesian)

        analyticsDate()

        view.findViewById<TextView>(R.id.budgetExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.budgetUpdate).setOnClickListener {
            val newFragment = BottomSheetBudgetUpdate.newInstance(budgets)
            newFragment.setTargetFragment(this@BottomSheetBudget, 0)

            newFragment.show(
                requireActivity().supportFragmentManager,
                "BottomSheetBudgetUpdate"
            )
            dismiss()

        }
    }

    private fun getOperationData() {

        Firebase.firestore.collection("users")
            .document(Firebase.auth.uid.toString())
            .collection("budgets").document(budgets.id!!)
            .collection("operation")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                operationArrayList.clear()
                for (dc in value?.documentChanges!!) {
                    val url = dc.document.toObject(lineChartBudgets::class.java)

                    FirebaseFirestore.getInstance()
                        .document(url.operationUrl!!)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val data = documentSnapshot.toObject(Operation::class.java)

                                operationArrayList.add(data!!)
                                operationAdapter.notifyDataSetChanged()
                            }
                        }
                        .addOnFailureListener { e ->
                        }
                }



        }

    }

    private fun analyticsDate() {
        Firebase.firestore.collection("users").document(Firebase.auth.uid.toString())
            .collection("budgets")
            .document(budgets.id!!)
            .collection("operation")
            .get().addOnSuccessListener { result ->

                val seriesData: MutableList<DataEntry> = ArrayList()

                var maxValue = 0.0

                budgets.maxValue?.let { maxValue = it }

                val sortedDocuments = result.documents.sortedBy { it.toObject(lineChartBudgets::class.java)?.timestamp }

                seriesData.add(ValueDataEntry("0", 0.0))

                var previousValue = 0.0

                for (document in sortedDocuments) {
                    val data = document.toObject(lineChartBudgets::class.java)
                    if (data != null) {
                        val timestamp = data.timestamp
                        val formattedTimestamp = SimpleDateFormat("dd/MM", Locale.getDefault()).format(timestamp!!.toDate())
                        val value = data.value ?: 0.0
                        seriesData.add(ValueDataEntry(formattedTimestamp, previousValue + value))
                        previousValue += value
                    }
                }

                cartesian.animation(true)
                cartesian.padding(10.0, 20.0, 5.0, 20.0)
                cartesian.crosshair().enabled(true)
                cartesian.crosshair()
                    .yLabel(true)
                    .yStroke(null as Stroke?, null, null, null as String?, null as String?)
                cartesian.tooltip().positionMode(TooltipPositionMode.POINT)
                cartesian.title(getString(R.string.trend))
                cartesian.xAxis(0).labels().padding(5.0, 5.0, 5.0, 5.0)

                val set: Set = Set.instantiate()
                set.data(seriesData)

                val series1Mapping: Mapping = set.mapAs("{ x: 'x', value: 'value' }")
                val series1: com.anychart.core.cartesian.series.Line? = cartesian.line(series1Mapping)
                series1!!.name(budgets.name)
                series1.hovered().markers().enabled(false)
                series1.hovered().markers()
                    .type(MarkerType.CIRCLE)
                    .size(4.0)
                series1.tooltip()
                    .position("right")
                    .anchor(Anchor.LEFT_CENTER)
                    .offsetX(5.0)
                    .offsetY(5.0)

                cartesian.yScale().maximum(maxValue)

                cartesian.yAxis(0).labels().format("{%Value}{scale:(1000)(1)|(K)}")

                cartesian.legend().enabled(true)
                cartesian.legend().fontSize(13.0)
                cartesian.legend().padding(0.0, 0.0, 10.0, 0.0)


                getOperationData()
                }

    }


    companion object {
        fun newInstance(
            budgets: Budgets
        ): BottomSheetBudget {
            val args = Bundle()
            args.putParcelable("budgets", budgets)

            val fragment = BottomSheetBudget()
            fragment.arguments = args

            return fragment
        }
    }

}