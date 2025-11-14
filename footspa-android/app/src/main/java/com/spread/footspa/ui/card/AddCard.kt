package com.spread.footspa.ui.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
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
import com.spread.footspa.db.CardType
import com.spread.footspa.db.FSDB
import com.spread.footspa.ui.common.PhoneNumberInput
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun AddCardScreen(
    modifier: Modifier = Modifier
) {
    var selectedCardInfo: CardType? by remember { mutableStateOf(null) }
    val phoneNumbers = remember { mutableStateListOf("") }
    val scope = rememberCoroutineScope()
    LazyColumn(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
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
}

@Composable
fun ChooseCardType(modifier: Modifier, onSelectCardType: (CardType) -> Unit) {
    val cardInfoList = remember { mutableStateListOf<CardType>() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            FSDB.cardInfoFlow.collectLatest {
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
