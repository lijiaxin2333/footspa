package com.example.footspa.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.footspa.MainApplication

@Database(
    entities = [
        MoneyNode::class,
        Bill::class,
        MassageService::class
    ],
    version = 1
)
@TypeConverters(FSDBTypeConverters::class)
abstract class FSDB : RoomDatabase() {

    companion object {

        suspend fun insertCustomer(
            name: String,
            phoneNumbers: List<String> = emptyList()
        ) {
            val moneyNode = buildMoneyNode {
                this.name = name
                type = MoneyNodeType.Customer
                keys = phoneNumbers
            }
            db.dao.insertMoneyNode(moneyNode)
        }

        suspend fun insertThirdParty(name: String) {
            val moneyNode = buildMoneyNode {
                this.name = name
                type = MoneyNodeType.Third
            }
            db.dao.insertMoneyNode(moneyNode)
        }

        suspend fun insertEmployer(
            name: String,
            phoneNumbers: List<String> = emptyList()
        ) {
            val moneyNode = buildMoneyNode {
                this.name = name
                type = MoneyNodeType.Employer
                keys = phoneNumbers
            }
            db.dao.insertMoneyNode(moneyNode)
        }

        suspend fun insertEmployee(
            name: String,
            phoneNumbers: List<String> = emptyList()
        ) {
            val moneyNode = buildMoneyNode {
                this.name = name
                type = MoneyNodeType.Employee
                keys = phoneNumbers
            }
            db.dao.insertMoneyNode(moneyNode)
        }

        suspend fun insertCard(
            name: String,
            cardLevel: String
        ) {

        }


        private val sqlist = listOf(
            SQLConst.UNIQUE_INDEX_TYPE_OUTSIDE,
            SQLConst.UNIQUE_INDEX_TYPE_PUBLIC
        )

        private val cb = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                sqlist.forEach { db.execSQL(it) }
            }
        }

        private val db by lazy {
            Room.databaseBuilder(
                context = MainApplication.instance,
                klass = FSDB::class.java,
                name = "fengshedb"
            )
                .addCallback(cb)
                .build()
        }
    }

    abstract val dao: FSDao

}