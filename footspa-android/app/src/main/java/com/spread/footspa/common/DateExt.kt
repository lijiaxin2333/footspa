package com.spread.footspa.common

import java.time.Instant
import java.time.ZoneId

fun isSameDay(time1: Long, time2: Long): Boolean {
    val zone = ZoneId.systemDefault()
    return Instant.ofEpochMilli(time1).atZone(zone).toLocalDate() == Instant.ofEpochMilli(time2)
        .atZone(zone).toLocalDate()
}