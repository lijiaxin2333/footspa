package com.spread.footspa.ui.statistics

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.spread.footspa.ui.common.TextConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNot
import kotlinx.coroutines.launch
import java.util.Calendar

@Stable
class CalendarState internal constructor(
    initialCalendar: Calendar
) {
    private val _calendar = initialCalendar.clone() as Calendar

    var year by mutableIntStateOf(_calendar.get(Calendar.YEAR))
        private set

    var month by mutableIntStateOf(_calendar.get(Calendar.MONTH)) // 0-11
        private set

    var day by mutableIntStateOf(_calendar.get(Calendar.DAY_OF_MONTH))
        private set

    var timeInMillis by mutableLongStateOf(_calendar.timeInMillis)
        private set

    fun getCalendar(): Calendar {
        return _calendar.clone() as Calendar
    }

    private fun syncState() {
        year = _calendar.get(Calendar.YEAR)
        month = _calendar.get(Calendar.MONTH)
        day = _calendar.get(Calendar.DAY_OF_MONTH)
        timeInMillis = _calendar.timeInMillis
    }

    fun addDays(days: Int) {
        _calendar.add(Calendar.DAY_OF_MONTH, days)
        syncState()
    }

    fun addMonths(months: Int) {
        _calendar.add(Calendar.MONTH, months)
        syncState()
    }

    fun addYears(years: Int) {
        _calendar.add(Calendar.YEAR, years)
        syncState()
    }

    fun setDate(year: Int, month: Int, day: Int) {
        _calendar.set(year, month, day)
        syncState()
    }

    fun setCalendar(calendar: Calendar) {
        _calendar.timeInMillis = calendar.timeInMillis
        syncState()
    }

    fun reset() {
        _calendar.clear()
        _calendar.timeInMillis = System.currentTimeMillis()
        syncState()
    }
}

@Composable
fun rememberCalendarState(
    initialCalendar: Calendar = Calendar.getInstance()
): CalendarState {
    return remember {
        CalendarState(initialCalendar)
    }
}

@Composable
fun DaySelector(
    modifier: Modifier = Modifier,
    calendarState: CalendarState,
) {
    var showPicker by remember { mutableStateOf(false) }
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {

        IconButton(onClick = {
            calendarState.addDays(-1)
        }) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Day")
        }

        Text(
            text = "${calendarState.year}年${calendarState.month + 1}月${calendarState.day}日",
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .clickable {
                    showPicker = true
                },
            fontSize = TextConstants.FONT_SIZE_H1,
            color = MaterialTheme.colorScheme.onSurface,
        )

        IconButton(onClick = {
            calendarState.addDays(1)
        }) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Day")
        }
    }
    if (showPicker) {
        YearMonthDayPickerDialog(
            onDismissRequest = { showPicker = false },
            calendarState = calendarState
        )
    }
}

@Composable
fun YearMonthDayPickerDialog(
    modifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    calendarState: CalendarState,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                TextButton(
                    onClick = {
                        calendarState.reset()
                        calendarState.addDays(-2)
                        onDismissRequest()
                    }
                ) {
                    Text(text = "前天")
                }
                TextButton(
                    onClick = {
                        calendarState.reset()
                        calendarState.addDays(-1)
                        onDismissRequest()
                    }
                ) {
                    Text(text = "昨天")
                }
                TextButton(
                    onClick = {
                        calendarState.reset()
                        onDismissRequest()
                    }
                ) {
                    Text(text = "今天")
                }
            }
        },
        text = {
            calendarState.getCalendar().run {
                YearMonthDayPicker(
                    year = get(Calendar.YEAR),
                    month = get(Calendar.MONTH),
                    day = get(Calendar.DAY_OF_MONTH),
                    onConfirm = { y, m, d ->
                        calendarState.setDate(y, m, d)
                        onDismissRequest()
                    }
                )
            }
        },
        confirmButton = {}
    )
}


@Composable
fun YearMonthDayPicker(
    year: Int,
    month: Int,
    day: Int,
    onConfirm: (Int, Int, Int) -> Unit
) {
    val years = (2000..2100).toList()
    val months = (1..12).toList()

    var selectedYear by remember { mutableIntStateOf(year) }
    var selectedMonth by remember { mutableIntStateOf(month) }
    var selectedDay by remember { mutableIntStateOf(day) }

    // 动态根据年/月计算对应天数
    val days = remember(selectedYear, selectedMonth) {
        mutableStateListOf<Int>().apply {
            addAll(1..getDaysInMonth(selectedYear, selectedMonth))
        }
    }

    val yearState = rememberLazyListState(initialFirstVisibleItemIndex = years.indexOf(year))
    val monthState = rememberLazyListState(initialFirstVisibleItemIndex = months.indexOf(month + 1))
    val dayState = rememberLazyListState(initialFirstVisibleItemIndex = days.indexOf(day))

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(300.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            PickerList(
                items = years,
                state = yearState,
                label = "年",
                onSelectedItemChanged = { selectedYear = it }
            )

            PickerList(
                items = months,
                state = monthState,
                label = "月",
                onSelectedItemChanged = { selectedMonth = it - 1 }
            )

            PickerList(
                items = days,
                state = dayState,
                label = "日",
                onSelectedItemChanged = { selectedDay = it }
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(onClick = {
            onConfirm(selectedYear, selectedMonth, selectedDay)
        }) {
            Text("确定")
        }
    }
}

@Composable
fun PickerList(
    items: List<Int>,
    state: LazyListState,
    label: String,
    onSelectedItemChanged: (Int) -> Unit
) {
    val itemHeight = 40.dp
    val visibleItemsCount = 5
    val paddingVertical = itemHeight * (visibleItemsCount / 2)
    val scope = rememberCoroutineScope()

    var currentSelected by remember { mutableIntStateOf(-1) }
    var isInScrollAnim by remember { mutableStateOf(false) }

    val select: CoroutineScope.(Boolean, Int) -> Unit = remember(items, currentSelected) {
        { scrollAnim,
          index ->
            items.getOrNull(index)?.let { target ->
                if (target != currentSelected) {
                    currentSelected = target
                    onSelectedItemChanged(target)
                }
                if (scrollAnim) {
                    launch {
                        isInScrollAnim = true
                        state.animateScrollToItem(index)
                        isInScrollAnim = false
                    }
                }
            }
        }
    }

    LaunchedEffect(state, items, currentSelected) {
        launch {
            snapshotFlow { state.firstVisibleItemIndex }
                .filterNot { isInScrollAnim }
                .collect {
                    val targetIndex = state.firstVisibleItemIndex
                    select(false, targetIndex)
                }
        }
        launch {
            snapshotFlow { state.isScrollInProgress }
                .distinctUntilChanged()
                .filterNot { it || isInScrollAnim }
                .collect {
                    val index = items.indexOf(currentSelected)
                    state.animateScrollToItem(index)
                }
        }
    }

    Box(
        modifier = Modifier
            .height(itemHeight * visibleItemsCount)
            .width(100.dp)
    ) {
        LazyColumn(
            state = state,
            // always select middle item
            contentPadding = PaddingValues(vertical = paddingVertical),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            itemsIndexed(items) { index, item ->
                Text(
                    text = "$item $label",
                    maxLines = 1,
                    modifier = Modifier
                        .height(itemHeight)
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.CenterVertically)
                        .selectable(
                            selected = item == currentSelected,
                            onClick = {
                                scope.select(true, index)
                            }
                        ),
                    textAlign = TextAlign.Center,
                    textDecoration = if (item == currentSelected) TextDecoration.Underline else TextDecoration.None
                )
            }
        }

    }
}

private fun getDaysInMonth(year: Int, month: Int): Int {
    val calendar = Calendar.getInstance().apply {
        clear()
        set(Calendar.YEAR, year)
        set(Calendar.MONTH, month)
        set(Calendar.DAY_OF_MONTH, 1)
    }
    return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
}
