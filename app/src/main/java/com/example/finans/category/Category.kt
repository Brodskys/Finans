package com.example.finans.category

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.firestore.DocumentReference

data class Category(var NameEng: String ?= null, var NameRus: String ?= null, var Image: String? = null) :
    Parcelable{
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(NameEng)
        parcel.writeString(NameRus)
        parcel.writeString(Image)
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
