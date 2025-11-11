package com.example.footspa.db

import androidx.room.Dao
import androidx.room.Insert

@Dao
abstract class FSDao {
    @Insert
    abstract suspend fun insertMoneyNode(vararg node: MoneyNode): List<Long>

    @Insert
    abstract suspend fun insertMassageService(vararg service: MassageService): List<Long>

    @Insert
    abstract suspend fun insertBill(vararg bill: Bill): List<Long>

    suspend fun checkDBHealthy(): Boolean {
        return true
    }
}