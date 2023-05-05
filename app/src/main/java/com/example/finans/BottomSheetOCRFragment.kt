package com.example.finans

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.squareup.picasso.Picasso
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class BottomSheetOCRFragment : BottomSheetDialogFragment() {
    private var imageFile: File? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.fragment_bottom_sheet_ocr, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        ActivityCompat.requestPermissions(requireActivity(), arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
        ), 110)

       view.findViewById<Button>(R.id.btnTakePhoto).setOnClickListener {
         //   EasyImage.openCameraForImage(requireActivity(), 0)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        EasyImage.handleActivityResult(requestCode, resultCode, data, requireActivity(), object : DefaultCallback() {
//            override fun onImagesPicked(imageFiles: MutableList<File>, source: EasyImage.ImageSource?, type: Int) {
//                CropImage.activity(Uri.fromFile(imageFiles[0])).start(requireActivity())
//            }
//        })
//        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
//            if (resultCode == Activity.RESULT_OK) {
//                val result = CropImage.getActivityResult(data)
//                imageFile = File(result.uri.path)
//                loadImage(imageFile)
//                ConvertTask().execute(imageFile)
//            }
//        }
    }

    private fun loadImage(imageFile: File?) {

        if (imageFile != null) {
            Picasso.get().load(imageFile).into(view?.findViewById<ImageView>(R.id.ivOCR))
        }
    }

  //  private inner class ConvertTask : AsyncTask<File, Void, String>() {
//        var tesseract = TessBaseAPI()
//
//        override fun onPreExecute() {
//            super.onPreExecute()
//            val datapath = "$filesDir/tesseract/";
//            checkFile(
//                requireContext(),
//                datapath.toString(),
//                File(datapath + "tessdata/")
//            )
//            tesseract.init(datapath, "rus")
//            tvResult.visibility = View.GONE
//            progressBar.visibility = View.VISIBLE
//        }
//
//        override fun doInBackground(vararg files: File): String {
//            val options = BitmapFactory.Options()
//            options.inSampleSize =
//                4 // 1 - means max size. 4 - means maxsize/4 size. Don't use value <4, because you need more memory in the heap to store your data.
//            val bitmap = BitmapFactory.decodeFile(imageFile?.path, options)
//            tesseract.setImage(bitmap)
//            val result = tesseract.utF8Text
//            tesseract.end()
//            return result
//        }
//
//        override fun onPostExecute(result: String) {
//            super.onPostExecute(result)
//            tvResult.text = result
//            tvResult.visibility = View.VISIBLE
//            progressBar.visibility = View.GONE
//        }
   // }

    fun checkFile(context: Context, datapath: String, dir: File) {
        //directory does not exist, but we can successfully create it
        if (!dir.exists() && dir.mkdirs()) {
            copyFiles(context, datapath)
        }
        //The directory exists, but there is no data file in it
        if (dir.exists()) {
            val datafilepath = "$datapath/tessdata/rus.traineddata"
            val datafile = File(datafilepath)
            if (!datafile.exists()) {
                copyFiles(context, datapath)
            }
        }
    }

    private fun copyFiles(context: Context, DATA_PATH: String) {
        try {
            val path = "tessdata"
            val fileList = context.assets.list(path)

            for (fileName in fileList!!) {

                // open file within the assets folder
                // if it is not already there copy it to the sdcard
                val pathToDataFile = "$DATA_PATH$path/$fileName"
                if (!File(pathToDataFile).exists()) {

                    val inputStream = context.assets.open("$path/$fileName")

                    val out = FileOutputStream(pathToDataFile)

                    // Transfer bytes from in to out
                    val buf = ByteArray(1024)
                    var len: Int
                    len = inputStream.read(buf)
                    while (len > 0) {
                        out.write(buf, 0, len)
                        len = inputStream.read(buf)
                    }
                    inputStream.close()
                    out.close()

                    Log.d("copyFiles", "Copied " + fileName + "to tessdata")
                }
            }
        } catch (e: IOException) {
            Log.e("copyFiles", "Unable to copy files to tessdata $e")
        }

    }


}