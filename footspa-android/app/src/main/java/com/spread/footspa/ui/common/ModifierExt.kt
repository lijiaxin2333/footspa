package com.spread.footspa.ui.common

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp

fun Modifier.underline(strokeWidth: Dp, color: Color) = this then Modifier.drawBehind {
    // in draw scope, you can call conversion functions directly
    val strokeWidthPx = strokeWidth.toPx()
    val width = size.width
    val height = size.height - strokeWidthPx / 2

    drawLine(
        color = color,
        start = Offset(x = 0f, y = height),
        end = Offset(x = width, y = height),
        strokeWidth = strokeWidthPx
    )
}
