package com.spread.footspa.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.frosch2010.fuzzywuzzy_kotlin.FuzzySearch
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.math.BigDecimal
import java.util.Locale


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

suspend fun CoroutineScope.queryMassageService(
    query: String,
    services: List<MassageService>,
    minScore: Int = 1,
    top: Int = 10
): List<MassageService> {
    val nameCandidates = async(Dispatchers.IO) {
        FuzzySearch.extractAll(
            query = query,
            choices = services.map { it.name }
        )
    }
    val descCandidates = async(Dispatchers.IO) {
        FuzzySearch.extractAll(
            query = query,
            choices = services.map { it.desc ?: "" }
        )
    }
    val priceCandidates = async(Dispatchers.IO) {
        FuzzySearch.extractAll(
            query = query,
            choices = services.map { it.price.toPlainString() }
        )
    }
    val pinyinCandidates = async(Dispatchers.IO) {
        FuzzySearch.extractAll(
            query = query,
            choices = services.map { Pinyin.toPinyin(it.name, "").lowercase(Locale.getDefault()) }
        )
    }
    val allCandidatesDuplicated =
        (nameCandidates.await() + descCandidates.await() + priceCandidates.await() + pinyinCandidates.await())
            .sortedByDescending { it.score }
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
