package com.example.finans.image

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraCharacteristics
import android.net.Uri
import android.os.Bundle
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.finans.operation.BottomSheetNewOperationFragment
import com.example.finans.R
import com.example.finans.settings.BottomSheetSettingsFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream


class BottomSheetPhotoFragment : BottomSheetDialogFragment() {

    override fun getTheme() = R.style.AppBottomSheetDialogTheme
    private val CAMERA_PERMISSION_CODE = 101
    private val REQUEST_IMAGE_CAPTURE = 1
    val REQUEST_PICK_PHOTO = 2
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var imageViewModel: ImageViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        imageViewModel = ViewModelProvider(requireActivity())[ImageViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        val switchState = sharedPreferences.getBoolean("modeSwitch", false)

        return if(switchState){
            inflater.inflate(R.layout.fragment_bottom_sheet_dark_photo, container, false)
        } else{
            inflater.inflate(R.layout.fragment_bottom_sheet_photo, container, false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        view.findViewById<LinearLayout>(R.id.relationphoto).setOnClickListener {

            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.CAMERA), CAMERA_PERMISSION_CODE)
            } else {
                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                takePictureIntent.putExtra("android.intent.extras.CAMERA_FACING", CameraCharacteristics.LENS_FACING_FRONT)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }

        view.findViewById<LinearLayout>(R.id.relationgallery).setOnClickListener {
            val pickPhoto = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhoto, REQUEST_PICK_PHOTO)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        val type = arguments?.getString("typePhoto")


        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_PICK_PHOTO -> {
                    val imageUri = data?.data

                    val inputStream: InputStream? = requireActivity().contentResolver.openInputStream(imageUri!!)
                    val bitmap: Bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    imageViewModel.setGalleryImageUri(imageUri, bitmap, type)

                    dismiss()
                }
                REQUEST_IMAGE_CAPTURE  -> {
                   val imageBitmap = data?.extras?.get("data") as? Bitmap

                    val file = File.createTempFile("image", ".jpg", context?.cacheDir)
                    val outputStream = FileOutputStream(file)
                    imageBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                    outputStream.flush()
                    outputStream.close()
                    val imageUri = Uri.fromFile(file)

                    imageViewModel.setCameraImageUri(imageUri, imageBitmap!!, type)

                    dismiss()
                }
            }
        }
    }

    companion object {
        fun newInstance(
            string: String
        ): BottomSheetPhotoFragment {
            val args = Bundle()
            args.putString("typePhoto", string)

            val fragment = BottomSheetPhotoFragment()
            fragment.arguments = args

            return fragment
        }
    }

}