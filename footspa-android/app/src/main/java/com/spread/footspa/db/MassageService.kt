package com.spread.footspa.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.math.BigDecimal


@Entity(tableName = SQLConst.TABLE_NAME_MASSAGE_SERVICE)
data class MassageService(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "desc") val desc: String?,
    @ColumnInfo(name = "price") val price: BigDecimal,
    @ColumnInfo(name = "create_time") val createTime: Long
) {
    override fun equals(other: Any?): Boolean {
        return other is MassageService
                && this.id == other.id
                && this.name == other.name
                && this.desc == other.desc
                && this.createTime == other.createTime
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + createTime.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (desc?.hashCode() ?: 0)
        result = 31 * result + price.hashCode()
        return result
    }
}

