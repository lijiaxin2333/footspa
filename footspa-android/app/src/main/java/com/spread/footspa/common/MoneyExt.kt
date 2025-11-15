package com.spread.footspa.common

import java.math.BigDecimal

val BigDecimal.displayStr: String
    get() = toPlainString() + "元"