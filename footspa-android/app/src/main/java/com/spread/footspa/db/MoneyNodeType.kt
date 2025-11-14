package com.spread.footspa.db

const val PEOPLE_TYPE_EMPLOYER = "股东"
const val PEOPLE_TYPE_EMPLOYEE = "员工"
const val PEOPLE_TYPE_CUSTOMER = "顾客"

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
        MoneyNodeType.Employer -> PEOPLE_TYPE_EMPLOYER
        MoneyNodeType.Employee -> PEOPLE_TYPE_EMPLOYEE
        MoneyNodeType.Customer -> PEOPLE_TYPE_CUSTOMER
        else -> ""
    }