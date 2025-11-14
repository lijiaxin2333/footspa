package com.spread.footspa.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.frosch2010.fuzzywuzzy_kotlin.FuzzySearch
import java.math.BigDecimal


@Entity(tableName = SQLConst.TABLE_NAME_MASSAGE_SERVICE)
data class MassageService(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "desc") val desc: String?,
    @ColumnInfo(name = "price") val price: BigDecimal,
    @ColumnInfo(name = "create_time") val createTime: Long
)

fun queryMassageService(
    query: String,
    services: List<MassageService>,
    minScore: Int = 1,
    top: Int = 10
): List<MassageService> {
    val nameCandidates = FuzzySearch.extractAll(
        query = query,
        choices = services.map { it.name }
    )
    val descCandidates = FuzzySearch.extractAll(
        query = query,
        choices = services.map { it.desc ?: "" }
    )
    val priceCandidates = FuzzySearch.extractAll(
        query = query,
        choices = services.map { it.price.toPlainString() }
    )
    val allCandidatesDuplicated =
        (nameCandidates + descCandidates + priceCandidates).sortedByDescending { it.score }
    val finalRes = mutableListOf<MassageService>()
    val dedup = hashSetOf<Int>()
    for (candidate in allCandidatesDuplicated) {
        val index = candidate.index
        val node = services[index]
        if (dedup.contains(index) || finalRes.contains(node) || candidate.score < minScore) {
            continue
        }
        finalRes.add(node)
        if (finalRes.size >= top) {
            break
        }
    }
    return finalRes
}
