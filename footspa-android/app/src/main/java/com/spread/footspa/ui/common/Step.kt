package com.spread.footspa.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

typealias StepContent = @Composable (onFinished: () -> Unit) -> Unit

@Composable
fun StepColumn(
    stepsBuilder: MutableList<StepContent>.() -> Unit
) {
    val steps = buildList(stepsBuilder)
    var currentIndex by remember { mutableStateOf(0) }

    Column {
        steps.forEachIndexed { index, step ->
            if (index <= currentIndex) {
                step {
                    // 当 step 完成后，展示下一个
                    if (currentIndex == index && currentIndex < steps.lastIndex) {
                        currentIndex++
                    }
                }
            }
        }
    }
}