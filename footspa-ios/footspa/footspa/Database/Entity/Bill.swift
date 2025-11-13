//
//  Bill.swift
//  footspa
//
//  Created by ljx on 2025/11/13.
//

import Foundation
import GRDB

struct Bill: Codable, FetchableRecord, PersistableRecord {
    var id: Int64?
    var date: Int64
    var fromId: Int64
    var toId: Int64
    var money: Decimal
    var valid: Bool
    var tags: [String]
    var remark: String
    var service: Int64
    var servant: Int64

    static let databaseTableName = "bill"

    private enum Columns: String, ColumnExpression {
        case id
        case date
        case fromId = "money_from"
        case toId = "money_to"
        case money
        case valid
        case tags
        case remark
        case service
        case servant
    }

    // 在插入或更新数据库时执行的编码规则
    func encode(to container: inout PersistenceContainer) {
        container[Columns.id] = id
        container[Columns.date] = date
        container[Columns.fromId] = fromId
        container[Columns.toId] = toId
        container[Columns.money] = money
        container[Columns.valid] = valid
        container[Columns.tags] = try? JSONEncoder().encode(tags)
        container[Columns.remark] = remark
        container[Columns.service] = service
        container[Columns.servant] = servant
    }

    init(row: Row) {
        id = row[Columns.id]
        date = row[Columns.date]
        fromId = row[Columns.fromId]
        toId = row[Columns.toId]
        money = row[Columns.money]
        valid = row[Columns.valid]
        if let data: Data = row[Columns.tags],
           let decoded = try? JSONDecoder().decode([String].self, from: data) {
            tags = decoded
        } else {
            tags = []
        }
        remark = row[Columns.remark]
        service = row[Columns.service]
        servant = row[Columns.servant]
    }
}
