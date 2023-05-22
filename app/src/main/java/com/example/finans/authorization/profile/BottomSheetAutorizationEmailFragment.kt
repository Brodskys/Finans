package com.example.finans.authorization.profile

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
import androidx.appcompat.app.AlertDialog
import com.example.finans.R
import com.example.finans.other.isConfirmPassword
import com.example.finans.other.isEmailValid
import com.example.finans.other.isValidPassword
import com.example.finans.settings.BottomSheetSettingsFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.Locale


class BottomSheetAutorizationEmailFragment : BottomSheetDialogFragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var sharedPreferences: SharedPreferences
    private var bottomSheetSettingsFragment: BottomSheetSettingsFragment? = null


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

        view.findViewById<TextInputEditText>(R.id.authUserTextEmailAddress).addTextChangedListener(textWatcher(view.findViewById(R.id.authUserTextEmailAddress)))
        view.findViewById<TextInputEditText>(R.id.authUserTextPassword).addTextChangedListener(textWatcher(view.findViewById(R.id.authUserTextPassword)))
        view.findViewById<TextInputEditText>(R.id.authUserTextConfirmPassword).addTextChangedListener(textWatcher(view.findViewById(R.id.authUserTextConfirmPassword)))

        auth = Firebase.auth


        val textInputLayout = view.findViewById<TextInputLayout>(R.id.authUserTextPasswordTextInputLayout)
        val textInputLayout2 = view.findViewById<TextInputLayout>(R.id.authUserTextConfirmPasswordTextInputLayout)


        if(password.text.toString().isValidPassword(requireContext()) != null || password.text.toString().isConfirmPassword(requireContext()) != null) {

            textInputLayout?.setPasswordVisibilityToggleEnabled(false)
        }
        else
            textInputLayout?.setPasswordVisibilityToggleEnabled(true)


        if(confirmPassword.text.toString().isValidPassword(requireContext()) != null|| confirmPassword.text.toString().isConfirmPassword(requireContext()) != null) {

            textInputLayout2?.setPasswordVisibilityToggleEnabled(false)
        }
        else
            textInputLayout2?.setPasswordVisibilityToggleEnabled(true)


        email.error = email.text.toString().isEmailValid(requireContext())
        password.error = password.text.toString().isValidPassword(requireContext())
        confirmPassword.error = confirmPassword.text.toString().isValidPassword(requireContext())

        view.findViewById<Button>(R.id.authUser).setOnClickListener {
            if(email.text.toString().isEmailValid(requireContext()) == null && password.text.toString().isValidPassword(requireContext()) == null
                && confirmPassword.text.toString().isValidPassword(requireContext()) == null) {

                if (password.text.toString() == confirmPassword.text.toString()) {
                val credential = EmailAuthProvider.getCredential(email.text.toString(), password.text.toString())

                auth.currentUser!!.linkWithCredential(credential)
                    .addOnCompleteListener(requireActivity()) { task ->
                        if (task.isSuccessful) {
                            val user = Firebase.auth.currentUser

                            val languageCode = Locale.getDefault().language
                            auth.setLanguageCode(languageCode)

                            user!!.sendEmailVerification()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Log.d("Email", "Email sent.")
                                    }
                                }
                            bottomSheetSettingsFragment?.dismiss()
                            dismiss()
                        } else {
                            val exception = task.exception
                            if (exception is FirebaseAuthUserCollisionException) {

                                val builder = AlertDialog.Builder(view.context)

                                builder.setTitle(R.string.error)

                                builder.setMessage(R.string.userAlreadyRegistered)

                                builder.setNegativeButton(
                                    "Ok") { dialog, id ->
                                }
                                builder.show()

                                email.text.clear()
                                password.text.clear()
                                confirmPassword.text.clear()

                            }
                        }
                    }
                }
            else {
                    textInputLayout?.setPasswordVisibilityToggleEnabled(false)
                    textInputLayout2?.setPasswordVisibilityToggleEnabled(false)

                password.error = email.text.toString().isConfirmPassword(requireContext())
                confirmPassword.error = email.text.toString().isConfirmPassword(requireContext())
            }
        }
        }
    }

    fun setBottomSheetSettingsFragment(fragment: BottomSheetSettingsFragment) {
        bottomSheetSettingsFragment = fragment
    }

    private fun textWatcher(editText: TextInputEditText): TextWatcher = object : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {

            val textInputLayout = editText.parent.parent as? TextInputLayout

            if (editText.inputType and InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS == InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS) {
                editText.error = editText.text.toString().isEmailValid(requireContext())
            } else
                if (editText.inputType and InputType.TYPE_TEXT_VARIATION_PASSWORD == InputType.TYPE_TEXT_VARIATION_PASSWORD) {

                    if(editText.text.toString().isValidPassword(requireContext()) != null) {

                        textInputLayout?.setPasswordVisibilityToggleEnabled(false)
                    }
                    else
                        textInputLayout?.setPasswordVisibilityToggleEnabled(true)

                    editText.error = editText.text.toString().isValidPassword(requireContext())

                }
        }
    }

}