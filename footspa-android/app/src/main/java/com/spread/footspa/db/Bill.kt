package com.spread.footspa.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = SQLConst.TABLE_NAME_BILL)
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo("money_from") val fromId: Long,
    @ColumnInfo("money_to") val toId: Long,
    @ColumnInfo("money") val money: BigDecimal,
    @ColumnInfo("valid") val valid: Boolean,
    @ColumnInfo("tags") val tags: List<String>,
    @ColumnInfo("remark") val remark: String,
    @ColumnInfo("service") val service: Long,
    @ColumnInfo("servant") val servant: Long
)

class BillBuilder {
    var date = System.currentTimeMillis()
    var fromId = 0L
    var toId = 0L
    var money = BigDecimal.ZERO
    var valid = true
    var tags: List<String> = emptyList()
    var remark = ""
    var service = 0L
    var servant = 0L
}

inline fun buildBill(init: BillBuilder.() -> Unit): Bill {
    val builder = BillBuilder().apply(init)
    if (builder.fromId == 0L || builder.toId == 0L || builder.money == BigDecimal.ZERO) {
        throw IllegalStateException("bill invalid")
    }
    return Bill(
        date = builder.date,
        fromId = builder.fromId,
        toId = builder.toId,
        money = builder.money,
        valid = builder.valid,
        tags = builder.tags,
        remark = builder.remark,
        service = builder.service,
        servant = builder.servant
    )
}
