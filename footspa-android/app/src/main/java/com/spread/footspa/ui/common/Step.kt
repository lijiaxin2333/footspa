package com.spread.footspa.ui.common

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

typealias StepContent = @Composable (onFinished: () -> Unit) -> Unit

@Composable
fun StepColumn(
    stepsBuilder: MutableList<StepContent>.() -> Unit
) {
    val steps = buildList(stepsBuilder)
    var maxStepIndex by remember { mutableIntStateOf(0) }

    Column {
        steps.forEachIndexed { index, step ->
            if (index <= maxStepIndex) {
                step {
                    if (maxStepIndex == index && maxStepIndex < steps.lastIndex) {
                        // next step
                        maxStepIndex++
                    }
                }
            }
        }
    }
}