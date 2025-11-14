package com.spread.footspa.ui.common

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun PhoneNumberInput(
    modifier: Modifier,
    phoneNumbers: List<String>,
    onPhoneNumberChange: (String, Int) -> Unit,
    onClickNewPhoneNumber: () -> Unit
) {

    Column(modifier = modifier) {
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
                trailingIcon = {
                    Icon(
                        modifier = Modifier.clickable {
                            onPhoneNumberChange("", index)
                        },
                        imageVector = Icons.Default.Clear,
                        contentDescription = "clear"
                    )
                },
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
