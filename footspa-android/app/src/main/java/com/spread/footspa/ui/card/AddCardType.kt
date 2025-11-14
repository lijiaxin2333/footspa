package com.spread.footspa.ui.card

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.spread.footspa.db.CardType
import com.spread.footspa.db.FSDB
import com.spread.footspa.ui.common.IconConstants
import com.spread.footspa.ui.common.MoneyExpr
import com.spread.footspa.ui.common.MoneyInput2
import com.spread.footspa.ui.common.MoneyInputState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Composable
fun CardTypeManagementScreen(
    modifier: Modifier = Modifier
) {

    var showAddCardTypeDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val cardInfoList = remember { mutableStateListOf<CardType>() }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            FSDB.cardInfoFlow.collectLatest {
                cardInfoList.clear()
                cardInfoList.addAll(it)
            }
        }
    }

    Column(modifier = modifier) {

        Button(
            onClick = {
                showAddCardTypeDialog = true
            }
        ) {
            Text(text = "新增会员卡类型")
        }

        CardTypeList(modifier = Modifier, cardInfoList = cardInfoList)
    }

    if (showAddCardTypeDialog) {
        AddCardTypeDialog(
            onDismiss = {
                showAddCardTypeDialog = false
            },
            onConfirm = {
                scope.launch(Dispatchers.IO) {
                    val ids = FSDB.insertCardInfo(it)
                    Log.d("Spread", "card ids: $ids")
                }
                showAddCardTypeDialog = false
            }
        )
    }
}


@Composable
fun CardTypeList(
    modifier: Modifier,
    cardInfoList: List<CardType>,
    onCardInfoClick: ((CardType) -> Unit)? = null
) {
    var selectedCardInfo by remember { mutableStateOf<CardType?>(null) }
    LazyColumn(modifier = modifier) {
        items(cardInfoList.size) { index ->
            val info = cardInfoList[index]
            CardInfoItem(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp),
                cardInfo = info,
                isSelected = selectedCardInfo == info,
                onCardInfoClick = {
                    selectedCardInfo = info
                    onCardInfoClick?.invoke(info)
                }
            )
        }
    }
}

@Composable
fun CardInfoItem(
    modifier: Modifier,
    cardInfo: CardType,
    isSelected: Boolean,
    onCardInfoClick: ((CardType) -> Unit)? = null
) {
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxSize(),
            onClick = {
                onCardInfoClick?.invoke(cardInfo)
            }) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        color =
                            if (isSelected) Color.Red
                            else MaterialTheme.colorScheme.background
                    ),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "卡类: ${cardInfo.name}")
                Text(text = "${cardInfo.price.toPlainString()}起充")
                Text(text = "${cardInfo.discount}折")
            }
        }
        if (cardInfo.legacy) {
            Icon(
                modifier = Modifier
                    .size(IconConstants.ICON_SIZE_NORMAL)
                    .align(Alignment.TopEnd),
                imageVector = Icons.Filled.Warning,
                contentDescription = "legacy"
            )
        }
    }
}

@Composable
fun AddCardTypeDialog(
    onDismiss: () -> Unit,
    onConfirm: (CardType) -> Unit
) {
    var cardNameInputText by remember { mutableStateOf("") }
    var cardDiscountInputText by remember { mutableStateOf("") }
    var cardLegacy by remember { mutableStateOf(false) }
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

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "新增会员卡类型")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = cardNameInputText,
                    onValueChange = {
                        cardNameInputText = it
                    },
                    label = { Text("卡类") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = cardDiscountInputText,
                    onValueChange = {
                        cardDiscountInputText = it
                    },
                    label = { Text("折扣") },
                    singleLine = true
                )
                Row {
                    MoneyExpr(
                        modifier = Modifier.weight(1f),
                        label = "充值金额",
                        expression = expression,
                        initial = null,
                        value = value,
                        err = err
                    )
                    FilterChip(
                        onClick = { cardLegacy = !cardLegacy },
                        label = { Text("老卡") },
                        selected = cardLegacy,
                        leadingIcon = if (cardLegacy) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Done,
                                    contentDescription = "legacy"
                                )
                            }
                        } else {
                            null
                        }
                    )
                }
                MoneyInput2(inputState = moneyInputState)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                value?.let {
                    val info = CardType(
                        name = cardNameInputText,
                        price = it,
                        discount = cardDiscountInputText,
                        legacy = cardLegacy,
                        valid = true
                    )
                    onConfirm(info)
                }
            }) {
                Text(text = "确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消")
            }
        }
    )
}