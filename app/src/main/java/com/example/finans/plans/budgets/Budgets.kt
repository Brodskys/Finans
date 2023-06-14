package com.example.finans.plans.budgets

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp

data class Budgets(
    var notification80Is: String? = null,
    var notificationOverrunsIs: String? = null,
    var notification80: String? = null,
    var notificationOverruns: String? = null,
    var id: String? = null,
    var name: String? = null,
    var maxValue: Double? = null,
    var valueNow: Double? = null,
    var accounts:  ArrayList<String>? = null,
    var categories:  ArrayList<String>? = null,
    var typeRu: String? = null,
    var typeEn: String? = null,
    var timeStart: Timestamp? = null,
    var timeEnd: Timestamp? = null,
) :
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.createStringArrayList(),
        parcel.createStringArrayList(),
        parcel.readString(),
        parcel.readString(),
        Timestamp(parcel.readLong(), parcel.readLong().toInt()),
        Timestamp(parcel.readLong(), parcel.readLong().toInt())
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(notification80)
        parcel.writeString(notificationOverruns)
        parcel.writeString(notification80Is)
        parcel.writeString(notificationOverrunsIs)
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeValue(maxValue)
        parcel.writeValue(valueNow)
        parcel.writeStringList(accounts)
        parcel.writeStringList(categories)
        parcel.writeString(typeRu)
        parcel.writeString(typeEn)
        parcel.writeLong(timeStart?.seconds ?: 0)
        parcel.writeLong((timeStart?.nanoseconds ?: 0).toLong())
        parcel.writeLong(timeEnd?.seconds ?: 0)
        parcel.writeLong((timeEnd?.nanoseconds ?: 0).toLong())
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
