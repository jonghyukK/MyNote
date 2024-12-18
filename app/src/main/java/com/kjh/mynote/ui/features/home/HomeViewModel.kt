package com.kjh.mynote.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CategoryWithStats
import com.example.domain.usecase.GetCategoriesWithStatsByDateUseCase
import com.example.domain.usecase.GetPlaceNotesByDateRangeUseCase
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.R
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.toUiModel
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.getFirstDayOfMonth
import com.kjh.mynote.utils.extensions.getLastDayOfMonth
import com.kjh.mynote.utils.extensions.getWeekStartAndEndDates
import com.kjh.mynote.utils.extensions.toMillis
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * Created by kangjonghyuk.
 * Created On 2024. 12. 9..
 * Description:
 */

sealed class HomePlaceNoteUiState {
    data object Empty: HomePlaceNoteUiState()
    data object More: HomePlaceNoteUiState()
    data class PlaceNote(val item: PlaceNoteUiModel): HomePlaceNoteUiState()
}

sealed class HomeCategoryStatsUiState {
    data class CategoryWithStatsItem(
        val categoryWithStatsItem: CategoryWithStats,
        val colors: Int
    ): HomeCategoryStatsUiState()

    data object MoreItem: HomeCategoryStatsUiState()
}

sealed class HomeUiState {
    data class PlaceNoteWeekItem(
        val placeNotesByDate: Map<LocalDate, List<PlaceNoteUiModel>> = emptyMap(),
        val selectedDate: LocalDate = LocalDate.now(),
        val displayedPlaceNotes: List<HomePlaceNoteUiState> = emptyList(),
        val eventDays: List<LocalDate> = emptyList()
    ): HomeUiState()

    data class PurchaseNoteCategoryPieChartItem(
        val categoryWithStatsItems: List<HomeCategoryStatsUiState> = emptyList(),
        val pieEntries: List<PieEntry> = emptyList(),
        val pieColors: List<Int> = emptyList(),
        val highlightedPieEntry: PieEntry? = null
    ): HomeUiState()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPlaceNotesByDateRangeUseCase: GetPlaceNotesByDateRangeUseCase,
    private val getCategoriesWithStatsByDateUseCase: GetCategoriesWithStatsByDateUseCase
): ViewModel() {

    private val _todayDate: LocalDate = LocalDate.now()

    private val _uiState = MutableStateFlow(
        listOf(HomeUiState.PlaceNoteWeekItem(), HomeUiState.PurchaseNoteCategoryPieChartItem()))
    val uiState = _uiState.asStateFlow()

    private val placeNotesWeekViewFlow =
        getPlaceNotesByDateRangeUseCase(
            startDate = _todayDate.getWeekStartAndEndDates().first.toMillis(),
            endDate = _todayDate.getWeekStartAndEndDates().second.toMillis()
        )
            .map { notes -> notes.toUiModel().groupBy { it.localDate } }
            .map { groupedNoteMap ->
                HomeUiState.PlaceNoteWeekItem(
                    placeNotesByDate = groupedNoteMap,
                    eventDays = groupedNoteMap.keys.toList()
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeUiState.PlaceNoteWeekItem()
            )

    private val categoryChartFlow =
        getCategoriesWithStatsByDateUseCase(
            startDate = _todayDate.getFirstDayOfMonth().toMillis(),
            endDate = _todayDate.getLastDayOfMonth().toMillis()
        )
            .map { results ->
                val filteredCategoryItems = results
                    .filter { it.purchaseNoteTotalCount > 0 }
                    .sortedByDescending { it.purchaseNoteTotalCount }

                val top5Items = filteredCategoryItems.take(5)
                val pieColors = makePieColors(top5Items.size)

                val categoryWithStatsItems: MutableList<HomeCategoryStatsUiState> =
                    top5Items.mapIndexed { index, categoryWithCount ->
                        HomeCategoryStatsUiState.CategoryWithStatsItem(
                            categoryWithStatsItem = categoryWithCount,
                            colors = pieColors[index]
                        )
                    }.toMutableList()

                if (filteredCategoryItems.size > 5) {
                    categoryWithStatsItems.add(HomeCategoryStatsUiState.MoreItem)
                }

                HomeUiState.PurchaseNoteCategoryPieChartItem(
                    categoryWithStatsItems = categoryWithStatsItems,
                    pieEntries = makePieEntry(top5Items),
                    pieColors = pieColors
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeUiState.PurchaseNoteCategoryPieChartItem()
            )

    init {
        viewModelScope.launch {
            combine(
                placeNotesWeekViewFlow,
                categoryChartFlow
            ) { placeNotesWeekViewFlow, categoryChartFlow ->
                val currentWeekViewItem = _uiState.value.filterIsInstance<HomeUiState.PlaceNoteWeekItem>().first()
                val currentSelectedDate = currentWeekViewItem.selectedDate
                val placeNotesInSelectedDay = placeNotesWeekViewFlow.placeNotesByDate[currentSelectedDate] ?: emptyList()

                val homePlaceNotesWeekViewUiItem = placeNotesWeekViewFlow.copy(
                    selectedDate = currentSelectedDate,
                    displayedPlaceNotes = makeHomePlaceNoteUiState(placeNotesInSelectedDay)
                )

                listOf(homePlaceNotesWeekViewUiItem, categoryChartFlow)
            }.collectLatest {
                _uiState.value = it
            }
        }
    }

    fun setHighlightPieEntry(entry: PieEntry?) {
        _uiState.update { uiState ->
            uiState.map { uiList ->
                if (uiList is HomeUiState.PurchaseNoteCategoryPieChartItem) {
                    uiList.copy(
                        highlightedPieEntry = entry,
                    )
                } else {
                    uiList
                }
            }
        }
    }

    fun changePlaceNoteWeekDay(newDay: LocalDate) {
        _uiState.update { uiState ->
            uiState.map { uiList ->
                if (uiList is HomeUiState.PlaceNoteWeekItem) {
                    val placeNoteItemsInSelectedDay = uiList.placeNotesByDate[newDay] ?: emptyList()
                    uiList.copy(
                        selectedDate = newDay,
                        displayedPlaceNotes = makeHomePlaceNoteUiState(placeNoteItemsInSelectedDay)
                    )
                } else {
                    uiList
                }
            }
        }
    }

    private fun makeHomePlaceNoteUiState(
        placeNotes: List<PlaceNoteUiModel>
    ): List<HomePlaceNoteUiState> = when {
        placeNotes.isEmpty() -> {
            listOf(HomePlaceNoteUiState.Empty)
        }
        placeNotes.size > 3 -> {
            placeNotes
                .take(3)
                .map { HomePlaceNoteUiState.PlaceNote(it) } + listOf(HomePlaceNoteUiState.More)
        }
        else -> {
            placeNotes.map { HomePlaceNoteUiState.PlaceNote(it) }
        }
    }

    private fun makePieEntry(categoryWithPurchaseNotesStatsItem: List<CategoryWithStats>): List<PieEntry> {
        if (categoryWithPurchaseNotesStatsItem.isEmpty()) {
            return listOf(PieEntry(1f, "없음"))
        }

        return categoryWithPurchaseNotesStatsItem.map { data ->
            PieEntry(data.purchaseNoteTotalCount.toFloat(), data.categoryName)
        }
    }

    private fun makePieColors(entrySize: Int): List<Int> {
        if (entrySize == 0) {
            return listOf(R.color.black_400)
        }

        return (0..< entrySize).mapIndexed { index, _ ->
            AppConstants.chartColorList[index]
        }
    }
}