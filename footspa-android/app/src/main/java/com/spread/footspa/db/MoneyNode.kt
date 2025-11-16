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
    @ColumnInfo(name = "card_type_id") val cardTypeId: Long?,
    @ColumnInfo(name = "card_valid") val cardValid: Boolean?,
) {
    fun containsKey(key: String): Boolean = keys?.contains(key) ?: false

    override fun equals(other: Any?): Boolean {
        return other is MoneyNode
                && this.id == other.id
                && this.type == other.type
                && this.keys == other.keys
                && this.cardTypeId == other.cardTypeId
                && this.cardValid == other.cardValid
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + (cardTypeId?.hashCode() ?: 0)
        result = 31 * result + (cardValid?.hashCode() ?: 0)
        result = 31 * result + name.hashCode()
        result = 31 * result + type.hashCode()
        result = 31 * result + (keys?.hashCode() ?: 0)
        return result
    }

    override fun toString(): String {
        return "MoneyNode(id=$id, cardTypeId=$cardTypeId, cardValid=$cardValid, name='$name', type=$type, keys=$keys)"
    }
}

class MoneyNodeBuilder {
    var name = "null"
    var type = MoneyNodeType.None
    var keys: List<String>? = null
    var cardTypeId: Long? = null
    var cardValid: Boolean? = null
}

inline fun buildMoneyNode(init: MoneyNodeBuilder.() -> Unit): MoneyNode {
    val builder = MoneyNodeBuilder().apply(init)
    if (builder.type == MoneyNodeType.None) {
        throw IllegalStateException("money node type is none")
    }
    return MoneyNode(
        name = builder.name,
        type = builder.type,
        keys = builder.keys,
        cardTypeId = builder.cardTypeId,
        cardValid = builder.cardValid,
    )
}

