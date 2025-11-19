package com.spread.footspa.db

enum class BillType(val str: String) {
    None(""),
    Purchase("purchase"),
    Deposit("deposit"),
    DepositCard("deposit_card"),
    UseCard("use_card"),
    ThirdPartyDisplay("third_party_display"),
    ThirdPartyReal("third_party_real")
}
