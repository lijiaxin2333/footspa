package com.spread.footspa.db

import androidx.room.TypeConverter
import com.spread.footspa.common.json
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.JsonUnquotedLiteral
import java.math.BigDecimal

class FSDBTypeConverters {

    @TypeConverter
    fun stringToBigDecimal(value: String): BigDecimal {
        return json.decodeFromString<JsonPrimitive>(value).content.toBigDecimal()
    }

    @OptIn(ExperimentalSerializationApi::class)
    @TypeConverter
    fun bigDecimalToString(value: BigDecimal): String {
        return json.encodeToString(JsonUnquotedLiteral(value.toPlainString()))
    }

    @TypeConverter
    fun stringToList(value: String): List<String> {
        return json.decodeFromString(value)
    }

    @TypeConverter
    fun listToString(value: List<String>): String {
        return json.encodeToString(value)
    }

    @TypeConverter
    fun stringToMoneyNodeType(value: String): MoneyNodeType {
        return MoneyNodeType.entries.firstOrNull { it.str == value } ?: MoneyNodeType.None
    }

    @TypeConverter
    fun moneyNodeTypeToString(value: MoneyNodeType): String {
        return value.str
    }

    @TypeConverter
    fun stringToBillType(value: String): BillType {
        return BillType.entries.firstOrNull { it.str == value } ?: BillType.None
    }

    @TypeConverter
    fun billTypeToString(value: BillType): String {
        return value.str
    }

}