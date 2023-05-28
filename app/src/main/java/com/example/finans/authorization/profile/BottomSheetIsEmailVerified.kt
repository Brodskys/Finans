package com.example.finans.authorization.profile

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.finans.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetIsEmailVerified : BottomSheetDialogFragment() {
    private lateinit var sharedPreferences: SharedPreferences
    private var switchState: Boolean = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight  = resources.displayMetrics.heightPixels
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        switchState = sharedPreferences.getBoolean("modeSwitch", false)


//        return if(switchState){
//            inflater.inflate(R.layout.fragment_bottom_sheet_dark_is_email_verified, container, false)
//        } else{
//            inflater.inflate(R.layout.fragment_bottom_sheet_is_email_verified, container, false)
//        }

        return inflater.inflate(R.layout.fragment_bottom_sheet_is_email_verified, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


    }

}