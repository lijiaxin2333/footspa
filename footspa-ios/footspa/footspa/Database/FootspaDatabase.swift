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
//        let databaseURL = try! FileManager.default
//                    .url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
//                    .appendingPathComponent("footspa_db.sqlite")
        // 临时切换方便调试
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
        Future<[MoneyNode], Error> { promise in
            do {
                let nodes = try self.dbQueue.read { db in
                    try MoneyNode.fetchAll(db)
                }
                promise(.success(nodes))
            } catch {
                promise(.failure(error))
            }
        }
        .receive(on: DispatchQueue.main)
        .sink { _ in } receiveValue: { [weak self] nodes in
            self?.moneyNodes = nodes
        }
        .store(in: &cancellables)
    }
    
    func refreshBills() {
        Future<[Bill], Error> { promise in
            do {
                let allBills = try self.dbQueue.read { db in
                    try Bill.fetchAll(db)
                }
                promise(.success(allBills))
            } catch {
                promise(.failure(error))
            }
        }
        .receive(on: DispatchQueue.main)
        .sink { _ in } receiveValue: { [weak self] bills in
            self?.bills = bills
        }
        .store(in: &cancellables)
    }
    
    func refreshCardInfos() {
        Future<[CardInfo], Error> { promise in
            do {
                let infos = try self.dbQueue.read { db in
                    try CardInfo.fetchAll(db)
                }
                promise(.success(infos))
            } catch {
                promise(.failure(error))
            }
        }
        .receive(on: DispatchQueue.main)
        .sink { _ in } receiveValue: { [weak self] infos in
            self?.cardInfos = infos
        }
        .store(in: &cancellables)
    }
    
    // MARK: - 异步写入
    func insertMoneyNode(_ node: MoneyNode) {
        Future<Void, Error> { promise in
            do {
                try self.dbQueue.write { db in
                    try node.insert(db)
                }
                promise(.success(()))
            } catch {
                promise(.failure(error))
            }
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
        Future<Void, Error> { promise in
            do {
                try self.dbQueue.write { db in
                    try bill.insert(db)
                }
                promise(.success(()))
            } catch {
                promise(.failure(error))
            }
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
        Future<Void, Error> { promise in
            do {
                try self.dbQueue.write { db in
                    try cardInfo.insert(db)
                }
                promise(.success(()))
            } catch {
                promise(.failure(error))
            }
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
