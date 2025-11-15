package com.spread.footspa.db

import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spread.footspa.MainApplication
import com.spread.footspa.asApplicationStateFlow
import com.spread.footspa.runOnApplicationScope

@Database(
    entities = [
        MoneyNode::class,
        Bill::class,
        MassageService::class,
        CardType::class
    ],
    version = 1
)
@TypeConverters(FSDBTypeConverters::class)
abstract class FSDB : RoomDatabase() {

    companion object {

        private val cb = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                Log.d("Spread", "db oncreate")
                sqlist.forEach { db.execSQL(it) }
                Log.d("Spread", "db oncreate finish")
            }

            override fun onOpen(db: SupportSQLiteDatabase) {
                db.beginTransaction()
                try {
                    if (!db.isReadOnly) {
                        runOnApplicationScope {
                            dao.initDBIfNeeded()
                        }
                    }
                    db.setTransactionSuccessful()
                } finally {
                    db.endTransaction()
                }
            }
        }

        val db by lazy {
            Room.databaseBuilder(
                context = MainApplication.instance,
                klass = FSDB::class.java,
                name = "fengshedb"
            ).addCallback(cb).setJournalMode(JournalMode.TRUNCATE).build()
        }

        val dao: FSDao get() = db.dao()

        val moneyNodeFlow = dao.listenToAllMoneyNodes().asApplicationStateFlow(emptyList())

        val massageServiceFlow =
            dao.listenToAllMassageServices().asApplicationStateFlow(emptyList())

        val billFlow = dao.listenToAllBills().asApplicationStateFlow(emptyList())

        val cardTypeFlow = dao.listenToAllCardTypes().asApplicationStateFlow(emptyList())

        suspend fun getAllMoneyNodes(): List<MoneyNode> {
            return dao.getAllMoneyNodes()
        }

        suspend fun getAllMassageServices(): List<MassageService> {
            return dao.getAllMassageServices()
        }

        suspend fun getAllBills(): List<Bill> {
            return dao.getAllBills()
        }

        suspend fun getAllCardTypes(): List<CardType> {
            return dao.getAllCardTypes()
        }

        private val sqlist = listOf(
            SQLConst.UNIQUE_INDEX_TYPE_OUTSIDE,
            SQLConst.UNIQUE_INDEX_TYPE_PUBLIC
        )

        suspend fun findCardType(card: MoneyNode): CardType? {
            if (card.cardTypeId == null) {
                return null
            }
            val type = getAllCardTypes().find { it.id == card.cardTypeId }
            return type
        }


        suspend fun insertMoneyNode(vararg moneyNode: MoneyNode) {
            dao.insertMoneyNode(*moneyNode)
        }

        suspend fun insertCustomer(
            name: String,
            phoneNumbers: List<String> = emptyList()
        ) {
            val moneyNode = buildMoneyNode {
                this.name = name
                type = MoneyNodeType.Customer
                keys = phoneNumbers
            }
            dao.insertMoneyNode(moneyNode)
        }

        suspend fun insertThirdParty(name: String) {
            val moneyNode = buildMoneyNode {
                this.name = name
                type = MoneyNodeType.Third
            }
            dao.insertMoneyNode(moneyNode)
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
            dao.insertMoneyNode(moneyNode)
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
            dao.insertMoneyNode(moneyNode)
        }

        suspend fun insertCard(
            name: String,
            phoneNumbers: List<String>,
            typeId: Long
        ): List<Long> {
            val moneyNode = buildMoneyNode {
                this.name = name
                type = MoneyNodeType.Card
                keys = phoneNumbers
                cardTypeId = typeId
                cardValid = true
            }
            return dao.insertMoneyNode(moneyNode)
        }

        suspend fun insertCardInfo(vararg cardInfo: CardType) = dao.insertCardInfo(*cardInfo)

        suspend fun insertMassageService(vararg services: MassageService) =
            dao.insertMassageService(*services)

    }

    abstract fun dao(): FSDao

}