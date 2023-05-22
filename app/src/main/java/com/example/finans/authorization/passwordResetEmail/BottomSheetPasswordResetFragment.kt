package com.example.finans.authorization.passwordResetEmail

import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.finans.R
import com.example.finans.other.dismissFragment
import com.example.finans.other.isEmailValid
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
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

        email.addTextChangedListener(textWatcher(view.findViewById(R.id.editTextTextEmailAddressReset)))

        view.findViewById<TextView>(R.id.resetPasswordExit).setOnClickListener {
            dismissFragment(this)
        }

        resetPassword.setOnClickListener {

            email.error = email.text.toString().isEmailValid(requireContext())

            if (email.text.toString().isEmailValid(requireContext()) == null) {


                FirebaseAuth.getInstance().fetchSignInMethodsForEmail(email.text.toString())
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val signInMethods = task.result?.signInMethods
                            if (signInMethods.isNullOrEmpty()) {
                                val builder = AlertDialog.Builder(view.context)

                                builder.setTitle(R.string.error)

                                builder.setMessage(R.string.userNotFound)

                                builder.setNegativeButton(
                                    "Ok"
                                ) { dialog, id ->
                                }
                                builder.show()

                                email.text.clear()

                            } else {
                                val languageCode = Locale.getDefault().language
                                Firebase.auth.setLanguageCode(languageCode)

                                Firebase.auth.sendPasswordResetEmail(email.text.toString())
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d("sendPasswordResetEmail", "Email sent.")
                                            dismissFragment(this)
                                            Toast.makeText(requireContext(), getString(R.string.sendEnail), Toast.LENGTH_LONG).show()
                                        }
                                    }
                            }
                        }
                    }

            }
        }

    }

    private fun textWatcher(editText: TextInputEditText): TextWatcher = object : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {

            if (editText.inputType and InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
                editText.error = editText.text.toString().isEmailValid(requireContext())
            }
        }
    }

}