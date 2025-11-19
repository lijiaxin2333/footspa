package com.spread.footspa.ui.statistics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.spread.footspa.common.isSameDay
import com.spread.footspa.db.Bill
import com.spread.footspa.db.BillType
import com.spread.footspa.db.FSDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal

@Composable
fun IncomeScreen(modifier: Modifier = Modifier) {
    val calendarState = rememberCalendarState()
    val day = calendarState.day
    val month = calendarState.month
    val year = calendarState.year
    val timeInMillis = calendarState.timeInMillis
    val todayBills = remember { mutableStateListOf<Bill>() }
    val bills by FSDB.billFlow.collectAsState()
    LaunchedEffect(timeInMillis) {
        val thisDay = withContext(Dispatchers.IO) {
            bills.filter {
                isSameDay(it.date, timeInMillis)
            }
        }
        withContext(Dispatchers.Main) {
            todayBills.clear()
            todayBills.addAll(thisDay)
        }
        withContext(Dispatchers.IO) {
            var totalIncome = BigDecimal.ZERO
            var totalExpense = BigDecimal.ZERO
            val public = FSDB.getPublic()
            val outside = FSDB.getOutside()
            for (bill in todayBills) {
                if (bill.valid) {
                    // 总收入/支出
                    if (bill.toId == public.id) {
                        totalIncome += bill.money
                    } else if (bill.fromId == public.id) {
                        totalExpense += bill.money
                    }
                }
                when (bill.type) {
                    BillType.Purchase -> {

                    }

                    BillType.Deposit -> {

                    }

                    BillType.DepositCard -> {

                    }

                    BillType.UseCard -> {

                    }

                    BillType.ThirdPartyDisplay -> {

                    }

                    BillType.ThirdPartyReal -> {

                    }

                    else -> {}
                }
            }
        }
    }
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        DaySelector(modifier = Modifier.wrapContentSize(), calendarState = calendarState)

    }

}