package com.spread.footspa.db

import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
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
                if (!db.isReadOnly) {
                    runOnApplicationScope {
                        dao.initDBIfNeeded()
                    }
                }
            }
        }

        private val db by lazy {
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

        val cardInfoFlow = dao.listenToAllCardInfo().asApplicationStateFlow(emptyList())

        private val sqlist = listOf(
            SQLConst.UNIQUE_INDEX_TYPE_OUTSIDE,
            SQLConst.UNIQUE_INDEX_TYPE_PUBLIC
        )

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

        fun getCardByPhoneNumber(phoneNumber: String): List<MoneyNode> {
            return moneyNodeFlow.value.filter {
                it.type == MoneyNodeType.Card && it.containsKey(phoneNumber)
            }
        }

        fun getCustomerByPhoneNumber(phoneNumber: String): List<MoneyNode> {
            return moneyNodeFlow.value.filter {
                it.type == MoneyNodeType.Customer && it.containsKey(phoneNumber)
            }
        }

        fun getEmployerByPhoneNumber(phoneNumber: String): List<MoneyNode> {
            return moneyNodeFlow.value.filter {
                it.type == MoneyNodeType.Employer && it.containsKey(phoneNumber)
            }
        }

        fun getEmployeeByPhoneNumber(phoneNumber: String): List<MoneyNode> {
            return moneyNodeFlow.value.filter {
                it.type == MoneyNodeType.Employee && it.containsKey(phoneNumber)
            }
        }

    }

    abstract fun dao(): FSDao

}