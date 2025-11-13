//
//  MoneyNode.swift
//  footspa
//
//  Created by ljx on 2025/11/13.
//

import Foundation
import GRDB

struct MoneyNode: Codable, FetchableRecord, PersistableRecord {
    var id: Int64?
    var name: String
    var type: MoneyNodeType
    var keys: [String]?
    var cardId: Int64?
    var cardValid: Bool?

    static let databaseTableName = "money_node"

    private enum Columns: String, ColumnExpression {
        case id, name, type, keys, cardId = "card_id", cardValid = "card_valid"
    }

    // insert/update 时对 keys 做 JSON 序列化
    func encode(to container: inout PersistenceContainer) {
        container[Columns.id] = id
        container[Columns.name] = name
        container[Columns.type] = type.rawValue
        container[Columns.keys] = try? keys.map { try JSONEncoder().encode($0) }
        container[Columns.cardId] = cardId
        container[Columns.cardValid] = cardValid
    }

    init(id: Int64? = nil,
         name: String,
         type: MoneyNodeType,
         keys: [String]? = nil,
         cardId: Int64? = nil,
         cardValid: Bool? = nil
    ) {
        self.id = id
        self.name = name
        self.type = type
        self.keys = keys
        self.cardId = cardId
        self.cardValid = cardValid
    }
    
    // 从数据库读取时解码 keys
    init(row: Row) {
        id = row[Columns.id]
        name = row[Columns.name]
        type = MoneyNodeType(rawValue: row[Columns.type]) ?? .none
        if let dataArray = row[Columns.keys] as? [Data] {
            keys = dataArray.compactMap { try? JSONDecoder().decode(String.self, from: $0) }
        } else {
            keys = nil
        }
        cardId = row[Columns.cardId]
        cardValid = row[Columns.cardValid]
    }

    // Kotlin 里的 containsKey 方法
    func contains(key: String) -> Bool {
        keys?.contains(key) ?? false
    }
}

class MoneyNodeBuilder {
    var name = "null"
    var type: MoneyNodeType = .none
    var keys: [String]? = nil
    var cardId: Int64? = nil
    var cardValid: Bool? = nil

    func build() throws -> MoneyNode {
        guard type != .none else {
            throw NSError(domain: "MoneyNodeBuilder", code: 1, userInfo: [NSLocalizedDescriptionKey: "money node type is none"])
        }
        return MoneyNode(
            name: name,
            type: type,
            keys: keys,
            cardId: cardId,
            cardValid: cardValid
        )
    }
}

func buildMoneyNode(_ initBlock: (MoneyNodeBuilder) -> Void) throws -> MoneyNode {
    let builder = MoneyNodeBuilder()
    initBlock(builder)
    return try builder.build()
}
