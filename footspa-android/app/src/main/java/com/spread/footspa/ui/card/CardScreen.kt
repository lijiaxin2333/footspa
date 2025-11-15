package com.spread.footspa.ui.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.spread.footspa.common.displayStr
import com.spread.footspa.db.CardType
import com.spread.footspa.db.FSDB
import com.spread.footspa.db.MoneyNode
import com.spread.footspa.db.MoneyNodeType
import com.spread.footspa.ui.common.InvalidCardChip
import com.spread.footspa.ui.common.LegacyCardChip
import com.spread.footspa.ui.common.PhoneNumberInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun CardScreen(
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )
    HorizontalPager(
        modifier = modifier,
        state = pagerState
    ) { page ->
        if (page == 0) {
            var selectedCardInfo: CardType? by remember { mutableStateOf(null) }
            val phoneNumbers = remember { mutableStateListOf("") }
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    ChooseCardType(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                    ) {
                        selectedCardInfo = it
                    }
                }
                item {
                    Text(text = "联系电话:")
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
                item {
                    Button(
                        onClick = {
                            val cardInfo = selectedCardInfo ?: return@Button
                            scope.launch(Dispatchers.IO) {
                                FSDB.insertCard(
                                    name = cardInfo.name,
                                    phoneNumbers = phoneNumbers,
                                    typeId = cardInfo.id
                                )
                            }
                        }
                    ) {
                        Text(text = "确定")
                    }
                }
            }
        } else {
            val cardList = remember { mutableStateListOf<MoneyNode>() }
            LaunchedEffect(Unit) {
                FSDB.moneyNodeFlow.collect { nodes ->
                    val cards = nodes.filter { it.type == MoneyNodeType.Card }
                    cardList.clear()
                    cardList.addAll(cards)
                }
            }
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(cardList) { card ->
                    Column {
                        var cardType: CardType? by remember { mutableStateOf(null) }
                        LaunchedEffect(card) {
                            cardType = FSDB.findCardType(card)
                        }
                        Row {
                            cardType?.let {
                                if (it.legacy) {
                                    LegacyCardChip()
                                }
                            }
                            if (card.cardValid != null && !card.cardValid) {
                                InvalidCardChip()
                            }
                        }
                        Text(text = "卡名: ${card.name}")
                        Text(text = "电话: ${card.keys?.joinToString()}")
                        cardType?.let {
                            Text(text = "起充价格: ${it.price.displayStr}")
                            Text(text = "折扣: ${it.discount}")
                            Text(text = "新老卡: ${if (it.legacy) "老卡" else "新卡"}")
                        }
                        HorizontalDivider()
                    }
                }
            }

        }
    }
}

@Composable
fun ChooseCardType(modifier: Modifier, onSelectCardType: (CardType) -> Unit) {
    val cardInfoList = remember { mutableStateListOf<CardType>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            FSDB.cardTypeFlow.collectLatest {
                cardInfoList.clear()
                cardInfoList.addAll(it)
            }
        }
    }
    Column(modifier = modifier) {
        Text(text = "选择卡类型：")
        CardTypeList(modifier = Modifier, cardInfoList = cardInfoList, onSelectCardType)
    }
}
