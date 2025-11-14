package com.spread.footspa.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.input.TextFieldState
import androidx.compose.foundation.text.input.clearText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.isTraversalGroup
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.traversalIndex

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MoneyNodeSearchInputSimple(
    modifier: Modifier = Modifier,
    label: String,
    textFieldState: TextFieldState,
    onSearch: (String) -> Unit
) {
    Box(
        modifier
            .semantics { isTraversalGroup = true }
    ) {
        SearchBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .semantics { traversalIndex = 0f },
            inputField = {
                SearchBarDefaults.InputField(
                    query = textFieldState.text.toString(),
                    onQueryChange = {
                        textFieldState.edit { replace(0, length, it) }
                        onSearch(textFieldState.text.toString())
                    },
                    onSearch = {
                        onSearch(textFieldState.text.toString())
                    },
                    trailingIcon = {
                        Icon(
                            modifier = Modifier.clickable {
                                textFieldState.clearText()
                                onSearch(textFieldState.text.toString())
                            },
                            imageVector = Icons.Default.Clear,
                            contentDescription = "clear"
                        )
                    },
                    expanded = false,
                    onExpandedChange = {},
                    placeholder = { Text(label) }
                )
            },
            expanded = false,
            onExpandedChange = {},
        ) {
        }
    }
}