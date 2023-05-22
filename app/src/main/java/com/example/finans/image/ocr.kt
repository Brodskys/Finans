package com.example.finans.image

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import com.example.finans.R
import com.google.android.play.integrity.internal.c
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.regex.Pattern


fun ocr(activity: Activity, bitmap: Bitmap): String{

    val tessDataPath = "${activity.filesDir.absolutePath}/tesseract-ocr/rus/"
    val tessDataDir = File(tessDataPath)
    if (!tessDataDir.exists()) {
        tessDataDir.mkdirs()
    }

    val tessDataSubPath = "${tessDataPath}tessdata/"
    val tessDataSubDir = File(tessDataSubPath)
    if (!tessDataSubDir.exists()) {
        tessDataSubDir.mkdirs()
    }

    val tessDataFile = File("$tessDataSubPath/rus.traineddata")
    if (!tessDataFile.exists()) {
        val inputStream = activity.resources.openRawResource(R.raw.rus)
        val outputStream = FileOutputStream(tessDataFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
    }

    val tessBaseApi = TessBaseAPI()
    tessBaseApi.init(tessDataPath, "rus")



    tessBaseApi.setImage(bitmap)

    val result = tessBaseApi.utF8Text

    tessBaseApi.end()

    return result

}

fun parsing(str: String): List<String> {

    val result = ArrayList<String>()
    var date = ""
    var time = ""
    var dateTime = ""
    var money: String? = null

    var pattern = Pattern.compile("\\b(\\d{2}[./ -]\\d{2}[./ -]\\d{2,4})\\b")
    var matcher = pattern.matcher(str)

    while (matcher.find()) {
        date = matcher.group()
    }

    result.add(date)

    pattern = Pattern.compile("(?<!\\d)(\\d{2}):(\\d{2})(?::(\\d{2}))?(?!\\d)")
    matcher = pattern.matcher(str)

    while (matcher.find()) {
        time = matcher.group()
    }

    result.add(time)

    pattern = Pattern.compile("\\b(\\d{1,2}[./ -]\\d{1,2}[./ -]\\d{2}(?:\\d{2})?)\\s+(\\d{2}:\\d{2}(?::\\d{2})?)\\b")

    matcher = pattern.matcher(str)

    while (matcher.find()) {
        dateTime = matcher.group(1) + " " + matcher.group(2)
    }

    result.add(dateTime)


    pattern = Pattern.compile("(?i)ИТОГ\\s*\\*(\\d{3}-\\d{2})")

    matcher = pattern.matcher(str)

    while (matcher.find()) {
        money = matcher.group(1)
    }

    if(money == null) {

        pattern = Pattern.compile("(?i)ИТОГ\\s*::\\.—([0-9\\.]+)")

        matcher = pattern.matcher(str)

        while (matcher.find()) {
            money = matcher.group(1)
        }

        if(money == null) {

            pattern = Pattern.compile("(?i)ИТОГО\\s+([0-9\\.]+)")

            matcher = pattern.matcher(str)

            while (matcher.find()) {
                money = matcher.group(1)
            }
            if(money == null) {

                pattern = Pattern.compile("(?i)итог\\s*=\\s*([0-9\\s']+\\s*‚\\s*[0-9]+)")

                matcher = pattern.matcher(str)

                while (matcher.find()) {
                    money = matcher.group(1)
                }

                if(money == null) {

                    pattern = Pattern.compile("(?i)итог\\s*=\\s*(\\d+[.,]\\d{2})")

                    matcher = pattern.matcher(str)

                    while (matcher.find()) {
                        money = matcher.group(1)
                    }

                    if(money == null) {

                        pattern = Pattern.compile("(?i)игог\\s+([0-9]+\\.?[0-9]*)")

                        matcher = pattern.matcher(str)

                        while (matcher.find()) {
                            money = matcher.group(1)
                        }

                    }

                }
            }

        }
    }

    result.add(money ?: "")

    return result
}
