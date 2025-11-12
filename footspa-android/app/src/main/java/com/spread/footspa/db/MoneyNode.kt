package com.spread.footspa.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = SQLConst.TABLE_NAME_MONEY_NODE)
data class MoneyNode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: MoneyNodeType,
    @ColumnInfo(name = "keys") val keys: List<String>?,
    @ColumnInfo(name = "card_id") val cardId: Long?,
    @ColumnInfo(name = "card_valid") val cardValid: Boolean?,
) {
    fun containsKey(key: String): Boolean = keys?.contains(key) ?: false
}

class MoneyNodeBuilder {
    var name = "null"
    var type = MoneyNodeType.None
    var keys: List<String>? = null
    var cardId: Long? = null
    var cardValid: Boolean? = null
}

inline fun buildMoneyNode(init: MoneyNodeBuilder.() -> Unit): MoneyNode {
    val builder = MoneyNodeBuilder().apply(init)
    if (builder.type == MoneyNodeType.None) {
        throw RuntimeException("money node type is none")
    }
    return MoneyNode(
        name = builder.name,
        type = builder.type,
        keys = builder.keys,
        cardId = builder.cardId,
        cardValid = builder.cardValid,
    )
}