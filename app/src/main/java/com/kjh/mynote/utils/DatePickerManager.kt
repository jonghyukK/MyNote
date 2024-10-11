package com.kjh.mynote.utils

import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 9..
 * Description:
 */

object DatePickerManager {

    fun build(
        title: String,
        selection: Long,
        positiveButtonClickAction: (Long) -> Unit
    ): MaterialDatePicker<Long> {
        val constraintsBuilder = CalendarConstraints.Builder()
                .setValidator(DateValidatorPointBackward.now())

        return MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(selection)
            .setCalendarConstraints(constraintsBuilder.build())
            .build().apply {
                addOnPositiveButtonClickListener(positiveButtonClickAction)
            }
    }
}