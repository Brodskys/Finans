package com.example.finans.accounts

import android.os.Parcel
import android.os.Parcelable

data class Accounts(
                     var nameRus: String ?= null,
                     var nameEng: String ?= null,
                     var icon: String ?= null,
                     var name: String?= null,
                     var currency: String?= null,
                     var new: String?= null,
                     var balance: Double? = null):
    Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(nameRus)
        parcel.writeString(nameEng)
        parcel.writeString(icon)
        parcel.writeString(name)
        parcel.writeString(new)
        parcel.writeString(currency)
        parcel.writeValue(balance)

    }
    override fun describeContents(): Int {
        return 0
    }


    override fun toString(): String {
        return "Operation(nameRus=$nameRus, nameEng=$nameEng, icon=$icon, icon=$new, name=$name, currency=$currency, balance=$balance)"
    }

    companion object CREATOR : Parcelable.Creator<Accounts> {
        override fun createFromParcel(parcel: Parcel): Accounts {
            return Accounts(parcel)
        }

        override fun newArray(size: Int): Array<Accounts?> {
            return arrayOfNulls(size)
        }
    }
}
