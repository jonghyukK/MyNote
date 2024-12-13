package com.kjh.mynote.ui.features.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.model.CategoryWithPurchaseNotesCountAndTotalPrice
import com.example.domain.usecase.GetCategoriesWithPurchaseNotesStatsUseCase
import com.example.domain.usecase.GetPlaceNotesByDateRangeUseCase
import com.github.mikephil.charting.data.PieEntry
import com.kjh.mynote.R
import com.kjh.mynote.model.PlaceNoteUiModel
import com.kjh.mynote.model.toUiModel
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

private val pieColorList = listOf(
    R.color.red_500,
    R.color.red_200,
    R.color.red_100,
    R.color.black_700,
    R.color.black_500
)

sealed class HomePlaceNoteUiState {
    data object Empty: HomePlaceNoteUiState()
    data object More: HomePlaceNoteUiState()
    data class PlaceNote(val item: PlaceNoteUiModel): HomePlaceNoteUiState()
}

sealed class HomeCategoryStatsUiState {
    data class CategoryWithStatsItem(
        val categoryStatsItem: CategoryWithPurchaseNotesCountAndTotalPrice,
        val colors: Int
    ): HomeCategoryStatsUiState()

    data object MoreItem: HomeCategoryStatsUiState()
}

sealed class HomeItem {
    data class HomePlaceNoteWeekView(
        val todayDate: LocalDate = LocalDate.now(),
        val placeNotesByDate: Map<LocalDate, List<PlaceNoteUiModel>> = emptyMap(),
        val selectedDate: LocalDate = todayDate,
        val displayedPlaceNotes: List<HomePlaceNoteUiState> = emptyList(),
        val eventDays: List<LocalDate> = emptyList()
    ): HomeItem()

    data class HomePurchaseNoteCategoryPirChart(
        val categoryWithStatsItems: List<HomeCategoryStatsUiState> = emptyList(),
        val pieEntries: List<PieEntry> = emptyList(),
        val pieColors: List<Int> = emptyList(),
        val highlightedPieEntry: PieEntry? = null
    ): HomeItem()
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getPlaceNotesByDateRangeUseCase: GetPlaceNotesByDateRangeUseCase,
    private val getCategoriesWithPurchaseNotesStatsUseCase: GetCategoriesWithPurchaseNotesStatsUseCase
): ViewModel() {

    private val _uiState = MutableStateFlow(
        listOf(HomeItem.HomePlaceNoteWeekView(), HomeItem.HomePurchaseNoteCategoryPirChart()))
    val uiState = _uiState.asStateFlow()

    private val placeNotesWeekViewFlow =
        getPlaceNotesByDateRangeUseCase(
            startDate = LocalDate.now().getWeekStartAndEndDates().first.toMillis(),
            endDate = LocalDate.now().getWeekStartAndEndDates().second.toMillis()
        )
            .map { notes -> notes.toUiModel().groupBy { it.localDate } }
            .map { groupedNoteMap ->
                HomeItem.HomePlaceNoteWeekView(
                    placeNotesByDate = groupedNoteMap,
                    eventDays = groupedNoteMap.keys.toList()
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeItem.HomePlaceNoteWeekView()
            )

    private val categoryChartFlow =
        getCategoriesWithPurchaseNotesStatsUseCase()
            .map { results ->
                val filteredCategoryItems = results
                    .filter { it.purchaseNoteCount > 0 }
                    .sortedByDescending { it.purchaseNoteCount }

                val top5Items = filteredCategoryItems.take(5)
                val pieColors = makePieColors(top5Items.size)

                val categoryWithStatsItems: MutableList<HomeCategoryStatsUiState> =
                    top5Items.mapIndexed { index, categoryWithCount ->
                        HomeCategoryStatsUiState.CategoryWithStatsItem(
                            categoryStatsItem = categoryWithCount,
                            colors = pieColors[index]
                        )
                    }.toMutableList()

                if (filteredCategoryItems.size > 5) {
                    categoryWithStatsItems.add(HomeCategoryStatsUiState.MoreItem)
                }

                HomeItem.HomePurchaseNoteCategoryPirChart(
                    categoryWithStatsItems = categoryWithStatsItems,
                    pieEntries = makePieEntry(top5Items),
                    pieColors = pieColors
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = HomeItem.HomePurchaseNoteCategoryPirChart()
            )

    init {
        viewModelScope.launch {
            combine(
                placeNotesWeekViewFlow,
                categoryChartFlow
            ) { placeNotesWeekViewFlow, categoryChartFlow ->
                val currentWeekViewItem = _uiState.value.filterIsInstance<HomeItem.HomePlaceNoteWeekView>().first()
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
                if (uiList is HomeItem.HomePurchaseNoteCategoryPirChart) {
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
                if (uiList is HomeItem.HomePlaceNoteWeekView) {
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

    private fun makePieEntry(categoryWithPurchaseNotesStatsItem: List<CategoryWithPurchaseNotesCountAndTotalPrice>): List<PieEntry> {
        if (categoryWithPurchaseNotesStatsItem.isEmpty()) {
            return listOf(PieEntry(1f, "없음"))
        }

        return categoryWithPurchaseNotesStatsItem.map { data ->
            PieEntry(data.purchaseNoteCount.toFloat(), data.categoryName)
        }
    }

    private fun makePieColors(entrySize: Int): List<Int> {
        if (entrySize == 0) {
            return listOf(R.color.black_400)
        }

        return (0..< entrySize).mapIndexed { index, _ ->
            pieColorList[index]
        }
    }
}