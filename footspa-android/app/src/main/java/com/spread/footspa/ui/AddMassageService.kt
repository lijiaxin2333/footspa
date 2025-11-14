package com.spread.footspa.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spread.footspa.db.FSDB
import com.spread.footspa.db.MassageService
import com.spread.footspa.ui.common.MoneyExpr
import com.spread.footspa.ui.common.MoneyInput2
import com.spread.footspa.ui.common.MoneyInputState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Composable
fun AddMassageService(modifier: Modifier = Modifier) {
    var showAddServiceDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    val serviceList = remember { mutableStateListOf<MassageService>() }

    LaunchedEffect(Unit) {
        scope.launch(Dispatchers.IO) {
            FSDB.massageServiceFlow.collectLatest {
                serviceList.clear()
                serviceList.addAll(it)
            }
        }
    }

    Column(modifier = modifier) {

        Button(
            onClick = {
                showAddServiceDialog = true
            }
        ) {
            Text(text = "新增项目")
        }

        ServiceList(modifier = Modifier, serviceList = serviceList)
    }

    if (showAddServiceDialog) {
        AddServiceDialog(
            onDismiss = {
                showAddServiceDialog = false
            },
            onConfirm = {
                scope.launch(Dispatchers.IO) {
                    val ids = FSDB.insertMassageService(it)
                    if (ids.size == 1) {
                    }
                }
                showAddServiceDialog = false
            }
        )
    }
}

@Composable
fun ServiceList(modifier: Modifier, serviceList: List<MassageService>) {
    LazyColumn(modifier = modifier) {
        items(serviceList) {
            Column(modifier = Modifier.padding(vertical = 5.dp)) {
                Text("项目: ${it.name}")
                Text("价格: ${it.price}")
                Text("描述: ${it.desc}")
            }
        }
    }
}

@Composable
fun AddServiceDialog(
    onDismiss: () -> Unit,
    onConfirm: (MassageService) -> Unit
) {
    var serviceNameInputText by remember { mutableStateOf("") }
    var serviceDescInputText by remember { mutableStateOf("") }
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
            Text(text = "新增项目 ")
        },
        text = {
            Column {
                OutlinedTextField(
                    value = serviceNameInputText,
                    onValueChange = {
                        serviceNameInputText = it
                    },
                    label = { Text("项目名") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = serviceDescInputText,
                    onValueChange = {
                        serviceDescInputText = it
                    },
                    label = { Text("描述") },
                    singleLine = false
                )
                MoneyExpr(
                    label = "价格",
                    expression = expression,
                    initial = null,
                    value = value,
                    err = err
                )
                MoneyInput2(inputState = moneyInputState)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                value?.let { price ->
                    serviceNameInputText.takeIf { it.isNotBlank() }?.let { name ->
                        val service = MassageService(
                            name = name,
                            desc = serviceDescInputText,
                            price = price,
                            createTime = System.currentTimeMillis()
                        )
                        onConfirm(service)
                    }
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
