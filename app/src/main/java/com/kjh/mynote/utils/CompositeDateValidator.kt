package com.kjh.mynote.utils

import android.os.Parcel
import android.os.Parcelable
import com.google.android.material.datepicker.CalendarConstraints

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 19..
 * Description: MaterialDatePicker에서 다중 Validator 사용을 위한 Custom DateValidator
 */

class CompositeDateValidator(
    private val validators: List<CalendarConstraints.DateValidator>
) : CalendarConstraints.DateValidator {

    override fun isValid(date: Long): Boolean {
        // 모든 Validator 조건을 만족해야 유효
        return validators.all { it.isValid(date) }
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeTypedList(validators)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<CompositeDateValidator> {
        override fun createFromParcel(source: Parcel): CompositeDateValidator {
            val validators = source.createTypedArrayList(CREATOR) ?: emptyList()
            return CompositeDateValidator(validators)
        }

        override fun newArray(size: Int): Array<CompositeDateValidator?> = arrayOfNulls(size)
    }
}