package com.spread.footspa.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.window.DialogProperties
import com.spread.footspa.db.FSDB
import com.spread.footspa.db.MassageService
import com.spread.footspa.db.MoneyNode
import com.spread.footspa.db.MoneyNodeType
import com.spread.footspa.db.buildMoneyNode
import com.spread.footspa.db.queryMassageService
import com.spread.footspa.db.queryMoneyNode
import com.spread.footspa.ui.common.MoneyNodeSearchInputSimple
import com.spread.footspa.ui.common.PhoneNumberInput
import com.spread.footspa.ui.common.SelectOneOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

private class Consumption(
    type: ConsumptionType = ConsumptionType.None,
    customer: MoneyNode? = null,
    card: MoneyNode? = null,
    money: BigDecimal = BigDecimal.ZERO,
    service: MassageService? = null,
    servant: MoneyNode? = null
) {
    var type by mutableStateOf(type)
    var customer by mutableStateOf(customer)
    var card by mutableStateOf(card)
    var money by mutableStateOf(money)
    var service by mutableStateOf(service)
    var servant by mutableStateOf(servant)
    val addMap = mutableStateMapOf<String, Boolean>()

    fun needAdd(key: String) = addMap[key] ?: false

    fun markNeedAdd(key: String, value: Boolean) {
        addMap[key] = value
    }
}


@Composable
fun ConsumeScreen(modifier: Modifier = Modifier) {
    val consumptions = remember { mutableStateListOf(Consumption()) }
    var customerDialogIndex by remember { mutableIntStateOf(-1) }
    var cardDialogIndex by remember { mutableIntStateOf(-1) }
    var serviceDialogIndex by remember { mutableIntStateOf(-1) }
    var servantDialogIndex by remember { mutableIntStateOf(-1) }
    LazyColumn(modifier = modifier) {
        itemsIndexed(consumptions) { index, consumption ->
            OneConsumption(
                consumption = consumption,
                onClickCustomer = {
                    customerDialogIndex = index
                }
            )
            HorizontalDivider()
        }
        item {
            Box(modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .clickable {
                            consumptions.add(Consumption())
                        },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        modifier = Modifier.padding(end = 5.dp),
                        text = "新增一笔消费"
                    )
                    Icon(imageVector = Icons.Default.Add, contentDescription = "添加")
                }
            }
        }
    }
    // TODO: must be outside of Dialog, otherwise UI not refresh, why???
    val customerQueryState = remember { TextFieldState() }
    val cardQueryState = remember { TextFieldState() }
    val serviceQueryState = remember { TextFieldState() }
    val servantQueryState = remember { TextFieldState() }
    if (customerDialogIndex in consumptions.indices) {
        QueryMoneyNodeDialog(
            onDismissRequest = {
                customerDialogIndex = -1
            },
            consumptions = consumptions,
            index = customerDialogIndex,
            queryType = MoneyNodeType.Customer,
            queryState = customerQueryState
        )
    }
    if (cardDialogIndex in consumptions.indices) {
        QueryMoneyNodeDialog(
            onDismissRequest = {
                cardDialogIndex = -1
            },
            consumptions = consumptions,
            index = cardDialogIndex,
            queryType = MoneyNodeType.Card,
            queryState = cardQueryState
        )
    }
    if (serviceDialogIndex in consumptions.indices) {
        QueryServiceDialog(
            onDismissRequest = {
                serviceDialogIndex = -1
            },
            consumptions = consumptions,
            index = serviceDialogIndex,
            queryState = serviceQueryState
        )
    }
    if (servantDialogIndex in consumptions.indices) {
        QueryMoneyNodeDialog(
            onDismissRequest = {
                servantDialogIndex = -1
            },
            consumptions = consumptions,
            index = servantDialogIndex,
            queryType = MoneyNodeType.Employee,
            queryState = servantQueryState
        )
    }
}

@Composable
private fun OneConsumption(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    onClickCustomer: () -> Unit
) {
    Column(modifier = modifier) {
        Text(text = "消费类型")
        SelectOneOptions(
            modifier = Modifier,
            options = listOf(
                ConsumptionType.Purchase.str,
                ConsumptionType.Deposit.str,
                ConsumptionType.UseCard.str,
                ConsumptionType.ThirdParty.str
            ),
            onOptionSelected = {
                consumption.type = typeOf(it)
            }
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClickCustomer
        ) {
            Text(text = "确定顾客信息")
        }
        if (consumption.needAdd(MoneyNodeType.Customer.str)) {
            var nameInput by remember { mutableStateOf("") }
            val phoneNumbers = remember { mutableStateListOf("") }
            OutlinedTextField(
                value = nameInput,
                onValueChange = {
                    nameInput = it
                },
                label = { Text("名字") },
                singleLine = true
            )
            Text(text = "联系电话:")
            PhoneNumberInput(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(),
                phoneNumbers = phoneNumbers,
                onPhoneNumberChange = { newNum, index ->
                    phoneNumbers[index] = newNum
                    if (newNum.isEmpty() && phoneNumbers.size > 1) {
                        phoneNumbers.removeAt(index)
                    }
                },
                onClickNewPhoneNumber = {
                    phoneNumbers.add("")
                }
            )
            Button(onClick = {
                val n = nameInput.takeIf { it.isNotBlank() } ?: return@Button
                val pn = phoneNumbers.takeIf { it.isNotEmpty() } ?: return@Button
                consumption.customer = buildMoneyNode {
                    name = n
                    type = MoneyNodeType.Customer
                    keys = pn
                }
                consumption.markNeedAdd(MoneyNodeType.Customer.str, false)
            }) { Text(text = "确定添加") }
        } else {
            val newCustomer = FSDB.moneyNodeFlow.value.find {
                consumption.customer == it
            } == null
            consumption.customer?.let {
                Text("姓名: ${it.name}" + if (newCustomer) "(新)" else "")
                Text("电话: ${it.keys?.joinToString()}")
            }
        }
    }
}

@Composable
private fun QueryMoneyNodeDialog(
    onDismissRequest: () -> Unit,
    consumptions: List<Consumption>,
    index: Int,
    queryType: MoneyNodeType,
    queryState: TextFieldState
) {
    val scope = rememberCoroutineScope()
    val queryResult = remember { mutableStateListOf<MoneyNode>() }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoneyNodeSearchInputSimple(
                    modifier = Modifier.weight(1f),
                    label = "搜索",
                    textFieldState = queryState,
                    onSearch = { query ->
                        if (query.isBlank()) {
                            queryResult.clear()
                        }
                        scope.launch(Dispatchers.IO) {
                            val res = queryMoneyNode(
                                query = query,
                                nodes = FSDB.moneyNodeFlow.value,
                                types = setOf(queryType)
                            )
                            withContext(Dispatchers.Main) {
                                queryResult.clear()
                                queryResult.addAll(res)
                            }
                        }
                    }
                )
                OutlinedIconButton(
                    modifier = Modifier.wrapContentSize(),
                    onClick = {
                        consumptions[index].markNeedAdd(queryType.str, true)
                        onDismissRequest()
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Icon(
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .align(Alignment.Center),
                            imageVector = Icons.Default.Add,
                            contentDescription = "add"
                        )
                    }
                }
            }
            LazyColumn {
                items(queryResult) { customer ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable {
                                consumptions[index].run {
                                    this.customer = customer
                                    markNeedAdd(queryType.str, false)
                                }
                                onDismissRequest()
                            },
                        text = customer.name + " " + customer.keys?.joinToString(),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun QueryServiceDialog(
    onDismissRequest: () -> Unit,
    consumptions: List<Consumption>,
    index: Int,
    queryState: TextFieldState
) {
    val scope = rememberCoroutineScope()
    val queryResult = remember { mutableStateListOf<MassageService>() }
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Column {
            consumptions[index].service?.let {
                Text("选择：${it.name}")
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MoneyNodeSearchInputSimple(
                    modifier = Modifier.weight(1f),
                    label = "搜索",
                    textFieldState = queryState,
                    onSearch = { query ->
                        if (query.isBlank()) {
                            queryResult.clear()
                        }
                        scope.launch(Dispatchers.IO) {
                            val res = queryMassageService(
                                query = query,
                                services = FSDB.massageServiceFlow.value
                            )
                            withContext(Dispatchers.Main) {
                                queryResult.clear()
                                queryResult.addAll(res)
                            }
                        }
                    }
                )
                OutlinedIconButton(
                    modifier = Modifier.wrapContentSize(),
                    onClick = {
                        consumptions[index].markNeedAdd(MassageService::class.java.name, true)
                        onDismissRequest()
                    }
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Icon(
                            modifier = Modifier
                                .padding(start = 5.dp)
                                .align(Alignment.Center),
                            imageVector = Icons.Default.Add,
                            contentDescription = "add"
                        )
                    }
                }
            }
            LazyColumn {
                items(queryResult) { service ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable {
                                consumptions[index].run {
                                    this.service = service
                                    markNeedAdd(MassageService::class.java.name, false)
                                }
                                onDismissRequest()
                            },
                        text = service.name + " " + service.price.toPlainString() + " " + service.desc,
                        maxLines = 1
                    )
                }
            }
        }
    }
}