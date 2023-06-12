package com.example.finans.plans.goals

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.category.subcategory.BottomSheetSubcategoryFragment
import com.example.finans.operation.Operation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class BottomSheetGoals : BottomSheetDialogFragment(), OnItemClickListener {
    private lateinit var goalsRecyclerView: RecyclerView
    private lateinit var goalsArrayList: ArrayList<Goals>
    private lateinit var goalsAdapter: GoalsAdapter
    private lateinit var userId: DocumentReference
    private lateinit var pref: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }

        dialog?.setCancelable(false)
        return inflater.inflate(R.layout.fragment_bottom_sheet_goals, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        dialog?.setCanceledOnTouchOutside(true)

        view.findViewById<TextView>(R.id.goalsExit).setOnClickListener {
            dismiss()
        }


        view.findViewById<RelativeLayout>(R.id.addGoalsRelativeLayout).setOnClickListener {
            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetGoalsAdd")
            if (existingFragment == null) {
                val newFragment = BottomSheetGoalsAdd()

                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetGoalsAdd"
                )
            }
        }


        goalsRecyclerView = view.findViewById(R.id.goalsRecyclerView)
        goalsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        goalsArrayList = arrayListOf()

        goalsAdapter = GoalsAdapter(goalsArrayList)
        goalsAdapter.setOnItemClickListener(this)


        goalsRecyclerView.adapter = goalsAdapter

        goalsAdapter.notifyDataSetChanged()

        userId = FirebaseFirestore.getInstance().collection("users")
            .document(Firebase.auth.uid.toString())

        getGoalsData()


    }

    fun getGoalsData() {


        userId
            .collection("goals")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                for (dc in value?.documentChanges!!) {
                    val goal = dc.document.toObject(Goals::class.java)
                    val index = goalsArrayList.indexOfFirst { it.id == goal.id }
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            if (index == -1) {
                                goalsArrayList.add(goal)
                            }
                        }

                        DocumentChange.Type.MODIFIED -> {
                            if (index != -1) {
                                goalsArrayList[index] = goal
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            if (index != -1) {
                                goalsArrayList.removeAt(index)
                            }
                        }
                    }
                }
                goalsAdapter.notifyDataSetChanged()
            }
    }


    override fun onItemClick(goal: Goals) {
        if (goal.valueNow!! >= goal.value!!) {
            val newFragment = BottomSheetGoal.newInstance(goal)
            newFragment.setTargetFragment(this@BottomSheetGoals, 0)

            newFragment.show(
                requireActivity().supportFragmentManager,
                "BottomSheetGoal"
            )
        }
        else{
            val newFragment = BottomSheetGoalCard.newInstance(goal)
            newFragment.setTargetFragment(this@BottomSheetGoals, 0)

            newFragment.show(
                requireActivity().supportFragmentManager,
                "BottomSheetGoalCard"
            )
        }

    }
}