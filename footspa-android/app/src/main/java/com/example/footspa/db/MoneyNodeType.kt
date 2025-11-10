package com.example.footspa.db

enum class MoneyNodeType(val str: String) {
    Public("public"),
    Outside("outside"),
    Third("third"),
    Employer("employer"),
    Employee("employee"),
    Customer("customer"),
    Card("card")
}