package com.spread.footspa.db

import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.withTransaction
import androidx.sqlite.db.SupportSQLiteDatabase
import com.frosch2010.fuzzywuzzy_kotlin.FuzzySearch
import com.github.promeg.pinyinhelper.Pinyin
import com.spread.footspa.MainApplication
import com.spread.footspa.asApplicationStateFlow
import com.spread.footspa.runOnApplicationScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import java.math.BigDecimal
import java.util.Locale

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

        private val cb = object : Callback() {
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


        suspend fun insertMoneyNode(vararg moneyNode: MoneyNode): List<Long> {
            return dao.insertMoneyNode(*moneyNode)
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

        suspend fun insertBill(vararg bill: Bill) = dao.insertBill(*bill)

        suspend fun isMoneyNodeExists(node: MoneyNode): Boolean {
            return dao.isMoneyNodeExists(node.id)
        }

        suspend fun isMassageServiceExists(service: MassageService): Boolean {
            return dao.isMassageServiceExists(service.id)
        }

        suspend fun isBillExists(bill: Bill): Boolean {
            return dao.isBillExists(bill.id)
        }

        suspend fun isCardTypeExists(card: CardType): Boolean {
            return dao.isCardTypeExists(card.id)
        }

        suspend fun getPublic(): MoneyNode {
            return dao.getPublic()
        }

        suspend fun getOutside(): MoneyNode {
            return dao.getOutside()
        }

        suspend fun getMoneyNode(id: Long): MoneyNode? {
            return dao.getMoneyNode(id)
        }

        suspend fun CoroutineScope.queryMoneyNode(
            query: String,
            minScore: Int = 1,
            top: Int = 10,
            types: Set<MoneyNodeType> = emptySet(),
            filter: ((MoneyNode) -> Boolean)? = null
        ): List<MoneyNode> {
            val finalRes = mutableListOf<MoneyNode>()
            db.withTransaction {
                val nodes = getAllMoneyNodes()
                val thisTypeNodes =
                    if (types.isEmpty()) nodes else nodes.filter { it.type in types }
                val choices = if (filter == null) thisTypeNodes else thisTypeNodes.filter(filter)
                val nameCandidates = async(Dispatchers.IO) {
                    FuzzySearch.extractAll(
                        query = query,
                        choices = choices.map { it.name }
                    )
                }
                val keyCandidates = async(Dispatchers.IO) {
                    FuzzySearch.extractAll(
                        query = query,
                        choices = choices.map { it.keys?.joinToString() ?: "" }
                    )
                }
                val pinyinCandidates = async(Dispatchers.IO) {
                    FuzzySearch.extractAll(
                        query = query,
                        choices = choices.map {
                            Pinyin.toPinyin(it.name, "").lowercase(Locale.getDefault())
                        }
                    )
                }
                val allCandidatesDuplicated =
                    (nameCandidates.await() + keyCandidates.await() + pinyinCandidates.await())
                        .sortedByDescending { it.score }
                val dedup = hashSetOf<Int>()
                for (candidate in allCandidatesDuplicated) {
                    val index = candidate.index
                    val node = choices[index]
                    if (dedup.contains(index) || finalRes.contains(node) || candidate.score < minScore) {
                        continue
                    }
                    finalRes.add(node)
                    if (finalRes.size >= top) {
                        break
                    }
                }
            }
            return finalRes
        }

        /**
         * max return size: customer's card count + top
         */
        suspend fun CoroutineScope.queryCard(
            query: String,
            customer: MoneyNode,
            minScore: Int = 1,
            top: Int = 10
        ): List<MoneyNode> {
            val finalRes = mutableListOf<MoneyNode>()
            db.withTransaction {
                val nodes = getAllMoneyNodes()
                val choices = nodes.filter { it.type == MoneyNodeType.Card }
                if (choices.isEmpty()) {
                    return@withTransaction
                }
                val bills = getAllBills()
                val customerCandidates = mutableListOf<MoneyNode>()
                for (bill in bills) {
                    if (bill.fromId == customer.id) {
                        val node = choices.find { it.id == bill.toId }
                        if (node != null
                            && node.type == MoneyNodeType.Card
                            && node.cardValid == true
                            && !customerCandidates.contains(node)
                        ) {
                            customerCandidates.add(node)
                        }
                    }
                }
                if (query.isEmpty()) {
                    finalRes.addAll(customerCandidates)
                    return@withTransaction
                }
                val phoneNumberCandidates = async(Dispatchers.IO) {
                    FuzzySearch.extractAll(
                        query = query,
                        choices = customerCandidates.map { it.keys?.joinToString() ?: "" }
                    )
                }
                val cardNameCandidates = async(Dispatchers.IO) {
                    FuzzySearch.extractAll(
                        query = query,
                        choices = customerCandidates.map { it.name }
                    )
                }
                val cardPinyinCandidates = async(Dispatchers.IO) {
                    FuzzySearch.extractAll(
                        query = query,
                        choices = customerCandidates.map {
                            Pinyin.toPinyin(it.name, "").lowercase(Locale.getDefault())
                        }
                    )
                }
                val allCandidatesDuplicated =
                    (phoneNumberCandidates.await() + cardNameCandidates.await() + cardPinyinCandidates.await())
                        .sortedByDescending { it.score }
                val dedup = hashSetOf<Int>()
                for (candidate in allCandidatesDuplicated) {
                    val index = candidate.index
                    val node = customerCandidates[index]
                    if (dedup.contains(index) || finalRes.contains(node) || candidate.score < minScore) {
                        continue
                    }
                    finalRes.add(node)
                    if (finalRes.size >= top) {
                        break
                    }
                }
            }
            return finalRes
        }

        suspend fun CoroutineScope.queryCardWithBalance(
            query: String = "",
            customer: MoneyNode,
            minScore: Int = 1,
            top: Int = 10
        ): Map<MoneyNode, BigDecimal> {
            val finalRes = mutableMapOf<MoneyNode, BigDecimal>()
            db.withTransaction {
                val nodes = getAllMoneyNodes()
                val choices = nodes.filter { it.type == MoneyNodeType.Card }
                if (choices.isEmpty()) {
                    return@withTransaction
                }
                val bills = getAllBills()
                val customerCandidates = mutableMapOf<MoneyNode, BigDecimal>()
                for (bill in bills) {
                    if (bill.fromId == customer.id) {
                        val node = choices.find { it.id == bill.toId }
                        if (node != null && node.type == MoneyNodeType.Card && node.cardValid == true) {
                            val money = customerCandidates[node]
                            if (money == null) {
                                customerCandidates[node] = bill.money
                            } else {
                                customerCandidates[node] = money + bill.money
                            }
                        }
                    }
                }
                val outside = getOutside()
                for (bill in bills) {
                    if (bill.toId != outside.id) {
                        continue
                    }
                    val card = customerCandidates.keys.find { it.id == bill.fromId }
                    if (card != null) {
                        // from card to outside
                        val money = customerCandidates[card]
                            ?: throw IllegalStateException("card not found")
                        customerCandidates[card] = money - bill.money
                    }
                }
                if (query.isEmpty()) {
                    finalRes.putAll(customerCandidates)
                    return@withTransaction
                }
                val entries = customerCandidates.toList()
                val phoneNumberCandidates = async(Dispatchers.IO) {
                    FuzzySearch.extractAll(
                        query = query,
                        choices = entries.map { it.first.keys?.joinToString() ?: "" }
                    )
                }
                val cardNameCandidates = async(Dispatchers.IO) {
                    FuzzySearch.extractAll(
                        query = query,
                        choices = entries.map { it.first.name }
                    )
                }
                val cardPinyinCandidates = async(Dispatchers.IO) {
                    FuzzySearch.extractAll(
                        query = query,
                        choices = entries.map {
                            Pinyin.toPinyin(it.first.name, "").lowercase(Locale.getDefault())
                        }
                    )
                }
                val allCandidatesDuplicated =
                    (phoneNumberCandidates.await() + cardNameCandidates.await() + cardPinyinCandidates.await())
                        .sortedByDescending { it.score }
                val dedup = hashSetOf<Int>()
                for (candidate in allCandidatesDuplicated) {
                    val index = candidate.index
                    val (node, money) = entries[index]
                    if (dedup.contains(index) || finalRes.contains(node) || candidate.score < minScore) {
                        continue
                    }
                    finalRes[node] = money
                    if (finalRes.size >= top) {
                        break
                    }
                }
            }
            return finalRes
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
                    choices = services.map {
                        Pinyin.toPinyin(it.name, "").lowercase(Locale.getDefault())
                    }
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

        suspend fun queryCardOwner(card: MoneyNode): MoneyNode {
            if (card.type != MoneyNodeType.Card) {
                throw IllegalArgumentException("card must be a card node")
            }
            val res = mutableListOf<MoneyNode>()
            db.withTransaction {
                val bills = getAllBills()
                for (bill in bills) {
                    if (bill.toId == card.id) {
                        val node = getMoneyNode(bill.fromId)
                        if (node != null && node.type == MoneyNodeType.Customer && !res.contains(node)) {
                            res.add(node)
                        }
                    }
                }
            }
            if (res.size != 1) {
                error("more than 1 owner of card ${card.id}")
            }
            return res[0]
        }

        suspend fun queryCardBalance(card: MoneyNode): BigDecimal {
            var ballance = BigDecimal.ZERO
            db.withTransaction {
                if (card.type != MoneyNodeType.Card) {
                    error("card type invalid")
                }
                val owner = queryCardOwner(card)
                if (owner.type != MoneyNodeType.Customer) {
                    error("card owner type invalid")
                }
                val outside = getOutside()
                val bills = getAllBills()
                for (bill in bills) {
                    if (bill.fromId == owner.id && bill.toId == card.id && bill.valid) {
                        ballance += bill.money
                    } else if (bill.fromId == card.id && bill.toId == outside.id && bill.valid) {
                        ballance -= bill.money
                    }
                }
            }
            return ballance
        }
    }

    abstract fun dao(): FSDao

}