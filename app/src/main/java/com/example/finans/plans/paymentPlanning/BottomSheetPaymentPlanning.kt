package com.example.finans.plans.paymentPlanning

import android.content.Context
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.category.subcategory.BottomSheetSubcategoryFragment
import com.example.finans.plans.goals.Goals
import com.example.finans.plans.goals.GoalsAdapter
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase

class BottomSheetPaymentPlanning : BottomSheetDialogFragment(), OnItemClickListener {
    private lateinit var paymentPlanningRecyclerView: RecyclerView
    private lateinit var paymentPlanningArrayList: ArrayList<PaymentPlanning>
    private lateinit var paymentPlanningAdapter: PaymentPlanningAdapter
    private lateinit var userId: DocumentReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight  = R.style.AppBottomSheetDialogTheme
        }
        dialog?.setCancelable(false)
        return inflater.inflate(R.layout.fragment_bottom_sheet_payment_planning, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        paymentPlanningRecyclerView = view.findViewById(R.id.paymentPlanningRecyclerView)
        paymentPlanningRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        paymentPlanningArrayList = arrayListOf()

        paymentPlanningAdapter = PaymentPlanningAdapter(paymentPlanningArrayList)
        paymentPlanningAdapter.setOnItemClickListener(this)

        val loc = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        paymentPlanningAdapter.setSharedPreferencesLocale(loc, switchState, requireContext())

        paymentPlanningRecyclerView.adapter = paymentPlanningAdapter

        paymentPlanningAdapter.notifyDataSetChanged()

        userId = FirebaseFirestore.getInstance().collection("users")
            .document(Firebase.auth.uid.toString())

        getPaymentPlanningData()

        view.findViewById<TextView>(R.id.paymentPlanningExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<RelativeLayout>(R.id.addPaymentPlanningRelativeLayout).setOnClickListener {
            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetPaymentPlanningAdd")
            if (existingFragment == null) {
                val newFragment = BottomSheetPaymentPlanningAdd()

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetPaymentPlanningAdd"
                )
            }
        }
    }
    private fun getPaymentPlanningData(){
        userId
            .collection("paymentPlanning")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }
                for (dc in value?.documentChanges!!) {
                    val updatedPaymentPlanning = dc.document.toObject(PaymentPlanning::class.java)
                    val index = paymentPlanningArrayList.indexOfFirst { it.id == updatedPaymentPlanning.id }
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            if (index != -1) {
                                paymentPlanningArrayList[index] = updatedPaymentPlanning
                            } else {
                                paymentPlanningArrayList.add(updatedPaymentPlanning)
                            }
                        }
                        DocumentChange.Type.MODIFIED -> {
                            if (index != -1) {
                                paymentPlanningArrayList[index] = updatedPaymentPlanning
                            }
                        }
                        DocumentChange.Type.REMOVED -> {
                            if (index != -1) {
                                paymentPlanningArrayList.removeAt(index)
                            }
                        }
                    }
                }
                paymentPlanningAdapter.notifyDataSetChanged()
            }
    }

    override fun onItemClick(paymentPlanning: PaymentPlanning) {
        val newFragment = BottomSheetPaymentPlanCard.newInstance(paymentPlanning)
        newFragment.setTargetFragment(this@BottomSheetPaymentPlanning, 0)

        newFragment.show(
            requireActivity().supportFragmentManager,
            "BottomSheetPaymentPlanCard"
        )
    }


}