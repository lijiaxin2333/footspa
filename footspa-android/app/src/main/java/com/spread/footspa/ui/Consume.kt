package com.spread.footspa.ui

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.room.withTransaction
import com.spread.footspa.common.displayStr
import com.spread.footspa.db.CardType
import com.spread.footspa.db.FSDB
import com.spread.footspa.db.MassageService
import com.spread.footspa.db.MoneyNode
import com.spread.footspa.db.MoneyNodeType
import com.spread.footspa.db.buildMoneyNode
import com.spread.footspa.db.queryCard
import com.spread.footspa.db.queryMassageService
import com.spread.footspa.db.queryMoneyNode
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

private class Consumption {
    var type by mutableStateOf(ConsumptionType.None)
    var customer by mutableStateOf<MoneyNode?>(null)
    var card by mutableStateOf<MoneyNode?>(null)
    var money by mutableStateOf<BigDecimal?>(null)
    var service by mutableStateOf<MassageService?>(null)
    var servant by mutableStateOf<MoneyNode?>(null)
    var third by mutableStateOf<MoneyNode?>(null)
    var remark by mutableStateOf<String?>(null)
    val addMap = mutableStateMapOf<String, Boolean>()
    var showMoneyInput by mutableStateOf(false)

    var ready by mutableStateOf(false)

    fun needAdd(type: MoneyNodeType) = addMap[type.str] ?: false

    fun markNeedAdd(type: MoneyNodeType, value: Boolean) {
        addMap[type.str] = value
    }

}


@Composable
fun ConsumeScreen(modifier: Modifier = Modifier) {
    val consumptions = remember { mutableStateListOf(Consumption()) }
    var customerDialogIndex by remember { mutableIntStateOf(-1) }
    var cardDialogIndex by remember { mutableIntStateOf(-1) }
    var serviceDialogIndex by remember { mutableIntStateOf(-1) }
    var servantDialogIndex by remember { mutableIntStateOf(-1) }
    var thirdDialogIndex by remember { mutableIntStateOf(-1) }
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
                        OutlinedButton(
                            onClick = {
                                scope.launch(Dispatchers.IO) {
                                    FSDB.db.withTransaction {
                                        submitConsumptions(consumptions)
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
            index = thirdDialogIndex,
            queryType = MoneyNodeType.Third,
            queryState = thirdQueryState
        )
    }
}

@Composable
private fun OneConsumption(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    onClickCustomer: () -> Unit,
    onClickService: () -> Unit,
    onClickServant: () -> Unit,
    onClickThirdParty: () -> Unit,
    onClickCard: () -> Unit,
    onDelete: () -> Unit
) {
    Column(modifier = modifier) {
        var typeSelected by remember { mutableStateOf(false) }
        if (!typeSelected) {
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
                    typeSelected = true
                }
            )
        } else {
            Text(text = "消费类型: ${consumption.type.str}", color = Color.Red)
        }
        when (consumption.type) {
            ConsumptionType.Purchase -> {
                var customerFinish by remember { mutableStateOf(false) }
                var serviceFinish by remember { mutableStateOf(false) }
                var servantFinish by remember { mutableStateOf(false) }
                var moneyFinish by remember { mutableStateOf(false) }
                var remarkFinish by remember { mutableStateOf(false) }
                StepColumn {
                    add { finish ->
                        CustomerInfo(
                            consumption = consumption,
                            onClickCustomer = onClickCustomer,
                            isFinished = customerFinish,
                            finish = {
                                finish()
                                customerFinish = true
                            }
                        )
                    }
                    add { finish ->
                        ServiceInfo(
                            consumption = consumption,
                            onClickService = onClickService,
                            isFinished = serviceFinish,
                            finish = {
                                finish()
                                serviceFinish = true
                            }
                        )
                    }
                    add { finish ->
                        ServantInfo(
                            consumption = consumption,
                            onClickServant = onClickServant,
                            isFinished = servantFinish,
                            finish = {
                                finish()
                                servantFinish = true
                            }
                        )
                    }
                    add { finish ->
                        MoneyInfo(
                            consumption = consumption,
                            isFinished = moneyFinish,
                            finish = {
                                finish()
                                moneyFinish = true
                            }
                        )
                    }
                    add { finish ->
                        RemarkInfo(
                            consumption = consumption,
                            isFinished = remarkFinish,
                            finish = {
                                finish()
                                remarkFinish = true
                                consumption.ready = true
                            }
                        )
                    }
                }
            }

            ConsumptionType.Deposit -> {
                var customerFinish by remember { mutableStateOf(false) }
                var cardFinish by remember { mutableStateOf(false) }
                var moneyFinish by remember { mutableStateOf(false) }
                var remarkFinish by remember { mutableStateOf(false) }
                StepColumn {
                    add { finish ->
                        CustomerInfo(
                            consumption = consumption,
                            onClickCustomer = onClickCustomer,
                            isFinished = customerFinish,
                            finish = {
                                finish()
                                customerFinish = true
                            }
                        )
                    }
                    add { finish ->
                        CardInfo(
                            consumption = consumption,
                            onClickCard = onClickCard,
                            isFinished = cardFinish,
                            finish = {
                                finish()
                                cardFinish = true
                            }
                        )
                    }
                    add { finish ->
                        MoneyInfo(
                            consumption = consumption,
                            isFinished = moneyFinish,
                            finish = {
                                finish()
                                moneyFinish = true
                            }
                        )
                    }
                    add { finish ->
                        RemarkInfo(
                            consumption = consumption,
                            isFinished = remarkFinish,
                            finish = {
                                finish()
                                remarkFinish = true
                                consumption.ready = true
                            }
                        )
                    }
                }
            }

            ConsumptionType.UseCard -> {
                var customerFinish by remember { mutableStateOf(false) }
                var serviceFinish by remember { mutableStateOf(false) }
                var servantFinish by remember { mutableStateOf(false) }
                var cardFinish by remember { mutableStateOf(false) }
                var moneyFinish by remember { mutableStateOf(false) }
                var remarkFinish by remember { mutableStateOf(false) }
                StepColumn {
                    add { finish ->
                        CustomerInfo(
                            consumption = consumption,
                            onClickCustomer = onClickCustomer,
                            isFinished = customerFinish,
                            finish = {
                                finish()
                                customerFinish = true
                            }
                        )
                    }
                    add { finish ->
                        ServiceInfo(
                            consumption = consumption,
                            onClickService = onClickService,
                            isFinished = serviceFinish,
                            finish = {
                                finish()
                                serviceFinish = true
                            }
                        )
                    }
                    add { finish ->
                        ServantInfo(
                            consumption = consumption,
                            onClickServant = onClickServant,
                            isFinished = servantFinish,
                            finish = {
                                finish()
                                servantFinish = true
                            }
                        )
                    }
                    add { finish ->
                        CardInfo(
                            consumption = consumption,
                            onClickCard = onClickCard,
                            isFinished = cardFinish,
                            finish = {
                                finish()
                                cardFinish = true
                            }
                        )
                    }
                    add { finish ->
                        MoneyInfo(
                            consumption = consumption,
                            isFinished = moneyFinish,
                            finish = {
                                finish()
                                moneyFinish = true
                            }
                        )
                    }
                    add { finish ->
                        RemarkInfo(
                            consumption = consumption,
                            isFinished = remarkFinish,
                            finish = {
                                finish()
                                remarkFinish = true
                                consumption.ready = true
                            }
                        )
                    }
                }
            }

            ConsumptionType.ThirdParty -> {
                var customerFinish by remember { mutableStateOf(false) }
                var serviceFinish by remember { mutableStateOf(false) }
                var servantFinish by remember { mutableStateOf(false) }
                var thirdPartyFinish by remember { mutableStateOf(false) }
                var moneyFinish by remember { mutableStateOf(false) }
                var remarkFinish by remember { mutableStateOf(false) }
                StepColumn {
                    add { finish ->
                        CustomerInfo(
                            consumption = consumption,
                            onClickCustomer = onClickCustomer,
                            isFinished = customerFinish,
                            finish = {
                                finish()
                                customerFinish = true
                            }
                        )
                    }
                    add { finish ->
                        ServiceInfo(
                            consumption = consumption,
                            onClickService = onClickService,
                            isFinished = serviceFinish,
                            finish = {
                                finish()
                                serviceFinish = true
                            }
                        )
                    }
                    add { finish ->
                        ServantInfo(
                            consumption = consumption,
                            onClickServant = onClickServant,
                            isFinished = servantFinish,
                            finish = {
                                finish()
                                servantFinish = true
                            }
                        )
                    }
                    add { finish ->
                        ThirdPartyInfo(
                            consumption = consumption,
                            onClickThirdParty = onClickThirdParty,
                            isFinished = thirdPartyFinish,
                            finish = {
                                finish()
                                thirdPartyFinish = true
                            }
                        )
                    }
                    add { finish ->
                        MoneyInfo(
                            consumption = consumption,
                            isFinished = moneyFinish,
                            finish = {
                                finish()
                                moneyFinish = true
                            }
                        )
                    }
                    add { finish ->
                        RemarkInfo(
                            consumption = consumption,
                            isFinished = remarkFinish,
                            finish = {
                                finish()
                                remarkFinish = true
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
    onClickCustomer: () -> Unit,
    isFinished: Boolean,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!isFinished) {
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
    isFinished: Boolean,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!isFinished) {
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
            finish()
        }
    }
}

@Composable
private fun ThirdPartyInfo(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    onClickThirdParty: () -> Unit,
    isFinished: Boolean,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!isFinished) {
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
    isFinished: Boolean,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!isFinished) {
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
            finish()
        }
    }
}

@Composable
private fun MoneyInfo(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    isFinished: Boolean,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!isFinished) {
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
                finish()
            }
        }
    }
}

@Composable
private fun CardInfo(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    onClickCard: () -> Unit,
    isFinished: Boolean,
    finish: () -> Unit
) {
    Column(modifier = modifier) {
        if (!isFinished) {
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
            var selectedCardInfo: CardType? by remember { mutableStateOf(null) }
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
                        selectedCardInfo = it
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
                val cardInfo = selectedCardInfo ?: return@Button
                consumption.card = buildMoneyNode {
                    name = cardInfo.name
                    type = MoneyNodeType.Card
                    keys = phoneNumbers
                    cardTypeId = cardInfo.id
                    cardValid = true
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
                finish()
            }
        }
    }
}

@Composable
private fun RemarkInfo(
    modifier: Modifier = Modifier,
    consumption: Consumption,
    isFinished: Boolean,
    finish: () -> Unit
) {
    var remarkInput by remember { mutableStateOf("") }
    Row(modifier = modifier) {
        if (!isFinished) {
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
                    )
                }
            } else {
                queryMoneyNode(
                    query = query,
                    types = setOf(queryType),
                )
            }
            withContext(Dispatchers.Main) {
                queryResult.clear()
                if (res != null) {
                    queryResult.addAll(res)
                }
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

private fun submitConsumptions(consumptions: List<Consumption>) {

}