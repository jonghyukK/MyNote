package com.kjh.mynote.ui_compose.feature.purchasenote

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.kizitonwose.calendar.compose.CalendarLayoutInfo
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.CalendarMonth
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.core.yearMonth
import com.kjh.mynote.model.CategoryUiModel
import com.kjh.mynote.model.PurchaseNoteUiModel
import com.kjh.mynote.ui_compose.theme.Black500
import com.kjh.mynote.ui_compose.theme.Black800
import com.kjh.mynote.ui_compose.theme.Black900
import com.kjh.mynote.ui_compose.theme.ColorOnPrimaryContainer
import com.kjh.mynote.ui_compose.theme.ColorPrimary
import com.kjh.mynote.ui_compose.theme.ColorPrimaryContainer
import com.kjh.mynote.ui_compose.theme.Red500
import com.kjh.mynote.utils.constants.AppConstants
import com.kjh.mynote.utils.extensions.toComma
import com.kjh.mynote.utils.extensions.toStringWithPattern
import kotlinx.coroutines.flow.filterNotNull
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Created by kangjonghyuk.
 * Created On 2025. 2. 27..
 * Description:
 */

@Composable
fun PurchaseNoteRoute(
    viewModel: PurchaseNoteViewModel = hiltViewModel(),
    navigateToPurchaseNoteStatistics: () -> Unit = {},
    navigateToPurchaseNoteDetail: (Int) -> Unit = {}
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(viewModel.effect) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is PurchaseNoteHomeSideEffect.ShowErrorToast -> {
                    Toast.makeText(context, effect.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    PurchaseNoteScreen(
        currentMonth = state.currentMonth,
        selectedDay = state.selectedDay,
        hasNoteDays = state.hasNoteDays,
        purchaseNoteItems = state.selectedDayPurchaseNotes,
        onClickStatistics = navigateToPurchaseNoteStatistics,
        onClickFilter = {
            // todo: PopUp Filter BottomSheet
        },
        onClickSearch = {
            // todo: navigateToPurchaseNoteSearch
        },
        onClickFab = {
            // todo: PopUp PurchaseNote Add Buttons..
        },
        onClickDay = { day -> viewModel.handleEvent(PurchaseNoteHomeUiEvent.UpdateSelectedDay(day)) },
        onChangedMonth = { month ->
            viewModel.handleEvent(
                PurchaseNoteHomeUiEvent.UpdateCurrentMonth(
                    month
                )
            )
        },
        onClickPurchaseNote = { purchaseNoteId -> navigateToPurchaseNoteDetail(purchaseNoteId) }
    )
}

@Composable
fun PurchaseNoteScreen(
    currentMonth: YearMonth,
    selectedDay: LocalDate,
    hasNoteDays: List<LocalDate>,
    purchaseNoteItems: List<PurchaseNoteUiModel>,
    onClickStatistics: () -> Unit,
    onClickFilter: () -> Unit,
    onClickSearch: () -> Unit,
    onClickFab: () -> Unit,
    onClickDay: (LocalDate) -> Unit,
    onChangedMonth: (YearMonth) -> Unit,
    onClickPurchaseNote: (Int) -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            PurchaseNoteTopBar(
                date = currentMonth.toStringWithPattern(AppConstants.DATE_FORMAT_YYYY_M),
                onClickStatistics = onClickStatistics,
                onClickFilter = onClickFilter,
                onClickSearch = onClickSearch
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onClickFab,
                shape = CircleShape,
                containerColor = ColorPrimary,
                contentColor = Color.White,
            ) {
                Icon(
                    modifier = Modifier.size(38.dp),
                    imageVector = Icons.Default.Add,
                    contentDescription = "PurchaseNote Add"
                )
            }
        }
    ) { innerPadding ->
        Column(
            Modifier.padding(innerPadding)
        ) {
            PurchaseNoteCalendar(
                selectedDate = selectedDay,
                hasNoteDays = hasNoteDays,
                onClickDay = onClickDay,
                onChangedMonth = onChangedMonth
            )

            PurchaseNoteList(
                purchaseNotes = purchaseNoteItems,
                onClickPurchaseNote = onClickPurchaseNote
            )
        }
    }
}

@Composable
fun PurchaseNoteTopBar(
    date: String,
    onClickStatistics: () -> Unit,
    onClickFilter: () -> Unit,
    onClickSearch: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onClickStatistics,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = "PurchaseNote Statistics"
                )
            }

            Spacer(Modifier.weight(1f))

            IconButton(
                onClick = onClickFilter,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = "Filters"
                )
            }

            IconButton(
                onClick = onClickSearch,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            }
        }

        Text(
            text = date,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = Black900,
            modifier = Modifier
                .wrapContentWidth()
                .align(Alignment.Center)
        )
    }
}


@Composable
fun PurchaseNoteCalendar(
    selectedDate: LocalDate,
    hasNoteDays: List<LocalDate>,
    onClickDay: (LocalDate) -> Unit,
    onChangedMonth: (YearMonth) -> Unit
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth }
    val daysOfWeek = remember { daysOfWeek(firstDayOfWeek = DayOfWeek.SUNDAY) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = daysOfWeek.first()
    )

    val visibleMonth = rememberFirstCompletelyVisibleMonth(state)

    LaunchedEffect(visibleMonth) {
        onChangedMonth(visibleMonth.yearMonth)
    }

    Column {
        DayOfWeekTitle(daysOfWeek = daysOfWeek)
        HorizontalCalendar(
            state = state,
            dayContent = { day ->
                Day(
                    day = day,
                    isSelected = day.date == selectedDate,
                    hasNotes = day.date in hasNoteDays,
                    onClick = onClickDay
                )
            }
        )
    }
}

@Composable
fun DayOfWeekTitle(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            val fontColor = when (dayOfWeek) {
                DayOfWeek.SUNDAY -> Red500
                else -> Black900
            }

            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Medium,
                color = fontColor,
                text = dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            )
        }
    }
}

@Composable
fun Day(
    day: CalendarDay,
    isSelected: Boolean,
    hasNotes: Boolean,
    onClick: (LocalDate) -> Unit
) {
    val today = remember { LocalDate.now() }
    val isAfterDayFromToday = remember { day.date.isAfter(today) }

    val dayFontColor = if (isSelected) {
        Color.White
    } else if (isAfterDayFromToday) {
        Black500
    } else {
        Black900
    }

    if (day.position == DayPosition.MonthDate) {
        Box(
            modifier = Modifier
                .aspectRatio(1f)
                .clip(CircleShape)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    enabled = !isAfterDayFromToday,
                    onClick = { onClick(day.date) }
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .size(30.dp)
                        .background(color = if (isSelected) ColorPrimary else Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = day.date.dayOfMonth.toString(),
                        color = dayFontColor,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(Modifier.height(6.dp))
                Box(modifier = Modifier.size(6.dp), contentAlignment = Alignment.Center) {
                    if (hasNotes) {
                        Badge(containerColor = Red500)
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseNoteList(
    purchaseNotes: List<PurchaseNoteUiModel>,
    onClickPurchaseNote: (Int) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(vertical = 20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(
            items = purchaseNotes,
            key = { purchaseNote -> purchaseNote.id }
        ) { purchaseNote ->
            PurchaseNoteItem(
                purchaseNoteItem = purchaseNote,
                onClickPurchaseNote = { onClickPurchaseNote(purchaseNote.id) }
            )
        }
    }
}

@Composable
fun PurchaseNoteItem(
    purchaseNoteItem: PurchaseNoteUiModel,
    onClickPurchaseNote: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .height(70.dp)
            .clickable(onClick = onClickPurchaseNote),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(1.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
            ) {
                Text(
                    text = purchaseNoteItem.category?.categoryName ?: "카테고리 없음",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = ColorOnPrimaryContainer,
                    lineHeight = 12.sp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(ColorPrimaryContainer)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                )

                Text(
                    text = purchaseNoteItem.purchaseName,
                    fontSize = 15.sp,
                    color = Black800
                )
            }

            Spacer(modifier = Modifier.weight(weight = 1f))

            Text(
                text = purchaseNoteItem.purchasePrice.toComma() + "원",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Black800
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PurchaseNoteScreenPreview() {
    PurchaseNoteScreen(
        currentMonth = LocalDate.now().yearMonth,
        selectedDay = LocalDate.now(),
        hasNoteDays = emptyList(),
        purchaseNoteItems = listOf(
            PurchaseNoteUiModel(
                id = 0,
                purchaseDate = 0,
                purchaseLocalDate = LocalDate.now(),
                purchasePrice = 5000,
                purchaseName = "test",
                category = CategoryUiModel(
                    categoryName = "편의점/카페/간식"
                )
            ),
            PurchaseNoteUiModel(
                id = 1,
                purchaseDate = 0,
                purchaseLocalDate = LocalDate.now(),
                purchasePrice = 5000,
                purchaseName = "test",
                category = CategoryUiModel(
                    categoryName = "편의점/카페/간식"
                )
            ),
            PurchaseNoteUiModel(
                id = 2,
                purchaseDate = 0,
                purchaseLocalDate = LocalDate.now(),
                purchasePrice = 5000,
                purchaseName = "test",
                category = CategoryUiModel(
                    categoryName = "편의점/카페/간식"
                )
            ),
        ),
        onClickStatistics = {},
        onClickFilter = {},
        onClickSearch = {},
        onClickFab = {},
        onClickDay = {},
        onChangedMonth = {},
        onClickPurchaseNote = {}
    )
}


@Composable
fun rememberFirstCompletelyVisibleMonth(state: CalendarState): CalendarMonth {
    val visibleMonth = remember(state) { mutableStateOf(state.firstVisibleMonth) }
    // Only take non-null values as null will be produced when the
    // list is mid-scroll as no index will be completely visible.
    LaunchedEffect(state) {
        snapshotFlow { state.layoutInfo.completelyVisibleMonths.firstOrNull() }
            .filterNotNull()
            .collect { month -> visibleMonth.value = month }
    }
    return visibleMonth.value
}

private val CalendarLayoutInfo.completelyVisibleMonths: List<CalendarMonth>
    get() {
        val visibleItemsInfo = this.visibleMonthsInfo.toMutableList()
        return if (visibleItemsInfo.isEmpty()) {
            emptyList()
        } else {
            val lastItem = visibleItemsInfo.last()
            val viewportSize = this.viewportEndOffset + this.viewportStartOffset
            if (lastItem.offset + lastItem.size > viewportSize) {
                visibleItemsInfo.removeAt(visibleItemsInfo.lastIndex)
            }
            val firstItem = visibleItemsInfo.firstOrNull()
            if (firstItem != null && firstItem.offset < this.viewportStartOffset) {
                visibleItemsInfo.removeAt(0)
            }
            visibleItemsInfo.map { it.month }
        }
    }