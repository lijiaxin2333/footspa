package com.spread.footspa.ui

import androidx.compose.animation.defaultDecayAnimationSpec
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.spread.footspa.db.CardType
import com.spread.footspa.db.FSDB
import com.spread.footspa.db.FSDB.Companion.queryCardWithBalance
import com.spread.footspa.db.FSDB.Companion.queryMoneyNode
import com.spread.footspa.db.MoneyNode
import com.spread.footspa.db.MoneyNodeType
import com.spread.footspa.ui.common.InvalidCardChip
import com.spread.footspa.ui.common.LegacyCardChip
import com.spread.footspa.ui.common.MoneyNodeSearchInputSimple
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Composable
fun CustomerScreen(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    val customerList = remember { mutableStateListOf<MoneyNode>() }
    LaunchedEffect(Unit) {
        FSDB.moneyNodeFlow.collect { moneyNodes ->
            val list = moneyNodes.filter {
                it.type == MoneyNodeType.Customer
            }
            customerList.clear()
            customerList.addAll(list)
        }
    }
    val filteredList = remember { mutableStateListOf<MoneyNode>() }
    var inSearch by remember { mutableStateOf(false) }
    val queryState = remember { TextFieldState() }
    var detailCustomer by remember { mutableStateOf<MoneyNode?>(null) }
    Column(modifier = modifier) {
        MoneyNodeSearchInputSimple(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(),
            textFieldState = queryState,
            label = "搜索顾客",
            onSearch = { query ->
                if (query.isBlank()) {
                    filteredList.clear()
                    inSearch = false
                } else {
                    scope.launch(Dispatchers.IO) {
                        inSearch = true
                        val res = queryMoneyNode(
                            query = query,
                            types = setOf(MoneyNodeType.Customer)
                        )
                        filteredList.clear()
                        filteredList.addAll(res)
                    }
                }
            }
        )
        LazyColumn {
            items(
                items =
                    if (inSearch) filteredList
                    else filteredList.takeIf { it.isNotEmpty() } ?: customerList
            ) { customer ->
                Column(
                    modifier = Modifier.fillMaxWidth().clickable {
                        detailCustomer = customer
                    }
                ) {
                    Text(text = "姓名: ${customer.name}")
                    Text(text = "电话: ${customer.keys?.joinToString()}")
                    HorizontalDivider()
                }
            }
        }
    }
    detailCustomer?.let { customer ->
        DetailCustomerDialog(
            customer = customer,
            onDismiss = {
                detailCustomer = null
            }
        )
    }
}

@Composable
private fun DetailCustomerDialog(
    customer: MoneyNode,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        val balanceInfo = remember { mutableStateListOf<Pair<Map.Entry<MoneyNode, BigDecimal>, CardType>>() }
        LaunchedEffect(Unit) {
            launch(Dispatchers.IO) {
                balanceInfo.clear()
                val infos = queryCardWithBalance(customer = customer)
                val l = mutableListOf<Pair<Map.Entry<MoneyNode, BigDecimal>, CardType>>()
                for (info in infos) {
                    val type = FSDB.findCardType(info.key)
                    if (type != null && type.valid) {
                        l.add(info to type)
                    }
                }
                balanceInfo.addAll(l)
            }
        }
        LazyColumn(modifier = Modifier.fillMaxWidth().height(300.dp)) {
            items(balanceInfo) { info ->
                val card = info.first.key
                val balance = info.first.value
                val type = info.second
                Column(
                    modifier = Modifier.fillMaxWidth().clickable {
                        onDismiss()
                    }
                ) {
                    Text(text = "卡名: ${card.name}")
                    Text(text = "电话: ${card.keys?.joinToString()}")
                    Text(text = "余额: $balance")
                    Text(text = "起充: ${type.price.toPlainString()}")
                    Text(text = "折扣: ${type.discount}")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (type.legacy) {
                            LegacyCardChip()
                        }
                        if (card.cardValid == false) {
                            InvalidCardChip()
                        }
                    }
                    HorizontalDivider()
                }
            }
        }
    }
}