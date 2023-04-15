package com.example.finans.settings

import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.example.finans.BottomSheetAuthorizationFragment
import com.example.finans.BottomSheetPhotoFragment
import com.example.finans.ImageViewModel
import com.example.finans.R
import com.example.finans.authorization.AuthorizationActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream

class BottomSheetSettingsFragment : BottomSheetDialogFragment() {

    private lateinit var imageViewModel: ImageViewModel
    private lateinit var sharedPreferences : SharedPreferences


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

        view.findViewById<TextView>(R.id.settingsSave).isVisible = false


        Picasso.get().load(Firebase.auth.currentUser?.photoUrl)
            .placeholder(R.drawable.person)
            .error(R.drawable.person)
            .into(view.findViewById<ImageView>(R.id.profileImage))


        imageViewModel = ViewModelProvider(requireActivity())[ImageViewModel::class.java]

        imageViewModel.cameraImageUri.observe(viewLifecycleOwner) { uri ->

            if(uri!=null) {
                val inputStream = requireContext().contentResolver.openInputStream(uri!!)
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
            if(uri!=null) {
                val inputStream = requireContext().contentResolver.openInputStream(uri!!)
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


        val user = Firebase.auth.currentUser

        if (user != null && user.isAnonymous) {
            view.findViewById<RelativeLayout>(R.id.authuser_relative).isVisible = false
            view.findViewById<LinearLayout>(R.id.authBtn).isVisible = true
        } else {
            view.findViewById<RelativeLayout>(R.id.authuser_relative).isVisible = true
            view.findViewById<LinearLayout>(R.id.authBtn).isVisible = false



            view.findViewById<TextInputEditText>(R.id.changeTextUser).setText(Firebase.auth.currentUser?.displayName)
            view.findViewById<TextInputEditText>(R.id.changeTextEmailAddress).setText(Firebase.auth.currentUser?.email)
            view.findViewById<TextInputEditText>(R.id.changeUserTextPassword).setText(" ")
        }

        view.findViewById<LinearLayout>(R.id.authBtn).setOnClickListener {

            val existingFragment =
                requireActivity().supportFragmentManager.findFragmentByTag("BottomSheetAuthorizationFragment")
            if (existingFragment == null) {
                val newFragment = BottomSheetAuthorizationFragment()
                newFragment.show(
                    requireActivity().supportFragmentManager,
                    "BottomSheetAuthorizationFragment"
                )
            }

        }


        view.findViewById<LinearLayout>(R.id.deliteUser).setOnClickListener {

                Firebase.auth.currentUser?.delete()
                    ?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("Delite", "User account deleted.")
                        }
                    }

            val db = Firebase.firestore

            val operationRef = db.collection("users").document(Firebase.auth.uid.toString()).collection("operation")
            val operationBatch = db.batch()
            operationRef.get().addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    operationBatch.delete(document.reference)
                }
                operationBatch.commit()
            }

            val categoryRef = db.collection("users").document(Firebase.auth.uid.toString()).collection("category")
            val categoryBatch = db.batch()
            categoryRef.get().addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    categoryBatch.delete(document.reference)
                }
                categoryBatch.commit()
            }

            val userRef = db.collection("users").document(Firebase.auth.uid.toString()).collection("user")
            val userBatch = db.batch()
            userRef.get().addOnSuccessListener { snapshot ->
                for (document in snapshot.documents) {
                    userBatch.delete(document.reference)
                }
                userBatch.commit()
            }


            try {
                val storageReference = FirebaseStorage.getInstance().getReference("images/${Firebase.auth.uid.toString()}")

                storageReference.delete().addOnSuccessListener {
                    Log.d("UserImage", "Файл успешно удален!")
                }.addOnFailureListener { e ->
                    Log.e("UserImage", "Ошибка при удалении файла: ${e.message}+jpg")
                }
            }
            catch (e:Exception){
                Log.e("Error", e.message!!)
            }

            sharedPreferences = activity?.getSharedPreferences("Settings", Context.MODE_PRIVATE)!!

            val editor = sharedPreferences.edit()
            editor?.remove("Pincode")
            editor?.apply()

            val editor2 = sharedPreferences.edit()
            editor2?.remove("currency")
            editor2?.apply()

            val editor3 = sharedPreferences.edit()
            editor3?.remove("modeSwitch")
            editor3?.apply()

            Firebase.auth.signOut()
            startActivity(Intent(requireActivity(), AuthorizationActivity::class.java))
            requireActivity().finish()

        }


        view.findViewById<TextView>(R.id.settingsExit).setOnClickListener {
            dismiss()
        }

        view.findViewById<TextView>(R.id.settingsSave).setOnClickListener {


            dismiss()

        }

    }


}