package com.example.finans.plans.paymentPlanning

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class PaymentPlanning(
    var value: Double? = null,
    var category: String? = null,
    var id: String? = null,
    var idNotification: String? = null,
    var currency: String? = null,
    var name: String? = null,
    var status: String? = null,
    var timestamp: Timestamp? = null,

    ) :
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        Timestamp(parcel.readLong(), parcel.readLong().toInt()),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(value)
        parcel.writeString(category)
        parcel.writeString(id)
        parcel.writeString(idNotification)
        parcel.writeString(name)
        parcel.writeString(status)
        parcel.writeString(currency)
        parcel.writeString(name)
        parcel.writeLong(timestamp?.seconds ?: 0)
        parcel.writeLong((timestamp?.nanoseconds ?: 0).toLong())
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PaymentPlanning> {
        override fun createFromParcel(parcel: Parcel): PaymentPlanning {
            return PaymentPlanning(parcel)
        }

        override fun newArray(size: Int): Array<PaymentPlanning?> {
            return arrayOfNulls(size)
        }
    }
}
