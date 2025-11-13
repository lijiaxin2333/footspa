//
//  MoneyNodeType.swift
//  footspa
//
//  Created by ljx on 2025/11/13.
//

import Foundation
import GRDB

enum MoneyNodeType: String, Codable, DatabaseValueConvertible {
    case none = ""
    case `public` = "public"
    case outside = "outside"
    case third = "third"
    case employer = "employer"
    case employee = "employee"
    case customer = "customer"
    case card = "card"
}
