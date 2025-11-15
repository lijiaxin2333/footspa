package com.spread.footspa.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.spread.footspa.db.FSDB.Companion.billFlow
import com.spread.footspa.db.FSDB.Companion.massageServiceFlow
import com.spread.footspa.db.FSDB.Companion.moneyNodeFlow
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FSDao {

    @Insert
    abstract suspend fun insertMoneyNode(vararg node: MoneyNode): List<Long>

    @Insert
    abstract suspend fun insertBill(vararg bill: Bill): List<Long>

    @Insert
    abstract suspend fun insertMassageService(vararg service: MassageService): List<Long>

    @Insert
    abstract suspend fun insertCardInfo(vararg cardInfo: CardType): List<Long>

    @Query("SELECT * FROM ${SQLConst.TABLE_NAME_MONEY_NODE}")
    abstract fun listenToAllMoneyNodes(): Flow<List<MoneyNode>>

    @Query("SELECT * FROM ${SQLConst.TABLE_NAME_MASSAGE_SERVICE}")
    abstract fun listenToAllMassageServices(): Flow<List<MassageService>>

    @Query("SELECT * FROM ${SQLConst.TABLE_NAME_BILL}")
    abstract fun listenToAllBills(): Flow<List<Bill>>

    @Query("SELECT * FROM ${SQLConst.TABLE_NAME_CARD_TYPE}")
    abstract fun listenToAllCardTypes(): Flow<List<CardType>>


    fun checkDBHealthy(): Boolean {
        // only 1 public
        val moneyNodes = moneyNodeFlow.value
        if (!moneyNodes.ensureOneExists(MoneyNodeType.Public)) {
            return false
        }
        // only 1 outside
        if (!moneyNodes.ensureOneExists(MoneyNodeType.Outside)) {
            return false
        }
        return true
    }

    suspend fun initDBIfNeeded() {
        if (moneyNodeFlow.value.isEmpty() && massageServiceFlow.value.isEmpty() && billFlow.value.isEmpty()) {
            val publicNode = buildMoneyNode {
                name = "public"
                type = MoneyNodeType.Public
            }
            val outsideNode = buildMoneyNode {
                name = "outside"
                type = MoneyNodeType.Outside
            }
            insertMoneyNode(publicNode, outsideNode)
        } else if (!checkDBHealthy()) {
            throw IllegalStateException("db is unhealthy on init!")
        }
    }

    private fun List<MoneyNode>.ensureOneExists(type: MoneyNodeType): Boolean =
        filter { it.type == type }.size == 1
}