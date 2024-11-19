package com.kjh.mynote.utils

import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker

/**
 * Created by kangjonghyuk.
 * Created On 2024. 10. 9..
 * Description:
 */

object DatePickerManager {

    /**
     * 오늘 날짜 이후 제한 DatePicker.
     *
     * @param title
     * @param selection
     * @param positiveButtonClickAction
     * @return MaterialDatePicker<Long>
     */
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

    /**
     *  최소 날짜 ~ 최대 날짜 제한 DatePicker.
     *
     * @param title
     * @param selection
     * @param minDate
     * @param maxDate
     * @param positiveButtonClickAction
     * @return MaterialDatePicker<Long>
     */
    fun build(
        title: String,
        selection: Long,
        minDate: Long,
        maxDate: Long,
        positiveButtonClickAction: (Long) -> Unit
    ): MaterialDatePicker<Long> {
        val startDateValidator = DateValidatorPointForward.from(minDate)
        val endDateValidator = DateValidatorPointBackward.before(maxDate)

        val compositeValidator = CompositeDateValidator(
            listOf(startDateValidator, endDateValidator)
        )

        val constraints = CalendarConstraints.Builder()
            .setValidator(compositeValidator)
            .build()

        return MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setSelection(selection)
            .setCalendarConstraints(constraints)
            .build().apply {
                addOnPositiveButtonClickListener(positiveButtonClickAction)
            }
    }
}