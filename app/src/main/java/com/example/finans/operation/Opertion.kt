package com.example.finans.operation

import android.os.Parcel
import android.os.Parcelable

data class Operation(var category: String ?= null, var date: String ?= null, var image: String ?= null, var note: String ?= null,
                     var photo: String ?= null, var time: String ?= null, var type: String?= null, var value: Double? = null, var id: String? = null):
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
        parcel.readString(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(category)
        parcel.writeString(date)
        parcel.writeString(image)
        parcel.writeString(note)
        parcel.writeString(photo)
        parcel.writeString(time)
        parcel.writeString(type)
        parcel.writeValue(value)
        parcel.writeString(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Operation(category=$category, date=$date, image=$image, note=$note, photo=$photo, time=$time, type=$type, value=$value), id=$id)"
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
