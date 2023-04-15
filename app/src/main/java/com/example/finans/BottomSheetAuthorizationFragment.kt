package com.example.finans

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class BottomSheetAuthorizationFragment : BottomSheetDialogFragment() {

    override fun getTheme() = R.style.AppBottomSheetDialogTheme
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_authorization, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_authorization, container, false)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        view.findViewById<ImageView>(R.id.imageEmail).setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAutorizationEmailFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetAutorizationEmailFragment()
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAutorizationEmailFragment"
                )
                dismiss()
            }
        }

    }

}