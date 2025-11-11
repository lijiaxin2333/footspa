package com.example.footspa.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "money_node")
data class MoneyNode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: MoneyNodeType,
    @ColumnInfo(name = "keys") val keys: List<String>?,
    @ColumnInfo(name = "card_price") val cardPrice: BigDecimal?,
    @ColumnInfo(name = "card_valid") val cardValid: Boolean?,
    @ColumnInfo(name = "card_legacy") val isCardLegacy: Boolean?
)

class MoneyNodeBuilder {
    var name = "null"
    var type = MoneyNodeType.None
    var keys: List<String>? = null
    var cardPrice: BigDecimal? = null
    var cardValid: Boolean? = null
    var isCardLegacy: Boolean? = null
}

inline fun buildMoneyNode(init: MoneyNodeBuilder.() -> Unit): MoneyNode {
    val builder = MoneyNodeBuilder().apply(init)
    return MoneyNode(
        name = builder.name,
        type = builder.type,
        keys = builder.keys,
        cardPrice = builder.cardPrice,
        cardValid = builder.cardValid,
        isCardLegacy = builder.isCardLegacy
    )
}