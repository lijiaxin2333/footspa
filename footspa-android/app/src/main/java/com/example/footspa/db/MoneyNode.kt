package com.example.footspa.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "money_node")
data class MoneyNode(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "type") val type: MoneyNodeType,
    @ColumnInfo(name = "keys") val keys: List<String>?
)
