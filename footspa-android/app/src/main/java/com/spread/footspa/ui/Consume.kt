package com.spread.footspa.ui

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.room.withTransaction
import com.spread.footspa.common.displayStr
import com.spread.footspa.db.Bill
import com.spread.footspa.db.CardType
import com.spread.footspa.db.FSDB
import com.spread.footspa.db.FSDB.Companion.queryCard
import com.spread.footspa.db.FSDB.Companion.queryMassageService
import com.spread.footspa.db.FSDB.Companion.queryMoneyNode
import com.spread.footspa.db.MassageService
import com.spread.footspa.db.MoneyNode
import com.spread.footspa.db.MoneyNodeType
import com.spread.footspa.db.buildBill
import com.spread.footspa.db.buildMoneyNode
import com.spread.footspa.ui.card.ChooseCardType
import com.spread.footspa.ui.common.EasyTextField
import com.spread.footspa.ui.common.InvalidCardChip
import com.spread.footspa.ui.common.LegacyCardChip
import com.spread.footspa.ui.common.MoneyExpr
import com.spread.footspa.ui.common.MoneyInput2
import com.spread.footspa.ui.common.MoneyInputState
import com.spread.footspa.ui.common.MoneyNodeSearchInputSimple
import com.spread.footspa.ui.common.NewNodeChip
import com.spread.footspa.ui.common.PhoneNumberInput
import com.spread.footspa.ui.common.SelectOneOptions
import com.spread.footspa.ui.common.StepColumn
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.math.BigDecimal

enum class ConsumptionType(val str: String) {
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

class Consumption {
    var type by mutableStateOf(ConsumptionType.None)
    var typeSelected by mutableStateOf(false)
    var customer by mutableStateOf<MoneyNode?>(null)
    var card by mutableStateOf<MoneyNode?>(null)
    var money by mutableStateOf<BigDecimal?>(null)
    var moneyThird by mutableStateOf<BigDecimal?>(null)
    var service by mutableStateOf<MassageService?>(null)
    var servant by mutableStateOf<MoneyNode?>(null)
    var third by mutableStateOf<MoneyNode?>(null)
    var remark by mutableStateOf<String?>(null)
    val addMap = mutableStateMapOf<String, Boolean>()
    var showMoneyInput by mutableStateOf(false)
    var showThirdMoneyInput by mutableStateOf(false)

    var customerFinish by mutableStateOf(false)
    var cardFinish by mutableStateOf(false)
    var serviceFinish by mutableStateOf(false)
    var servantFinish by mutableStateOf(false)
    var thirdPartyFinish by mutableStateOf(false)
    var moneyThirdFinish by mutableStateOf(false)
    var moneyFinish by mutableStateOf(false)
    var remarkFinish by mutableStateOf(false)

    var ready by mutableStateOf(false)

    fun needAdd(type: MoneyNodeType) = addMap[type.str] ?: false

    fun markNeedAdd(type: MoneyNodeType, value: Boolean) {
        addMap[type.str] = value
    }

}


@Composable
fun ConsumeScreen(modifier: Modifier = Modifier) {
    val consumptions = remember { mutableStateListOf(Consumption()) }
    val cache = remember { ConsumeCache() }
    var customerDialogIndex by remember { mutableIntStateOf(-1) }
    var cardDialogIndex by remember { mutableIntStateOf(-1) }
    var serviceDialogIndex by remember { mutableIntStateOf(-1) }
    var servantDialogIndex by remember { mutableIntStateOf(-1) }
    var thirdDialogIndex by remember { mutableIntStateOf(-1) }
    val previewInfo = remember {
        mutableStateMapOf<MoneyNode, List<Pair<MoneyNode, ConsumeCache.BalanceTrace>>>()
    }
    var confirmDialogResult: CompletableDeferred<Boolean>? by remember { mutableStateOf(null) }
    val completeDialog: CompletableDeferred<Boolean>.(Boolean) -> Unit = remember {
        {
            complete(it)
            confirmDialogResult = null
            previewInfo.clear()
        }
    }
    LazyColumn(modifier = modifier) {
        itemsIndexed(consumptions) { index, consumption ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight()
                ) {
                    Text(modifier = Modifier.align(Alignment.Center), text = "${index + 1}")
                }
                VerticalDivider()
                OneConsumption(
                    modifier = Modifier
                        .weight(1f)
                        .wrapContentHeight(),
                    consumption = consumption,
                    cache = cache,
                    onClickCustomer = {
                        customerDialogIndex = index
                    },
                    onClickService = {
                        serviceDialogIndex = index
                    },
                    onClickServant = {
                        servantDialogIndex = index
                    },
                    onClickThirdParty = {
                        thirdDialogIndex = index
                    },
                    onClickCard = {
                        cardDialogIndex = index
                    },
                    onDelete = {
                        consumptions.remove(consumption)
                    }
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 5.dp))
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(
                        onClick = {
                            consumptions.add(Consumption())
                        }
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                modifier = Modifier.padding(end = 5.dp),
                                text = "新增一笔消费"
                            )
                            Icon(imageVector = Icons.Default.Add, contentDescription = "添加")
                        }
                    }
                    if (consumptions.all { it.ready }) {
                        val scope = rememberCoroutineScope()
                        val ctx = LocalContext.current
                        OutlinedButton(
                            onClick = {
                                scope.launch(Dispatchers.Main) {
                                    withContext(Dispatchers.IO) {
                                        cache.getPreviewInfo(consumptions)
                                    }.let {
                                        previewInfo.clear()
                                        previewInfo.putAll(it)
                                    }
                                    confirmDialogResult = CompletableDeferred()
                                    val confirm = confirmDialogResult?.await() ?: return@launch
                                    if (confirm) {
                                        kotlin.runCatching {
                                            FSDB.db.withTransaction {
                                                submitConsumptions(consumptions, cache)
                                            }
                                        }.onFailure {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    ctx,
                                                    "提交失败",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }.onSuccess {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    ctx,
                                                    "提交成功",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                    } else {
                                        Toast.makeText(
                                            ctx,
                                            "取消成功",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        ) {
                            Text(text = "提交所有消费")
                        }
                    }
                }
            }
        }
    }
    // TODO: must be outside of Dialog, otherwise UI not refresh, why???
    val customerQueryState = remember { TextFieldState() }
    val cardQueryState = remember { TextFieldState() }
    val serviceQueryState = remember { TextFieldState() }
    val servantQueryState = remember { TextFieldState() }
    val thirdQueryState = remember { TextFieldState() }
    if (customerDialogIndex in consumptions.indices) {
        QueryMoneyNodeDialog(
            onDismissRequest = {
                customerDialogIndex = -1
            },
            consumptions = consumptions,
            cache = cache,
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
            cache = cache,
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
            cache = cache,
            index = servantDialogIndex,
            queryType = MoneyNodeType.Employee,
            queryState = servantQueryState,
            canAdd = false
        )
    }
    if (thirdDialogIndex in consumptions.indices) {
        QueryMoneyNodeDialog(
            onDismissRequest = {
                thirdDialogIndex = -1
            },
            consumptions = consumptions,
            cache = cache,
            index = thirdDialogIndex,
            queryType = MoneyNodeType.Third,
            queryState = thirdQueryState
        )
    }
    confirmDialogResult?.run {
        AlertDialog(
            onDismissRequest = {
                completeDialog(false)
            },
            properties = DialogProperties(dismissOnBackPress = false),
            confirmButton = {
                TextButton(onClick = {
                    completeDialog(true)
                }) {
                    Text(text = "确定")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    completeDialog(false)
                }) {
                    Text(text = "取消")
                }
            },
            title = {
                Text(text = "确定提交吗？")
            },
            text = {
                PreviewList(previewInfo)
            }
        )
    }
}

@Composable
private fun PreviewList(previewInfo: Map<MoneyNode, List<Pair<MoneyNode, ConsumeCache.BalanceTrace>>>) {
    Column(
        Modifier.scrollable(
            state = rememberScrollState(),
            orientation = Orientation.Vertical
        )
    ) {
        for ((customer, cardInfoList) in previewInfo) {
            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                Text("${customer.name}的消费：")
                for ((card, trace) in cardInfoList) {
                    Text("\t${card.name}")
                    Text("\t\t原始余额：${trace.originBalance}")
                    for (deposit in trace.depositList) {
                        Text("\t\t\t充值：+${deposit.displayStr}")
                    }
                    for (use in trace.useList) {
                        Text("\t\t\t消费：-${use.displayStr}")
                    }
                    Text("\t\t当前余额：${trace.balance}")
                    HorizontalDivider()
                }
            }
        }
    }
}

@Composable
private fun OneConsumption(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    cache: ConsumeCache,
    onClickCustomer: () -> Unit,
    onClickService: () -> Unit,
    onClickServant: () -> Unit,
    onClickThirdParty: () -> Unit,
    onClickCard: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = modifier) {
        if (!consumption.typeSelected) {
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
                    consumption.typeSelected = true
                }
            )
        } else {
            Text(text = "消费类型: ${consumption.type.str}", color = Color.Red)
        }
        when (consumption.type) {
            ConsumptionType.Purchase -> {
                StepColumn {
                    add { finish ->
                        CustomerInfo(
                            consumption = consumption,
                            cache = cache,
                            onClickCustomer = onClickCustomer,
                            finish = finish
                        )
                    }
                    add { finish ->
                        ServiceInfo(
                            consumption = consumption,
                            onClickService = onClickService,
                            finish = finish
                        )
                    }
                    add { finish ->
                        ServantInfo(
                            consumption = consumption,
                            onClickServant = onClickServant,
                            finish = finish
                        )
                    }
                    add { finish ->
                        MoneyInfo(
                            consumption = consumption,
                            finish = finish
                        )
                    }
                    add { finish ->
                        RemarkInfo(
                            consumption = consumption,
                            finish = {
                                finish()
                                consumption.ready = true
                            }
                        )
                    }
                }
            }

            ConsumptionType.Deposit -> {
                StepColumn {
                    add { finish ->
                        CustomerInfo(
                            consumption = consumption,
                            cache = cache,
                            onClickCustomer = onClickCustomer,
                            finish = finish
                        )
                    }
                    add { finish ->
                        CardInfo(
                            consumption = consumption,
                            cache = cache,
                            onClickCard = onClickCard,
                            finish = finish
                        )
                    }
                    add { finish ->
                        MoneyInfo(
                            consumption = consumption,
                            finish = finish
                        )
                    }
                    add { finish ->
                        RemarkInfo(
                            consumption = consumption,
                            finish = {
                                finish()
                                consumption.ready = true
                            }
                        )
                    }
                }
            }

            ConsumptionType.UseCard -> {
                StepColumn {
                    add { finish ->
                        CustomerInfo(
                            consumption = consumption,
                            cache = cache,
                            onClickCustomer = onClickCustomer,
                            finish = finish
                        )
                    }
                    add { finish ->
                        ServiceInfo(
                            consumption = consumption,
                            onClickService = onClickService,
                            finish = finish
                        )
                    }
                    add { finish ->
                        ServantInfo(
                            consumption = consumption,
                            onClickServant = onClickServant,
                            finish = finish
                        )
                    }
                    add { finish ->
                        CardInfo(
                            consumption = consumption,
                            cache = cache,
                            onClickCard = onClickCard,
                            finish = finish
                        )
                    }
                    add { finish ->
                        MoneyInfo(
                            consumption = consumption,
                            finish = finish
                        )
                    }
                    add { finish ->
                        RemarkInfo(
                            consumption = consumption,
                            finish = {
                                finish()
                                consumption.ready = true
                            }
                        )
                    }
                }
            }

            ConsumptionType.ThirdParty -> {
                StepColumn {
                    add { finish ->
                        CustomerInfo(
                            consumption = consumption,
                            cache = cache,
                            onClickCustomer = onClickCustomer,
                            finish = finish
                        )
                    }
                    add { finish ->
                        ServiceInfo(
                            consumption = consumption,
                            onClickService = onClickService,
                            finish = finish
                        )
                    }
                    add { finish ->
                        ServantInfo(
                            consumption = consumption,
                            onClickServant = onClickServant,
                            finish = finish
                        )
                    }
                    add { finish ->
                        ThirdPartyInfo(
                            consumption = consumption,
                            cache = cache,
                            onClickThirdParty = onClickThirdParty,
                            finish = finish
                        )
                    }
                    add { finish ->
                        MoneyThirdInfo(
                            consumption = consumption,
                            finish = finish
                        )
                    }
                    add { finish ->
                        MoneyInfo(
                            consumption = consumption,
                            finish = finish
                        )
                    }
                    add { finish ->
                        RemarkInfo(
                            consumption = consumption,
                            finish = {
                                finish()
                                consumption.ready = true
                            }
                        )
                    }
                }
            }

            else -> {}
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            if (consumption.ready) {
                FilledIconButton(
                    enabled = false,
                    onClick = {}
                ) {
                    Icon(
                        imageVector = Icons.Filled.Done,
                        contentDescription = "完成"
                    )
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
            FilledIconButton(
                onClick = onDelete,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.Red
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "删除"
                )
            }
        }
    }
}

@Composable
private fun CustomerInfo(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    cache: ConsumeCache,
    onClickCustomer: () -> Unit,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!consumption.customerFinish && !consumption.needAdd(MoneyNodeType.Customer)) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onClickCustomer()
                }
            ) {
                Text(text = "确定顾客信息")
            }
        }
        if (consumption.needAdd(MoneyNodeType.Customer)) {
            val scope = rememberCoroutineScope()
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
                }.also {
                    scope.launch(Dispatchers.IO) {
                        cache.commit(consumption to it)
                    }
                }
                consumption.markNeedAdd(MoneyNodeType.Customer, false)
            }) { Text(text = "确定添加") }
        } else {
            val nodes by FSDB.moneyNodeFlow.collectAsState()
            val newCustomer = nodes.find {
                consumption.customer == it
            } == null
            consumption.customer?.let {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("顾客姓名: ${it.name}")
                    if (newCustomer) {
                        NewNodeChip(MoneyNodeType.Customer)
                    }
                }
                Text("顾客电话: ${it.keys?.joinToString()}")
                consumption.customerFinish = true
                finish()
            }
        }
    }
}

@Composable
private fun ServantInfo(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    onClickServant: () -> Unit,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!consumption.servantFinish) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onClickServant()
                },
            ) {
                Text(text = "确定服务员工信息")
            }
        }
        consumption.servant?.let {
            Text("员工姓名: ${it.name}")
            Text("员工电话: ${it.keys?.joinToString()}")
            consumption.servantFinish = true
            finish()
        }
    }
}

@Composable
private fun ThirdPartyInfo(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    cache: ConsumeCache,
    onClickThirdParty: () -> Unit,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!consumption.thirdPartyFinish && !consumption.needAdd(MoneyNodeType.Third)) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onClickThirdParty()
                }
            ) {
                Text(text = "确定三方平台信息")
            }
        }
        if (consumption.needAdd(MoneyNodeType.Third)) {
            val scope = rememberCoroutineScope()
            var nameInput by remember { mutableStateOf("") }
            Row(modifier = modifier) {
                EasyTextField(
                    modifier = Modifier.weight(1f),
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                    }
                )
                TextButton(
                    onClick = {
                        consumption.third = buildMoneyNode {
                            this.name = nameInput
                            this.type = MoneyNodeType.Third
                        }.also {
                            scope.launch(Dispatchers.IO) {
                                cache.commit(consumption to it)
                            }
                        }
                        consumption.markNeedAdd(MoneyNodeType.Third, false)
                    }
                ) {
                    Text(text = "确定")
                }
            }
        } else {
            consumption.third?.let {
                Text(text = "三方平台: ${it.name}")
                consumption.thirdPartyFinish = true
                finish()
            }
        }
    }
}

@Composable
private fun ServiceInfo(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    onClickService: () -> Unit,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!consumption.serviceFinish) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onClickService()
                }
            ) {
                Text(text = "确定服务信息")
            }
        }
        consumption.service?.let {
            Text("项目: ${it.name}")
            Text("价格: ${it.price.displayStr}")
            it.desc?.takeIf { d -> d.isNotBlank() }?.let { desc ->
                Text(desc)
            }
            consumption.serviceFinish = true
            finish()
        }
    }
}

@Composable
private fun MoneyInfo(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!consumption.moneyFinish && !consumption.showMoneyInput) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    consumption.showMoneyInput = true
                }
            ) {
                Text(text = "确认金额")
            }
        }
        if (consumption.showMoneyInput) {
            val moneyInputState = remember { MoneyInputState() }
            var expression by remember { mutableStateOf("") }
            var value: BigDecimal? by remember { mutableStateOf(BigDecimal.ZERO) }
            var err: String? by remember { mutableStateOf(null) }
            LaunchedEffect(Unit) {
                moneyInputState.expressionDataFlow.collect {
                    expression = it.expr
                    value = it.value
                    err = it.err
                }
            }
            MoneyExpr(
                label = "价格",
                expression = expression,
                initial = null,
                value = value,
                err = err
            )
            MoneyInput2(inputState = moneyInputState)
            value?.let {
                Button(
                    onClick = {
                        consumption.money = it
                        consumption.showMoneyInput = false
                    }
                ) {
                    Text("确认金额: ${it.displayStr}")
                }
            }
        } else {
            consumption.money?.let { money ->
                Text("金额: ${money.displayStr}")
                consumption.moneyFinish = true
                finish()
            }
        }
    }
}


@Composable
private fun MoneyThirdInfo(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!consumption.moneyThirdFinish && !consumption.showThirdMoneyInput) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    consumption.showThirdMoneyInput = true
                }
            ) {
                Text(text = "确认三方平台金额")
            }
        }
        if (consumption.showThirdMoneyInput) {
            val moneyInputState = remember { MoneyInputState() }
            var expression by remember { mutableStateOf("") }
            var value: BigDecimal? by remember { mutableStateOf(BigDecimal.ZERO) }
            var err: String? by remember { mutableStateOf(null) }
            LaunchedEffect(Unit) {
                moneyInputState.expressionDataFlow.collect {
                    expression = it.expr
                    value = it.value
                    err = it.err
                }
            }
            MoneyExpr(
                label = "价格",
                expression = expression,
                initial = null,
                value = value,
                err = err
            )
            MoneyInput2(inputState = moneyInputState)
            value?.let {
                Button(
                    onClick = {
                        consumption.moneyThird = it
                        consumption.showThirdMoneyInput = false
                    }
                ) {
                    Text("确认金额: ${it.displayStr}")
                }
            }
        } else {
            consumption.moneyThird?.let { money ->
                Text("三方平台收款: ${money.displayStr}")
                consumption.moneyThirdFinish = true
                finish()
            }
        }
    }
}

@Composable
private fun CardInfo(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    cache: ConsumeCache,
    onClickCard: () -> Unit,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!consumption.cardFinish && !consumption.needAdd(MoneyNodeType.Card)) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    onClickCard()
                }
            ) {
                Text(text = "确定会员卡信息")
            }
        }
        if (consumption.needAdd(MoneyNodeType.Card)) {
            val scope = rememberCoroutineScope()
            var selectedCardType: CardType? by remember { mutableStateOf(null) }
            val phoneNumbers = remember { mutableStateListOf("") }
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    ChooseCardType(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight()
                    ) {
                        selectedCardType = it
                    }
                }
                item {
                    PhoneNumberInput(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
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
                }
            }
            Button(onClick = {
                val cardType = selectedCardType ?: return@Button
                consumption.card = buildMoneyNode {
                    name = cardType.name
                    type = MoneyNodeType.Card
                    keys = phoneNumbers
                    cardTypeId = cardType.id
                    cardValid = true
                }.also {
                    scope.launch(Dispatchers.IO) {
                        cache.commit(consumption to it)
                    }
                }
                consumption.markNeedAdd(MoneyNodeType.Card, false)
            }) {
                Text(text = "确定添加")
            }
        } else {
            val cards by FSDB.moneyNodeFlow.collectAsState()
            val newCard = cards.find {
                consumption.card == it
            } == null
            var cardType by remember {
                mutableStateOf<CardType?>(null)
            }
            LaunchedEffect(consumption.card) {
                consumption.card?.let {
                    cardType = FSDB.findCardType(it)
                }
            }
            consumption.card?.let { card ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("卡名: ${card.name}")
                    if (newCard) {
                        NewNodeChip(MoneyNodeType.Card)
                    }
                    if (cardType?.legacy == true) {
                        LegacyCardChip()
                    }
                    card.cardValid?.let { valid ->
                        if (!valid) {
                            InvalidCardChip()
                        }
                    }
                }
                Text("卡电话: ${card.keys?.joinToString()}")
                cardType?.let {
                    Text("起充: ${it.price.displayStr}")
                    Text("折扣: ${it.discount}")
                }
                consumption.cardFinish = true
                finish()
            }
        }
    }
}

@Composable
private fun RemarkInfo(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    finish: () -> Unit
) {
    var remarkInput by remember { mutableStateOf("") }
    Row(modifier = modifier) {
        if (!consumption.remarkFinish) {
            EasyTextField(
                modifier = Modifier.weight(1f),
                value = remarkInput,
                onValueChange = {
                    remarkInput = it
                }
            )
            TextButton(
                onClick = {
                    consumption.remark = remarkInput
                    consumption.remarkFinish = true
                    finish()
                }
            ) {
                Text(text = "确定备注")
            }
        } else {
            Text(text = "备注: ${consumption.remark}")
        }
    }
}

@Composable
private fun QueryMoneyNodeDialog(
    onDismissRequest: () -> Unit,
    consumptions: List<Consumption>,
    cache: ConsumeCache,
    index: Int,
    queryType: MoneyNodeType,
    queryState: TextFieldState,
    canAdd: Boolean = true,
) {
    val scope = rememberCoroutineScope()
    val consumption = consumptions[index]
    val queryResult = remember { mutableStateListOf<MoneyNode>() }
    val search: (query: String) -> Unit = { query ->
        if (query.isBlank()) {
            queryResult.clear()
        }
        scope.launch(Dispatchers.IO) {
            val res = if (queryType == MoneyNodeType.Card) {
                consumption.customer?.let {
                    queryCard(
                        query = query,
                        customer = it
                    ).toMutableList()
                } ?: mutableListOf()
            } else {
                queryMoneyNode(
                    query = query,
                    types = setOf(queryType),
                ).toMutableList()
            }
            cache.merge(res, queryType, consumption.customer)
            withContext(Dispatchers.Main) {
                queryResult.clear()
                queryResult.addAll(res)
            }
        }
    }
    LaunchedEffect(Unit) {
        search(queryState.text.toString())
    }
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
                    onSearch = search
                )
                if (canAdd) {
                    OutlinedIconButton(
                        modifier = Modifier.wrapContentSize(),
                        shape = RectangleShape,
                        onClick = {
                            consumption.markNeedAdd(queryType, true)
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
            }
            LazyColumn {
                items(queryResult) { node ->
                    Text(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp)
                            .clickable {
                                consumption.run {
                                    when (queryType) {
                                        MoneyNodeType.Customer -> this.customer = node
                                        MoneyNodeType.Card -> this.card = node
                                        MoneyNodeType.Employee -> this.servant = node
                                        MoneyNodeType.Third -> this.third = node
                                        else -> {}
                                    }
                                    if (canAdd) {
                                        markNeedAdd(queryType, false)
                                    }
                                }
                                onDismissRequest()
                            },
                        text = node.name + " " + node.keys?.joinToString(),
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
    val massageServices by FSDB.massageServiceFlow.collectAsState()
    val search: (query: String) -> Unit = { query ->
        if (query.isBlank()) {
            queryResult.clear()
        }
        scope.launch(Dispatchers.IO) {
            val res = queryMassageService(
                query = query,
                services = massageServices
            )
            withContext(Dispatchers.Main) {
                queryResult.clear()
                queryResult.addAll(res)
            }
        }
    }
    LaunchedEffect(Unit) {
        search(queryState.text.toString())
    }
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
                    onSearch = search
                )
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
                                }
                                onDismissRequest()
                            },
                        text = service.name + " " + service.price.displayStr + " " + service.desc,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

private suspend fun genPreviewInfo(
    consumptions: List<Consumption>,
    cache: ConsumeCache
) {

}

private suspend fun submitConsumptions(
    consumptions: List<Consumption>,
    cache: ConsumeCache
) {
    // stage 1: add all money nodes and services
    cache.flush(consumptions)
    // stage 2: add bills
    val public = FSDB.getPublic()
    val outside = FSDB.getOutside()
    val bills = mutableListOf<Bill>()
    for (consumption in consumptions) {
        when (consumption.type) {
            ConsumptionType.Purchase -> {
                val customer = consumption.customer.checkAndGet()
                val money = consumption.money.checkAndGet()
                val remark = consumption.remark ?: ""
                val service = consumption.service.checkAndGet()
                val servant = consumption.servant.checkAndGet()
                val bill = buildBill {
                    fromId = customer.id
                    toId = public.id
                    this.money = money
                    this.remark = remark
                    this.service = service.id
                    this.servant = servant.id
                }
                bills.add(bill)
            }

            ConsumptionType.Deposit -> {
                val customer = consumption.customer.checkAndGet()
                val card = consumption.card.checkAndGet()
                val money = consumption.money.checkAndGet()
                val remark = consumption.remark ?: ""
                val bill1 = buildBill {
                    fromId = customer.id
                    toId = public.id
                    this.money = money
                    this.remark = remark
                }
                val bill2 = buildBill {
                    fromId = customer.id
                    toId = card.id
                    this.money = money
                    this.remark = remark
                }
                bills.add(bill1)
                bills.add(bill2)
            }

            ConsumptionType.UseCard -> {
                val service = consumption.service.checkAndGet()
                val servant = consumption.servant.checkAndGet()
                val card = consumption.card.checkAndGet()
                val money = consumption.money.checkAndGet()
                val remark = consumption.remark ?: ""
                val bill = buildBill {
                    fromId = card.id
                    toId = outside.id
                    this.money = money
                    this.remark = remark
                    this.service = service.id
                    this.servant = servant.id
                }
                bills.add(bill)
            }

            ConsumptionType.ThirdParty -> {
                val customer = consumption.customer.checkAndGet()
                val service = consumption.service.checkAndGet()
                val servant = consumption.servant.checkAndGet()
                val third = consumption.third.checkAndGet()
                val moneyThird = consumption.moneyThird.checkAndGet()
                val money = consumption.money.checkAndGet()
                val remark = consumption.remark ?: ""
                val bill1 = buildBill {
                    fromId = customer.id
                    toId = third.id
                    this.money = moneyThird
                    this.remark = remark
                    this.service = service.id
                    this.servant = servant.id
                }
                val bill2 = buildBill {
                    fromId = third.id
                    toId = public.id
                    this.money = money
                    this.remark = remark
                    this.service = service.id
                    this.servant = servant.id
                }
                bills.add(bill1)
                bills.add(bill2)
            }

            else -> {}
        }
    }
    FSDB.insertBill(*bills.toTypedArray())
    cache.clear()
}


private fun BigDecimal?.checkAndGet(): BigDecimal {
    if (this == null || this == BigDecimal.ZERO) {
        errorInTransaction("money is null or zero")
    }
    return this
}

private fun MassageService?.nonNullGet(): MassageService {
    if (this == null) {
        errorInTransaction("null service")
    }
    return this
}

private fun MoneyNode?.nonNullGet(): MoneyNode {
    if (this == null) {
        errorInTransaction("null money node")
    }
    return this
}

private fun MassageService?.checkAndGet(): MassageService {
    if (this == null || this.id == 0L) {
        errorInTransaction("null or invalid service: ${this?.name}")
    }
    return this
}

private fun MoneyNode?.checkAndGet(): MoneyNode {
    if (this == null || this.id == 0L) {
        errorInTransaction("null or invalid money node: ${this?.name}, ${this?.type?.str}")
    }
    return this
}

class ConsumeCache {
    private val moneyNodePairs = mutableStateListOf<Pair<Consumption, MoneyNode>>()
    private val mutex = Mutex()

    suspend fun commit(vararg pairs: Pair<Consumption, MoneyNode>) {
        mutex.withLock {
            for (pair in pairs) {
                if (FSDB.isMoneyNodeExists(pair.second)) {
                    continue
                }
                if (!moneyNodePairs.contains(pair)) {
                    moneyNodePairs.add(pair)
                }
            }
        }
    }

    fun merge(target: MutableCollection<MoneyNode>, type: MoneyNodeType, customer: MoneyNode?) {
        for ((consumption, node) in moneyNodePairs) {
            if (type == MoneyNodeType.Card) {
                if (customer != null && node.type == type && consumption.customer == customer) {
                    target.add(node)
                }
                continue
            }
            if (node.type == type && !target.contains(node)) {
                target.add(node)
            }
        }
    }

    class BalanceTrace {
        val depositList = mutableListOf<BigDecimal>()
        val useList = mutableListOf<BigDecimal>()
        var originBalance: BigDecimal = BigDecimal.ZERO
        var balance: BigDecimal = BigDecimal.ZERO
    }

    suspend fun getPreviewInfo(consumptions: List<Consumption>): Map<MoneyNode, List<Pair<MoneyNode, BalanceTrace>>> {
        val ownerCardBalance =
            mutableMapOf<MoneyNode, MutableList<Pair<MoneyNode, BalanceTrace>>>()
        mutex.withLock {
            for (consumption in consumptions) {
                if (consumption.type == ConsumptionType.Deposit) {
                    val customer = consumption.customer.nonNullGet()
                    val card = consumption.card.nonNullGet()
                    val money = consumption.money.checkAndGet()
                    val newCustomer = isNewNode(customer)
                    val newCard = isNewNode(card)
                    if (newCustomer && !newCard) {
                        error("新顾客${customer.name}, 老卡${card.name}, 第一次消费的顾客，不能给一张已经存在的卡充值")
                    }
                    val thisCustomerCardsInfo = ownerCardBalance[customer]
                    if (thisCustomerCardsInfo == null) {
                        ownerCardBalance[customer] = mutableListOf()
                    }
                    val thisCustomerCardTrace = ownerCardBalance[customer]!!.find { it.first == card }
                    if (!newCard) {
                        val owner = FSDB.queryCardOwner(card)
                        if (owner != customer) {
                            error("owner is: ${owner.name} but customer is ${customer.name}")
                        }
                        if (thisCustomerCardTrace == null) {
                            ownerCardBalance[customer]!!.add(card to BalanceTrace().apply {
                                originBalance = FSDB.queryCardBalance(card)
                                balance = originBalance + money
                                depositList.add(money)
                            })
                        } else {
                            thisCustomerCardTrace.second.balance += money
                            thisCustomerCardTrace.second.depositList.add(money)
                        }
                    } else {
                        if (thisCustomerCardTrace == null) {
                            ownerCardBalance[customer]!!.add(card to BalanceTrace().apply {
                                balance = money
                                depositList.add(money)
                            })
                        } else {
                            thisCustomerCardTrace.second.balance += money
                            thisCustomerCardTrace.second.depositList.add(money)
                        }
                    }
                }
            }
            for (consumption in consumptions) {
                if (consumption.type == ConsumptionType.UseCard) {
                    val customer = consumption.customer.nonNullGet()
                    val card = consumption.card.nonNullGet()
                    val money = consumption.money.checkAndGet()
                    val newCustomer = isNewNode(customer)
                    val newCard = isNewNode(card)
                    if (newCustomer && !newCard) {
                        error("新顾客${customer.name}, 老卡${card.name}, 第第一次消费的顾客，不能用一张存在的卡消费")
                    }
                    val thisCustomerCardsInfo = ownerCardBalance[customer]
                    if (thisCustomerCardsInfo == null) {
                        ownerCardBalance[customer] = mutableListOf()
                    }
                    val thisCustomerCardTrace = ownerCardBalance[customer]!!.find { it.first == card }
                    if (!newCard) {
                        val owner = FSDB.queryCardOwner(card)
                        if (owner != customer) {
                            error("owner is: ${owner.name} but customer is ${customer.name}")
                        }
                        if (thisCustomerCardTrace == null) {
                            ownerCardBalance[customer]!!.add(card to BalanceTrace().apply {
                                originBalance = FSDB.queryCardBalance(card)
                                balance = originBalance - money
                                useList.add(money)
                            })
                        } else {
                            thisCustomerCardTrace.second.balance -= money
                            thisCustomerCardTrace.second.useList.add(money)
                        }
                    } else {
                        if (thisCustomerCardTrace == null) {
                            error("card cannot be use without deposit")
                        }
                        thisCustomerCardTrace.second.balance -= money
                        thisCustomerCardTrace.second.useList.add(money)
                    }
                }
            }
            return ownerCardBalance
        }
    }

    suspend fun flush(consumptions: List<Consumption>) {
        mutex.withLock {
            for ((_, node) in moneyNodePairs) {
                if (!FSDB.isMoneyNodeExists(node)) {
                    val id = FSDB.insertMoneyNode(node).firstOrNull()
                    if (id != null && id > 0L) {
                        updateConsumption(node, consumptions, id)
                        continue
                    }
                }
                errorInTransaction("failure on add node: $node")
            }
        }
    }

    private suspend fun updateConsumption(
        node: MoneyNode,
        consumptions: List<Consumption>,
        id: Long
    ) {
        for (consumption in consumptions) {
            when (node) {
                consumption.customer -> {
                    consumption.customer = FSDB.getMoneyNode(id)
                }

                consumption.card -> {
                    consumption.card = FSDB.getMoneyNode(id)
                }

                consumption.third -> {
                    consumption.third = FSDB.getMoneyNode(id)
                }
            }
        }
    }

    private fun isNewNode(moneyNode: MoneyNode): Boolean {
        return moneyNodePairs.any { it.second == moneyNode }
    }

    suspend fun clear() {
        mutex.withLock {
            moneyNodePairs.clear()
        }
    }
}

private fun errorInTransaction(err: String): Nothing {
    throw RuntimeException(err)
}
