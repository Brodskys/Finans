package com.example.finans.category

import android.os.Parcel
import android.os.Parcelable

data class Category(
    var nameEng: String ?= null, var name: String ?= null, var nameRus: String ?= null, var image: String? = null, var new: String? = null) :
    Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeString(nameEng)
        parcel.writeString(nameRus)
        parcel.writeString(image)
        parcel.writeString(new)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Category> {
        override fun createFromParcel(parcel: Parcel): Category {
            return Category(parcel)
        }

        override fun newArray(size: Int): Array<Category?> {
            return arrayOfNulls(size)
        }
    }
}
