package com.spread.footspa.db

const val NODE_TYPE_EMPLOYER = "股东"
const val NODE_TYPE_EMPLOYEE = "员工"
const val NODE_TYPE_CUSTOMER = "顾客"
const val NODE_TYPE_THIRD = "三方平台"
const val NODE_TYPE_CARD = "卡"

enum class MoneyNodeType(val str: String) {

    None(""),
    Public("public"),
    Outside("outside"),
    Third("third"),
    Employer("employer"),
    Employee("employee"),
    Customer("customer"),
    Card("card")
}

val MoneyNodeType.displayStr: String
    get() = when (this) {
        MoneyNodeType.Employer -> NODE_TYPE_EMPLOYER
        MoneyNodeType.Employee -> NODE_TYPE_EMPLOYEE
        MoneyNodeType.Customer -> NODE_TYPE_CUSTOMER
        MoneyNodeType.Card -> NODE_TYPE_CARD
        MoneyNodeType.Third -> NODE_TYPE_THIRD
        else -> ""
    }