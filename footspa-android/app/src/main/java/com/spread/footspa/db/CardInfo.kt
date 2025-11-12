package com.spread.footspa.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = SQLConst.TABLE_NAME_CARD_INFO)
data class CardInfo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "price") val price: BigDecimal,
    @ColumnInfo(name = "discount") val discount: String,
    @ColumnInfo(name = "legacy") val legacy: Boolean
)
