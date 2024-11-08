package com.kjh.mynote.ui.features.purchase

import androidx.lifecycle.ViewModel
import com.kizitonwose.calendar.core.yearMonth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 11. 7..
 * Description:
 */

@HiltViewModel
class PurchaseHomeViewModel @Inject constructor(): ViewModel() {

    private val _currentMonth = MutableStateFlow(LocalDate.now().yearMonth)
    val currentMonth = _currentMonth.asStateFlow()

    private val _selectedDay = MutableStateFlow(LocalDate.now())
    val selectedDay = _selectedDay.asStateFlow()

    fun setCurrentMonth(date: YearMonth) {
        _currentMonth.value = date
    }

    fun setSelectedDay(day: LocalDate) {
        _selectedDay.value = day
    }
}