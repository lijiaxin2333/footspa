package com.spread.footspa.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import com.spread.footspa.db.FSDB
import com.spread.footspa.db.MoneyNode
import com.spread.footspa.db.MoneyNodeType
import com.spread.footspa.db.PEOPLE_TYPE_CUSTOMER
import com.spread.footspa.db.PEOPLE_TYPE_EMPLOYEE
import com.spread.footspa.db.PEOPLE_TYPE_EMPLOYER
import com.spread.footspa.db.displayStr
import com.spread.footspa.db.queryMoneyNode
import com.spread.footspa.ui.common.MoneyNodeSearchInputSimple
import com.spread.footspa.ui.common.PhoneNumberInput
import com.spread.footspa.ui.common.SelectOneOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun PeopleScreen(modifier: Modifier = Modifier) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { 2 }
    )
    val scope = rememberCoroutineScope()
    HorizontalPager(
        modifier = modifier,
        state = pagerState,
    ) { page ->
        if (page == 0) {
            var nodeType by remember { mutableStateOf(MoneyNodeType.None) }
            val options = listOf(
                PEOPLE_TYPE_EMPLOYER,
                PEOPLE_TYPE_EMPLOYEE,
                PEOPLE_TYPE_CUSTOMER
            )
            val phoneNumbers = remember { mutableStateListOf("") }
            var nameInput by remember { mutableStateOf("") }
            Column(modifier = modifier) {
                Text(text = "人员类型")
                SelectOneOptions(
                    modifier = Modifier.wrapContentSize(),
                    options = options,
                    onOptionSelected = {
                        nodeType = when (it) {
                            PEOPLE_TYPE_EMPLOYER -> MoneyNodeType.Employer
                            PEOPLE_TYPE_EMPLOYEE -> MoneyNodeType.Employee
                            PEOPLE_TYPE_CUSTOMER -> MoneyNodeType.Customer
                            else -> MoneyNodeType.None
                        }
                    }
                )
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = {
                        nameInput = it
                    },
                    label = { Text("名字") },
                    singleLine = true
                )
                Text(text = "联系电话:")
                LazyColumn(modifier = Modifier.weight(1f)) {
                    item {
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
                    }
                }
                Button(onClick = {
                    scope.launch(Dispatchers.IO) {
                        val name = nameInput
                        if (name.isNotBlank() && phoneNumbers.isNotEmpty()) {
                            when (nodeType) {
                                MoneyNodeType.Employee -> FSDB.insertEmployee(name, phoneNumbers)
                                MoneyNodeType.Employer -> FSDB.insertEmployer(name, phoneNumbers)
                                MoneyNodeType.Customer -> FSDB.insertCustomer(name, phoneNumbers)
                                else -> {}
                            }
                        }
                    }
                }) { Text(text = "确定") }
            }
        } else {
            val peopleList = remember { mutableStateListOf<MoneyNode>() }
            LaunchedEffect(Unit) {
                FSDB.moneyNodeFlow.collect { moneyNodes ->
                    val list = moneyNodes.filter {
                        it.type in setOf(
                            MoneyNodeType.Employee,
                            MoneyNodeType.Employer,
                            MoneyNodeType.Customer
                        )
                    }
                    peopleList.clear()
                    peopleList.addAll(list)
                }
            }
            val filteredList = remember { mutableStateListOf<MoneyNode>() }
            var inSearch by remember { mutableStateOf(false) }
            Column(modifier = modifier) {
                val queryState = remember { TextFieldState() }
                MoneyNodeSearchInputSimple(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    textFieldState = queryState,
                    label = "搜人",
                    onSearch = { query ->
                        if (query.isBlank()) {
                            filteredList.clear()
                            inSearch = false
                        } else {
                            scope.launch(Dispatchers.IO) {
                                inSearch = true
                                val res = queryMoneyNode(query, peopleList)
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
                            else filteredList.takeIf { it.isNotEmpty() } ?: peopleList
                    ) { people ->
                        Column {
                            Text(text = "姓名: ${people.name}")
                            Text(text = "人员类型: ${people.type.displayStr}")
                            Text(text = "电话: ${people.keys?.joinToString()}")
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}