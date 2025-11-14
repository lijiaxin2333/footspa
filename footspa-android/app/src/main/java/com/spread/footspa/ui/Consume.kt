package com.spread.footspa.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.spread.footspa.db.MassageService
import com.spread.footspa.db.MoneyNode
import com.spread.footspa.ui.common.SelectOneOptions
import java.math.BigDecimal

private enum class ConsumptionType(val str: String) {
    None(""),
    Purchase("直接购买"),
    Deposit("储值"),
    UseCard("消卡"),
    ThirdParty("三方平台")
}

private fun typeOf(str: String): ConsumptionType = when (str) {
    ConsumptionType.Purchase.str -> ConsumptionType.Purchase
    ConsumptionType.Deposit.str -> ConsumptionType.Deposit
    ConsumptionType.UseCard.str -> ConsumptionType.UseCard
    ConsumptionType.ThirdParty.str -> ConsumptionType.ThirdParty
    else -> ConsumptionType.None
}

private data class Consumption(
    val type: ConsumptionType,
    val customer: MoneyNode,
    val money: BigDecimal,
    val service: MassageService
)


@Composable
fun ConsumeScreen(modifier: Modifier = Modifier) {
    LazyColumn(modifier = modifier) {
        item {
        }
    }
}

@Composable
fun OneConsumption(modifier: Modifier = Modifier) {
    var selectedType by remember { mutableStateOf(ConsumptionType.None) }
    Column {
        Text(text = "")
        SelectOneOptions(
            modifier = Modifier,
            options = listOf(
                ConsumptionType.Purchase.str,
                ConsumptionType.Deposit.str,
                ConsumptionType.UseCard.str,
                ConsumptionType.ThirdParty.str
            ),
            onOptionSelected = {
                selectedType = typeOf(it)
            }
        )
    }
}