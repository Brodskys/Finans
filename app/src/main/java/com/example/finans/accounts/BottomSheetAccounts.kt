package com.example.finans.accounts

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.finans.R
import com.example.finans.category.CategoryViewModel
import com.example.finans.category.subcategory.BottomSheetSubcategoryFragment
import com.example.finans.category.updateCategory.BottomSheetUpdateIcon
import com.example.finans.operation.Operation
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase


class BottomSheetAccounts : BottomSheetDialogFragment(), OnItemClickListener {
    private lateinit var accountsRecyclerView: RecyclerView
    private lateinit var accountsArrayList: ArrayList<Accounts>
    private lateinit var accountsAdapter: AccountsAdapter
    private lateinit var type: String
    private lateinit var accountsViewModel: AccountsViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountsViewModel = ViewModelProvider(requireActivity())[AccountsViewModel::class.java]
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
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {
            it.behavior.peekHeight  = R.style.AppBottomSheetDialogTheme
        }
        return inflater.inflate(R.layout.fragment_bottom_sheet_accounts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        type = arguments?.getString("type")!!


        accountsRecyclerView = view.findViewById(R.id.accountsRecyclerView)
        accountsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        accountsRecyclerView.setHasFixedSize(true)

        accountsArrayList = arrayListOf()

        accountsAdapter = AccountsAdapter(accountsArrayList)
        accountsAdapter.setOnItemClickListener(this)

        val loc = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)


        accountsAdapter.setSharedPreferencesLocale(loc)

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
            dismiss()
        }

        view.findViewById<SearchView>(R.id.accountsSearch).setOnQueryTextListener(object: SearchView.OnQueryTextListener{
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
                    accountsArrayList.add(dc.document.toObject(Accounts::class.java))
                }

                accountsAdapter.notifyDataSetChanged()
            }
    }

    override fun onItemClick(accounts: Accounts) {
             if(type == "selectMainAccount"){
            val sharedPref = requireActivity().getSharedPreferences("Settings", Context.MODE_PRIVATE)

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


        }
        else{
            accountsViewModel.selectAccounts(accounts)
        }

        dismiss()

    }

}