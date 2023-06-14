package com.example.finans.plans.budgets

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class lineChartBudgets(
    var value: Double? = null,
    var timestamp: Timestamp? = null,
    var operationUrl: String ?= null,
):
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(Double::class.java.classLoader) as? Double,
        Timestamp(parcel.readLong(), parcel.readLong().toInt()),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(value)
        parcel.writeLong(timestamp?.seconds ?: 0)
        parcel.writeLong((timestamp?.nanoseconds ?: 0).toLong())
        parcel.writeString(operationUrl)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Budgets> {
        override fun createFromParcel(parcel: Parcel): Budgets {
            return Budgets(parcel)
        }

        override fun newArray(size: Int): Array<Budgets?> {
            return arrayOfNulls(size)
        }
    }
}
