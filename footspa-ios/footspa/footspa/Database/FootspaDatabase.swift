import Foundation
import GRDB
import Combine

final class FootspaDatabase: ObservableObject {
    static let shared = FootspaDatabase()
    
    let dbQueue: DatabaseQueue
    private var cancellables = Set<AnyCancellable>()
    
    @Published private(set) var moneyNodes: [MoneyNode] = []
    @Published private(set) var bills: [Bill] = []
    @Published private(set) var cardInfos: [CardInfo] = []

    private init() {
        let databaseURL = URL(fileURLWithPath: "/Users/ljx/Desktop/footspa_db.sqlite")
        dbQueue = try! DatabaseQueue(path: databaseURL.path)
        
        try? migrator.migrate(dbQueue)
        
        refreshMoneyNodes()
        refreshBills()
        refreshCardInfos()
    }
    
    // MARK: - 数据库迁移
    private var migrator: DatabaseMigrator {
        var migrator = DatabaseMigrator()
        migrator.registerMigration("createTables") { db in
            try db.create(table: "money_node") { t in
                t.autoIncrementedPrimaryKey("id")
                t.column("name", .text).notNull()
                t.column("type", .text).notNull()
                t.column("keys", .text)
                t.column("card_id", .integer)
                t.column("card_valid", .boolean)
            }
            try db.create(table: "bill") { t in
                t.autoIncrementedPrimaryKey("id")
                t.column("date", .integer).notNull()
                t.column("money_from", .integer).notNull()
                t.column("money_to", .integer).notNull()
                t.column("money", .double).notNull()
                t.column("valid", .boolean).notNull()
                t.column("tags", .text)
                t.column("remark", .text)
                t.column("service", .integer)
                t.column("servant", .integer)
            }
        }
        return migrator
    }
    
    // MARK: - 异步刷新数据
    func refreshMoneyNodes() {
        Just(())
            .subscribe(on: DispatchQueue.global(qos: .background))
            .tryMap { [weak self] _ -> [MoneyNode] in
                guard let self = self else { return [] }
                return try self.dbQueue.read { try MoneyNode.fetchAll($0) }
            }
            .receive(on: DispatchQueue.main)
            .sink { completion in
                if case let .failure(error) = completion {
                    print("Refresh MoneyNodes failed:", error)
                }
            } receiveValue: { [weak self] nodes in
                self?.moneyNodes = nodes
            }
            .store(in: &cancellables)
    }
    
    func refreshBills() {
        Just(())
            .subscribe(on: DispatchQueue.global(qos: .background))
            .tryMap { [weak self] _ -> [Bill] in
                guard let self = self else { return [] }
                return try self.dbQueue.read { try Bill.fetchAll($0) }
            }
            .receive(on: DispatchQueue.main)
            .sink { completion in
                if case let .failure(error) = completion {
                    print("Refresh Bills failed:", error)
                }
            } receiveValue: { [weak self] bills in
                self?.bills = bills
            }
            .store(in: &cancellables)
    }
    
    func refreshCardInfos() {
        Just(())
            .subscribe(on: DispatchQueue.global(qos: .background))
            .tryMap { [weak self] _ -> [CardInfo] in
                guard let self = self else { return [] }
                return try self.dbQueue.read { try CardInfo.fetchAll($0) }
            }
            .receive(on: DispatchQueue.main)
            .sink { completion in
                if case let .failure(error) = completion {
                    print("Refresh CardInfos failed:", error)
                }
            } receiveValue: { [weak self] infos in
                self?.cardInfos = infos
            }
            .store(in: &cancellables)
    }
    
    // MARK: - 异步写入
    func insertMoneyNode(_ node: MoneyNode) {
        Just(node)
            .subscribe(on: DispatchQueue.global(qos: .background))
            .tryMap { [weak self] node -> Void in
                guard let self = self else { return }
                try self.dbQueue.write { try node.insert($0) }
            }
            .receive(on: DispatchQueue.main)
            .sink { completion in
                if case let .failure(error) = completion {
                    print("Insert MoneyNode failed:", error)
                }
            } receiveValue: { [weak self] _ in
                self?.refreshMoneyNodes()
            }
            .store(in: &cancellables)
    }
    
    func insertBill(_ bill: Bill) {
        Just(bill)
            .subscribe(on: DispatchQueue.global(qos: .background))
            .tryMap { [weak self] bill -> Void in
                guard let self = self else { return }
                try self.dbQueue.write { try bill.insert($0) }
            }
            .receive(on: DispatchQueue.main)
            .sink { completion in
                if case let .failure(error) = completion {
                    print("Insert Bill failed:", error)
                }
            } receiveValue: { [weak self] _ in
                self?.refreshBills()
            }
            .store(in: &cancellables)
    }
    
    func insertCardInfo(_ cardInfo: CardInfo) {
        Just(cardInfo)
            .subscribe(on: DispatchQueue.global(qos: .background))
            .tryMap { [weak self] cardInfo -> Void in
                guard let self = self else { return }
                try self.dbQueue.write { try cardInfo.insert($0) }
            }
            .receive(on: DispatchQueue.main)
            .sink { completion in
                if case let .failure(error) = completion {
                    print("Insert CardInfo failed:", error)
                }
            } receiveValue: { [weak self] _ in
                self?.refreshCardInfos()
            }
            .store(in: &cancellables)
    }
}
