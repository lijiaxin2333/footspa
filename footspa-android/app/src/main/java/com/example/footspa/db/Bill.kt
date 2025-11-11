package com.example.footspa.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal

@Entity(tableName = "bills_all")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "date") val date: Long,
    @ColumnInfo("money_from") val fromId: Long,
    @ColumnInfo("money_to") val toId: Long,
    @ColumnInfo("money") val money: BigDecimal,
    @ColumnInfo("valid") val valid: Boolean = true,
    @ColumnInfo("tags") val tags: List<String>,
    @ColumnInfo("remark") val remark: String,
    @ColumnInfo("service") val service: Long,
    @ColumnInfo("servant") val servant: Long
)