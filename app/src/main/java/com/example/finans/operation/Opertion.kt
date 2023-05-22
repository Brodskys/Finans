package com.example.finans.operation

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import com.google.firebase.firestore.GeoPoint

data class Operation(var category: String ?= null,
                     var id: String? = null,
                     var image: String ?= null,
                     var map: GeoPoint ?= null,
                     var note: String ?= null,
                     var photo: String ?= null,
                     var timestamp: Timestamp? = null,
                     var type: String?= null,
                     var value: Double? = null):
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readParcelable(GeoPoint::class.java.classLoader),
        parcel.readString(),
        parcel.readString(),
        Timestamp(parcel.readLong(), parcel.readLong().toInt()),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(category)
        parcel.writeString(id)
        parcel.writeString(image)
        parcel.writeString(note)
        parcel.writeString(photo)
        parcel.writeLong(timestamp?.seconds ?: 0)
        parcel.writeLong((timestamp?.nanoseconds ?: 0).toLong())
        parcel.writeString(type)
        parcel.writeValue(value)

    }
    override fun describeContents(): Int {
        return 0
    }


    override fun toString(): String {
        return "Operation(category=$category, timestamp=$timestamp, image=$image, note=$note, photo=$photo, type=$type, value=$value), id=$id)"
    }

    companion object CREATOR : Parcelable.Creator<Operation> {
        override fun createFromParcel(parcel: Parcel): Operation {
            return Operation(parcel)
        }

        override fun newArray(size: Int): Array<Operation?> {
            return arrayOfNulls(size)
        }
    }
}
