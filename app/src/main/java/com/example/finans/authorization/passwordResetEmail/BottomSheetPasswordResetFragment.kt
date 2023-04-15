package com.example.finans.authorization.passwordResetEmail

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.example.finans.R
import com.example.finans.dismissFragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.*


class BottomSheetPasswordResetFragment : BottomSheetDialogFragment() {

    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight  = resources.displayMetrics.heightPixels
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)


        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_password_reset, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_password_reset, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val resetPassword = view.findViewById<Button>(R.id.resetPassword_btn)
        val email = view.findViewById<EditText>(R.id.editTextTextEmailAddressReset)

        view.findViewById<TextView>(R.id.resetPasswordExit).setOnClickListener {
            dismissFragment(this)
        }

        resetPassword.setOnClickListener {

            val languageCode = Locale.getDefault().language
            Firebase.auth.setLanguageCode(languageCode)

            Firebase.auth.sendPasswordResetEmail(email.text.toString())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d("sendPasswordResetEmail", "Email sent.")
                        dismissFragment(this)
                    }
                }
        }

    }

}