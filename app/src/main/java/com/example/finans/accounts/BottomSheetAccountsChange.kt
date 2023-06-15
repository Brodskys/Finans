package com.example.finans.accounts

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.finans.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetAccountsChange : BottomSheetDialogFragment() {
    private lateinit var sharedPreferences: SharedPreferences

    override fun getTheme() = R.style.AppBottomSheetDialogTheme

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_accounts_change, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_accounts_change, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<LinearLayout>(R.id.accountSetupLinearLayout).setOnClickListener{
            val newFragment = BottomSheetAccount()  //.newInstance(accounts)
            newFragment.setTargetFragment(this@BottomSheetAccountsChange, 0)

            newFragment.show(
                requireActivity().supportFragmentManager,
                "BottomSheetAccount"
            )
            dismiss()
        }

        view.findViewById<LinearLayout>(R.id.SelectMainAccountLinearLayout).setOnClickListener{
            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAccounts")
            if (existingFragment == null) {
                val newFragment = BottomSheetAccounts.newInstance("selectMainAccount")
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAccounts"
                )
            }
            dismiss()
        }
    }

}