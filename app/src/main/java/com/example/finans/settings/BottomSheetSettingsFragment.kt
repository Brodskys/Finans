package com.example.finans.settings

import android.annotation.SuppressLint
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.preference.PreferenceManager
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.finans.R
import com.example.finans.authorization.AuthorizationActivity
import com.example.finans.authorization.authWithFacebook.AuthorizationPresenterFacebook.Companion.TAG
import com.example.finans.authorization.profile.BottomSheetAutorizationEmailFragment
import com.example.finans.image.BottomSheetPhotoFragment
import com.example.finans.image.ImageViewModel
import com.example.finans.other.isValidPassword
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class BottomSheetSettingsFragment : BottomSheetDialogFragment() {

    private lateinit var imageViewModel: ImageViewModel
    private lateinit var sharedPreferences : SharedPreferences
    private lateinit var user : FirebaseUser
    private lateinit var password : TextInputEditText
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (dialog as? BottomSheetDialog)?.let {

            it.behavior.peekHeight  = R.style.AppBottomSheetDialogTheme
        }
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_settings, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_settings, container, false)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        requireActivity().recreate()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Picasso.get().load(Firebase.auth.currentUser?.photoUrl)
            .placeholder(R.drawable.person)
            .error(R.drawable.person)
            .into(view.findViewById<ImageView>(R.id.profileImage))

        user = Firebase.auth.currentUser!!
        password = view.findViewById(R.id.changeTextPassword)

        imageViewModel = ViewModelProvider(requireActivity())[ImageViewModel::class.java]

        imageViewModel.cameraImageUri.observe(viewLifecycleOwner) { uri ->

            if(uri?.uri != null) {
                val inputStream = requireContext().contentResolver.openInputStream(uri.uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val storageRef = Firebase.storage.reference
                val imagesRef =
                    storageRef.child("images/${Firebase.auth.uid.toString()}/${Firebase.auth.uid.toString()}.jpg")

                val uploadTask = imagesRef.putBytes(data)
                uploadTask.addOnSuccessListener { taskSnapshot ->

                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        val profileUpdates = userProfileChangeRequest {
                            photoUri = uri
                        }

                        Firebase.auth.currentUser!!.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(ContentValues.TAG, "User profile updated.")

                                    Picasso.get().load(Firebase.auth.currentUser?.photoUrl)
                                        .into(view.findViewById<ImageView>(R.id.profileImage))

                                    imageViewModel.clearCameraImage()
                                }
                            }
                    }

                }
                    .addOnFailureListener { exception -> }
            }
        }

        imageViewModel.galleryImageUri.observe(viewLifecycleOwner) { uri ->
            if(uri?.uri != null) {
                val inputStream = requireContext().contentResolver.openInputStream(uri.uri)
                val bitmap = BitmapFactory.decodeStream(inputStream)

                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
                val data = baos.toByteArray()

                val storageRef = Firebase.storage.reference
                val imagesRef =
                    storageRef.child("images/${Firebase.auth.uid.toString()}/${Firebase.auth.uid.toString()}.jpg")

                val uploadTask = imagesRef.putBytes(data)
                uploadTask.addOnSuccessListener { taskSnapshot ->

                    taskSnapshot.metadata?.reference?.downloadUrl?.addOnSuccessListener { uri ->
                        val profileUpdates = userProfileChangeRequest {
                            photoUri = uri
                        }

                        Firebase.auth.currentUser!!.updateProfile(profileUpdates)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d(ContentValues.TAG, "User profile updated.")

                                    Picasso.get().load(Firebase.auth.currentUser?.photoUrl)
                                        .into(view.findViewById<ImageView>(R.id.profileImage))

                                    imageViewModel.clearGalleryImage()
                                }
                            }
                    }
                }
                    .addOnFailureListener { exception -> }
            }
        }

        view.findViewById<TextView>(R.id.changeTextPasswordBtn).setOnClickListener {

            val textInputLayout = view.findViewById<TextInputLayout>(R.id.changeTextPasswordTextInputLayout)

            if(password.text.toString().isValidPassword(requireContext()) != null) {

                textInputLayout?.setPasswordVisibilityToggleEnabled(false)
            }
            else
                textInputLayout?.setPasswordVisibilityToggleEnabled(true)

            password.error = password.text.toString().isValidPassword(requireContext())

            if(password.text.toString().isValidPassword(requireContext()) == null) {

                Firebase.auth.currentUser!!.updatePassword(password.text.toString()).addOnCompleteListener { task ->
                    if (task.isSuccessful) {

                        Firebase.auth.signOut()
                        startActivity(Intent(requireContext(), AuthorizationActivity::class.java))
                        requireActivity().finish()
                    } else{
                        Toast.makeText(requireContext(),task.exception!!.message, Toast.LENGTH_LONG).show()
                    }
                }

            }

        }


        view.findViewById<ImageView>(R.id.сhangeImage).setOnClickListener{

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetPhotoFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetPhotoFragment()
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetPhotoFragment"
                )
            }

        }


        if (user.isAnonymous) {
            view.findViewById<RelativeLayout>(R.id.authuser_relative).isVisible = false
            view.findViewById<LinearLayout>(R.id.authBtn).isVisible = true
        } else {
            view.findViewById<RelativeLayout>(R.id.authuser_relative).isVisible = true
            view.findViewById<LinearLayout>(R.id.authBtn).isVisible = false



            view.findViewById<TextInputEditText>(R.id.changeTextUser).setText(Firebase.auth.currentUser?.displayName)
            view.findViewById<TextInputEditText>(R.id.changeTextEmailAddress).setText(Firebase.auth.currentUser?.email)
        }

        view.findViewById<LinearLayout>(R.id.authBtn).setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAutorizationEmailFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetAutorizationEmailFragment()
                newFragment.setBottomSheetSettingsFragment(this)
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAutorizationEmailFragment"
                )
            }

        }


        view.findViewById<LinearLayout>(R.id.deliteUser).setOnClickListener {
            sharedPreferences = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)!!

            deleteUser(sharedPreferences, requireActivity())

        }

        view.findViewById<TextView>(R.id.settingsExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextInputEditText>(R.id.changeTextUser).addTextChangedListener(userWatcher(view.findViewById(R.id.changeTextUser)))
        password.addTextChangedListener(textWatcher(password))

    }

    private fun textWatcher(editText: TextInputEditText): TextWatcher = object : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        @SuppressLint("SuspiciousIndentation")
        override fun afterTextChanged(editable: Editable) {

            val textInputLayout = editText.parent.parent as? TextInputLayout

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
    private fun userWatcher(editText: TextInputEditText): TextWatcher = object : TextWatcher {

        override fun beforeTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {

        }
        override fun onTextChanged(charSequence: CharSequence, i: Int, i1: Int, i2: Int) {}
        override fun afterTextChanged(editable: Editable) {

            val user = Firebase.auth.currentUser

            val profileUpdates = userProfileChangeRequest {
                displayName = editText.text.toString()
            }

            user!!.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User profile updated.")
                    }
                }
        }
    }


}