package com.example.finans

import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class BottomSheetAutorizationEmailFragment : BottomSheetDialogFragment() {

    override fun getTheme() = R.style.AppBottomSheetDialogTheme
    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)


        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_autorization_email, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_autorization_email, container, false)
        }

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val email = view.findViewById<EditText>(R.id.authUserTextEmailAddress)
        val password = view.findViewById<EditText>(R.id.authUserTextPassword)
        val confirmPassword = view.findViewById<EditText>(R.id.authUserTextConfirmPassword)

        auth = Firebase.auth

        view.findViewById<Button>(R.id.authUser).setOnClickListener {
            if(password.text.toString() == confirmPassword.text.toString()) {
                val credential = EmailAuthProvider.getCredential(email.text.toString(), password.text.toString())

                auth.currentUser!!.linkWithCredential(credential)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            Log.d(TAG, "linkWithCredential:success")
                            dismiss()
                        } else {
                            Log.w(TAG, "linkWithCredential:failure", task.exception)
                            Toast.makeText(requireContext(), "Authentication failed.",
                                Toast.LENGTH_SHORT).show()
                        }
                    }
            }
            else {
                Toast.makeText(requireContext(), "Passwords do not match.", Toast.LENGTH_SHORT).show()
            }
        }



    }

}