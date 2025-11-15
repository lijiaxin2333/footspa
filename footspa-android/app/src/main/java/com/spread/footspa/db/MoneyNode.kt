package com.spread.footspa.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.frosch2010.fuzzywuzzy_kotlin.FuzzySearch
import com.github.promeg.pinyinhelper.Pinyin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.util.Locale

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
        throw RuntimeException("money node type is none")
    }
    return MoneyNode(
        name = builder.name,
        type = builder.type,
        keys = builder.keys,
        cardTypeId = builder.cardTypeId,
        cardValid = builder.cardValid,
    )
}

/**
 * Fuzzle search MoneyNodes by name and keys
 */
suspend fun CoroutineScope.queryMoneyNode(
    query: String,
    nodes: List<MoneyNode>,
    minScore: Int = 1,
    top: Int = 10,
    types: Set<MoneyNodeType> = emptySet()
): List<MoneyNode> {
    val thisTypeNodes = if (types.isEmpty()) nodes else nodes.filter { it.type in types }
    val nameCandidates = async(Dispatchers.IO) {
        FuzzySearch.extractAll(
            query = query,
            choices = thisTypeNodes.map { it.name }
        )
    }
    val keyCandidates = async(Dispatchers.IO) {
        FuzzySearch.extractAll(
            query = query,
            choices = thisTypeNodes.map { it.keys?.joinToString() ?: "" }
        )
    }
    val pinyinCandidates = async(Dispatchers.IO) {
        FuzzySearch.extractAll(
            query = query,
            choices = thisTypeNodes.map {
                Pinyin.toPinyin(it.name, "").lowercase(Locale.getDefault())
            }
        )
    }
    val allCandidatesDuplicated =
        (nameCandidates.await() + keyCandidates.await() + pinyinCandidates.await())
            .sortedByDescending { it.score }
    val finalRes = mutableListOf<MoneyNode>()
    val dedup = hashSetOf<Int>()
    for (candidate in allCandidatesDuplicated) {
        val index = candidate.index
        val node = thisTypeNodes[index]
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
