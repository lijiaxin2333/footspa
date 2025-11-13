//
//  Bill.swift
//  footspa
//
//  Created by ljx on 2025/11/13.
//

import Foundation
import GRDB

struct CardInfo: Codable, FetchableRecord, PersistableRecord {
    var id: Int64?
    var name: String
    var price: Decimal
    var discount: String
    var legacy: Bool
    
    static let databaseTableName: String = "card_info"
    
    enum Columns: String, ColumnExpression {
        case id, name, price, discount, legacy
    }
}
