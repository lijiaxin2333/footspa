package com.spread.footspa.ui.common

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.spread.footspa.db.MoneyNodeType
import com.spread.footspa.db.displayStr

@Composable
fun LegacyCardChip() {
    AssistChip(
        modifier = Modifier.padding(5.dp),
        onClick = {},
        enabled = false,
        label = { Text(text = "老卡") },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(IconConstants.ICON_SIZE_NORMAL),
                imageVector = Icons.Filled.Warning,
                contentDescription = "legacy card"
            )
        }
    )
}

@Composable
fun InvalidCardChip() {
    AssistChip(
        modifier = Modifier.padding(5.dp),
        onClick = {},
        enabled = false,
        label = { Text(text = "无效卡") },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(IconConstants.ICON_SIZE_NORMAL),
                imageVector = Icons.Filled.Warning,
                contentDescription = "invalid card"
            )
        }
    )
}

@Composable
fun NewNodeChip(type: MoneyNodeType) {
    AssistChip(
        modifier = Modifier.padding(5.dp),
        onClick = {},
        enabled = false,
        label = { Text(text = "新${type.displayStr}") },
        leadingIcon = {
            Icon(
                modifier = Modifier.size(IconConstants.ICON_SIZE_NORMAL),
                imageVector = Icons.Filled.Add,
                contentDescription = "new node"
            )
        }
    )
}