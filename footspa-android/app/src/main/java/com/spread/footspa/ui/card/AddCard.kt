package com.spread.footspa.ui.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.spread.footspa.db.CardInfo
import com.spread.footspa.db.FSDB
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun AddCardScreen(
    modifier: Modifier = Modifier
) {
    var selectedCardInfo: CardInfo? by remember { mutableStateOf(null) }
    var phoneNumbers by remember { mutableStateOf(listOf("")) }
    LazyColumn(modifier = modifier) {
        item {
            ChooseCardType(modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)) {
                selectedCardInfo = it
            }
        }
        item {
            PhoneNumberInput(
                modifier = Modifier.fillMaxWidth().wrapContentHeight(),
                phoneNumbers = phoneNumbers,
                onPhoneNumberChange = { newNum, index ->
                    phoneNumbers = phoneNumbers.toMutableList().also { it[index] = newNum }
                },
                onClickNewPhoneNumber = {
                    phoneNumbers = phoneNumbers + ""
                }
            )
        }
    }
}

@Composable
fun ChooseCardType(modifier: Modifier, onSelectCardType: (CardInfo) -> Unit) {
    val cardInfoList = remember { mutableStateListOf<CardInfo>() }
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

@Composable
fun PhoneNumberInput(
    modifier: Modifier,
    phoneNumbers: List<String>,
    onPhoneNumberChange: (String, Int) -> Unit,
    onClickNewPhoneNumber: () -> Unit
) {

    Column(modifier = modifier) {
        Text(text = "联系电话:")
        phoneNumbers.forEachIndexed { index, value ->
            OutlinedTextField(
                value = value,
                onValueChange = { newValue ->
                     onPhoneNumberChange(newValue, index)
                },
                label = { Text("电话 ${index + 1}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        IconButton(
            onClick = onClickNewPhoneNumber,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "添加输入框")
        }
    }
}