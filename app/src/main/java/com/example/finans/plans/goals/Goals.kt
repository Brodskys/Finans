package com.example.finans.plans.goals

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Goals(
    var account: String? = null,
    var currency: String? = null,
    var id: String? = null,
    var icon: String? = null,
    var name: String? = null,
    var note: String? = null,
    var accountIcon: String? = null,
    var value: Double? = null,
    var valueNow: Double? = null
) :
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(account)
        parcel.writeString(currency)
        parcel.writeString(id)
        parcel.writeString(icon)
        parcel.writeString(name)
        parcel.writeString(accountIcon)
        parcel.writeString(note)
        parcel.writeValue(value)
        parcel.writeValue(valueNow)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Goals> {
        override fun createFromParcel(parcel: Parcel): Goals {
            return Goals(parcel)
        }

        override fun newArray(size: Int): Array<Goals?> {
            return arrayOfNulls(size)
        }
    }
}
