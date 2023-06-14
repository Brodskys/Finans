package com.example.finans.analytics

import android.app.Activity
import androidx.appcompat.app.AppCompatActivity
import com.example.finans.R
import com.example.finans.operation.Operation
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.poi.xddf.usermodel.chart.ChartTypes
import org.apache.poi.xddf.usermodel.chart.LegendPosition
import org.apache.poi.xddf.usermodel.chart.XDDFCategoryDataSource
import org.apache.poi.xddf.usermodel.chart.XDDFDataSourcesFactory
import org.apache.poi.xddf.usermodel.chart.XDDFNumericalDataSource
import org.apache.poi.xddf.usermodel.chart.XDDFPieChartData
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Properties
import javax.activation.DataHandler
import javax.activation.FileDataSource
import javax.mail.Authenticator
import javax.mail.Message
import javax.mail.PasswordAuthentication
import javax.mail.Session
import javax.mail.Transport
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage

private lateinit var array: ArrayList<Array<String>>
private var lenght = 0

@OptIn(DelicateCoroutinesApi::class)
fun sendEmail(
    email: String?,
    templateData: Map<String, String?>,
    tableOperation: ArrayList<Operation>,
    activity: Activity
) {
    try {
        val pref = activity.getSharedPreferences("Settings", AppCompatActivity.MODE_PRIVATE)
        val locale = pref.getString("locale", "")
        val dateFormat = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())

        lenght = tableOperation.size

        array = arrayListOf()

        array.add(
            arrayOf(
                activity.getString(R.string.dateOperation),
                activity.getString(R.string.typeOperation),
                activity.getString(R.string.сategory),
                activity.getString(R.string.amount)
            )
        )

        for (operation in tableOperation) {


            val documentRef = FirebaseFirestore.getInstance()
                .document("users/${Firebase.auth.uid.toString()}${operation.category}")

            val type = if (locale == "ru") operation.typeRu else operation.typeEn



            documentRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val documentData = documentSnapshot.data

                        val categoryEn = documentData!!["nameEng"].toString()
                        val categoryRu = documentData["nameRus"].toString()

                        val category = if (locale == "ru") {
                            categoryRu
                        } else {
                            categoryEn
                        }
                        val row = arrayOf(
                            dateFormat.format(operation.timestamp!!.toDate()),
                            type!!,
                            category,
                            operation.value.toString()
                        )
                        GlobalScope.launch(Dispatchers.IO) {
                            processData(row, templateData, activity, email)
                        }


                    }
                }
                .addOnFailureListener { exception ->

                }
        }

    } catch (e: Exception) {
        e.printStackTrace()
        println("Email failed to send")
    }

}

fun processData(
    row: Array<String>,
    templateData: Map<String, String?>,
    activity: Activity,
    email: String?
) {
    try {
        var file: File?


        array.add(row)

        if (array.size == lenght+1) {
            val tableData = array.toTypedArray()

            val wb = XSSFWorkbook()
            val sheet = wb.createSheet("Report")

            val headerCellStyle = wb.createCellStyle().apply {
                setFont(wb.createFont().apply { bold = true })
            }
            val valueCellStyle = wb.createCellStyle().apply {
                setFont(wb.createFont())
            }

            var rowIndex = 0


            for ((label, value) in templateData) {
                val row = sheet.createRow(rowIndex++)
                row.createCell(0).setCellValue("$label:")
                row.createCell(1).setCellValue(value)
                row.getCell(0).cellStyle = headerCellStyle
                row.getCell(1).cellStyle = valueCellStyle
            }

            rowIndex += 2


            for (rowData in tableData) {
                val row = sheet.createRow(rowIndex++)
                for ((columnIndex, value) in rowData.withIndex()) {
                    row.createCell(columnIndex).setCellValue(value)
                }
            }

            val drawing = sheet.createDrawingPatriarch()
            val anchor = drawing.createAnchor(0, 0, 0, 0, 0, rowIndex + 2, 10, rowIndex + 12)

            val chart = drawing.createChart(anchor)
            chart.setTitleText(activity.getString(R.string.chartOperations))
            chart.titleOverlay = false

            val legend = chart.orAddLegend
            legend.position = LegendPosition.BOTTOM

            var categoryData: XDDFCategoryDataSource? = null
            var amountData: XDDFNumericalDataSource<Double>? = null

            val categoryAmountMap = mutableMapOf<String, Double>()

            for (i in 1 until tableData.size) {
                val rowData = tableData[i]
                val category = rowData[2]
                val amount = rowData[3].toDouble()

                if (categoryAmountMap.containsKey(category)) {
                    val currentAmount = categoryAmountMap[category]!!
                    categoryAmountMap[category] = currentAmount + amount
                } else {
                    categoryAmountMap[category] = amount
                }
            }

            val categories = mutableListOf<String>()
            val amounts = mutableListOf<Double>()

            for ((category, amount) in categoryAmountMap) {
                categories.add(category)
                amounts.add(amount)
            }

            categoryData = XDDFDataSourcesFactory.fromArray(categories.toTypedArray())
            amountData = XDDFDataSourcesFactory.fromArray(amounts.toTypedArray())

            val data = chart.createData(ChartTypes.PIE, null, null) as XDDFPieChartData
            val series = data.addSeries(categoryData, amountData)
            series.setShowLeaderLines(true)
            data.setVaryColors(true)

            chart.plot(data)

            val columnWidths = arrayOf(15, 15, 20, 10)

            for (columnIndex in columnWidths.indices) {
                sheet.setColumnWidth(columnIndex, columnWidths[columnIndex] * 256)
            }

            file = File(activity.filesDir, "report.xlsx")
            val fileOutputStream = FileOutputStream(file)
            wb.write(fileOutputStream)
            fileOutputStream.close()

            wb.close()


            val props = Properties()
            props["mail.smtp.host"] = "smtp.gmail.com"
            props["mail.smtp.port"] = "587"
            props["mail.smtp.auth"] = "true"
            props["mail.smtp.starttls.enable"] = "true"

            val session = Session.getInstance(props, object : Authenticator() {
                override fun getPasswordAuthentication(): PasswordAuthentication {
                    return PasswordAuthentication(
                        "finansappfordiplom@gmail.com",
                        "njpbleolrzbggwhf"
                    )
                }
            })

            val message = MimeMessage(session)
            message.setFrom(InternetAddress("finansappfordiplom@gmail.com"))
            message.setRecipients(
                Message.RecipientType.TO,
                InternetAddress.parse(email)
            )
            message.subject = activity.getString(R.string.operationsReport)
            message.setText("message")

            val fileDataSource = FileDataSource(file)
            message.dataHandler = DataHandler(fileDataSource)
            message.fileName = fileDataSource.name

            Transport.send(message)

            println("Email sent successfully")
        }
    } catch (e: Exception) {
        e.printStackTrace()
        println("Email failed to send")
    }
}
