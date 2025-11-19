package com.spread.footspa.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

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

    @Query("SELECT * FROM ${SQLConst.TABLE_NAME_MONEY_NODE}")
    abstract suspend fun getAllMoneyNodes(): List<MoneyNode>

    @Query("SELECT * FROM ${SQLConst.TABLE_NAME_MASSAGE_SERVICE}")
    abstract suspend fun getAllMassageServices(): List<MassageService>

    @Query("SELECT * FROM ${SQLConst.TABLE_NAME_BILL}")
    abstract suspend fun getAllBills(): List<Bill>

    @Query("SELECT * FROM ${SQLConst.TABLE_NAME_CARD_TYPE}")
    abstract suspend fun getAllCardTypes(): List<CardType>

    @Query("SELECT EXISTS(SELECT * FROM ${SQLConst.TABLE_NAME_MONEY_NODE} WHERE id = :id)")
    abstract suspend fun isMoneyNodeExists(id: Long): Boolean

    @Query("SELECT EXISTS(SELECT * FROM ${SQLConst.TABLE_NAME_MASSAGE_SERVICE} WHERE id = :id)")
    abstract suspend fun isMassageServiceExists(id: Long): Boolean

    @Query("SELECT EXISTS(SELECT * FROM ${SQLConst.TABLE_NAME_BILL} WHERE id = :id)")
    abstract suspend fun isBillExists(id: Long): Boolean

    @Query("SELECT EXISTS(SELECT * FROM ${SQLConst.TABLE_NAME_CARD_TYPE} WHERE id = :id)")
    abstract suspend fun isCardTypeExists(id: Long): Boolean

    @Query("SELECT * FROM ${SQLConst.TABLE_NAME_MONEY_NODE} WHERE type = 'public' LIMIT 1")
    abstract suspend fun getPublic(): MoneyNode

    @Query("SELECT * FROM ${SQLConst.TABLE_NAME_MONEY_NODE} WHERE type = 'outside' LIMIT 1")
    abstract suspend fun getOutside(): MoneyNode

    @Query("SELECT * FROM ${SQLConst.TABLE_NAME_MONEY_NODE} WHERE id = :id LIMIT 1")
    abstract suspend fun getMoneyNode(id: Long): MoneyNode?

    @Query("SELECT * FROM ${SQLConst.TABLE_NAME_BILL} WHERE id = :id LIMIT 1")
    abstract suspend fun getBill(id: Long): Bill?


    suspend fun checkDBHealthy(): Boolean {
        // only 1 public
        val moneyNodes = FSDB.getAllMoneyNodes()
        if (!moneyNodes.ensureOneExists(MoneyNodeType.Public)) {
            return false
        }
        // only 1 outside
        if (!moneyNodes.ensureOneExists(MoneyNodeType.Outside)) {
            return false
        }
        return true
    }

    private val initMutex = Mutex()

    suspend fun initDBIfNeeded() {
        initMutex.withLock {
            val moneyNodes = FSDB.getAllMoneyNodes()
            if (moneyNodes.isEmpty()) {
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
    }

    private fun List<MoneyNode>.ensureOneExists(type: MoneyNodeType): Boolean =
        filter { it.type == type }.size == 1
}