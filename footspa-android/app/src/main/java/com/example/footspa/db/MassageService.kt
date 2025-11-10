package com.example.footspa.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import java.math.BigDecimal


@Entity(tableName = "massage_service")
data class MassageService (
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "price") val price: @Serializable(with = BigDecimalSerializer::class) BigDecimal,
    @ColumnInfo(name = "create_time") val createTime: Long
)