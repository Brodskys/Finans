package com.example.finans.accounts

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.plans.goals.Goals
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class BottomSheetAccounts : BottomSheetDialogFragment(), OnItemClickListener {
    private lateinit var accountsRecyclerView: RecyclerView
    private lateinit var accountsArrayList: ArrayList<Accounts>
    private lateinit var accountsAdapter: AccountsAdapter
    private var type: String? = null
    private var type2: String? = null
    private var toAccounts: Accounts? = null
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var accountsViewModel: AccountsViewModel
    private lateinit var accountsViewModel2: AccountsViewModel2

    private var accountsList: ArrayList<Accounts>? = null
    private lateinit var accountsBudgetsViewModel: AccountsBudgetsViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountsViewModel = ViewModelProvider(requireActivity())[AccountsViewModel::class.java]
        accountsViewModel2 = ViewModelProvider(requireActivity())[AccountsViewModel2::class.java]
        accountsBudgetsViewModel =
            ViewModelProvider(requireActivity())[AccountsBudgetsViewModel::class.java]
    }

    companion object {
        fun newInstance(
            type: String
        ): BottomSheetAccounts {
            val args = Bundle()
            args.putString("type", type)

            val fragment = BottomSheetAccounts()
            fragment.arguments = args
            return fragment
        }

        fun newInstance2(
            toAccounts: Accounts?,
            type: String
        ): BottomSheetAccounts {
            val args = Bundle()
            args.putString("type2", type)
            args.putParcelable("accounts", toAccounts)

            val fragment = BottomSheetAccounts()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {
            it.behavior.peekHeight = R.style.AppBottomSheetDialogTheme
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_accounts, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_accounts, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        toAccounts = arguments?.getParcelable("accounts")

        type = arguments?.getString("type") ?: "newOper"
        type2 = arguments?.getString("type2")

        if (type == "budgets") {
            view.findViewById<LinearLayout>(R.id.accountsLinearLayout).visibility = View.GONE

            view.findViewById<TextView>(R.id.accountsEditExit).setText(R.string.back2)
        }

        accountsRecyclerView = view.findViewById(R.id.accountsRecyclerView)
        accountsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        accountsArrayList = arrayListOf()

        accountsAdapter = AccountsAdapter(accountsArrayList)
        accountsAdapter.setOnItemClickListener(this)

        val loc = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        accountsAdapter.setSharedPreferencesLocale(loc,switchState, type)

        accountsRecyclerView.adapter = accountsAdapter

        accountsAdapter.notifyDataSetChanged()



        getOperationData()

        view.findViewById<RelativeLayout>(R.id.addAccountsRelativeLayout).setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAccountsAdd")
            if (existingFragment == null) {
                val newFragment = BottomSheetAccountsAdd()
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAccountsAdd"
                )
            }

        }

        view.findViewById<TextView>(R.id.accountsEditExit).setOnClickListener {

            if (accountsList != null)
                accountsBudgetsViewModel.selectAccountsBudgets(accountsList!!)

            dismiss()
        }

        view.findViewById<SearchView>(R.id.accountsSearch)
            .setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    accountsAdapter.getFilter().filter(newText)
                    return false
                }

            })

    }

    private fun getOperationData() {

        FirebaseFirestore.getInstance().collection("users").document(Firebase.auth.uid.toString())
            .collection("accounts")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    return@addSnapshotListener
                }

                for (dc in value?.documentChanges!!) {
                    val accounts = dc.document.toObject(Accounts::class.java)
                    val index = accountsArrayList.indexOfFirst { it.name == accounts.name }
                    when (dc.type) {
                        DocumentChange.Type.ADDED -> {
                            if (index == -1) {
                                if (toAccounts != null) {
                                    if (accounts != toAccounts) {
                                        accountsArrayList.add(accounts)
                                    }
                                } else
                                    accountsArrayList.add(accounts)
                            }
                        }

                        DocumentChange.Type.MODIFIED -> {
                            if (index != -1) {
                                accountsArrayList[index] = accounts
                            }
                        }

                        DocumentChange.Type.REMOVED -> {
                            if (index != -1) {
                                accountsArrayList.removeAt(index)
                            }
                        }
                    }
                }

                accountsAdapter.notifyDataSetChanged()
            }
    }

    override fun onItemClick(accounts: Accounts) {
        if (type == "selectMainAccount") {
            val sharedPref =
                requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)

            val docRef = FirebaseFirestore.getInstance().collection("users")
                .document(Firebase.auth.uid.toString()).collection("user")
                .document("information")

            val accountsCurrncy = hashMapOf<String, Any>(
                "accounts" to accounts.name!!
            )

            val close = requireActivity()

            docRef.update(accountsCurrncy)
                .addOnSuccessListener {

                    val editor = sharedPref?.edit()
                    editor!!.putString("accounts", accounts.name)
                    editor.apply()

                    close.recreate()
                }
                .addOnFailureListener { e ->
                }


        } else {
            if (type2 != null) {
                accountsViewModel2.selectAccounts(accountsType(accounts, type2))
            } else

                accountsViewModel.selectAccounts(accounts)
        }

        dismiss()

    }

    override fun onItemsClick(accounts: java.util.ArrayList<Accounts>) {

        accountsList = accounts

    }

}