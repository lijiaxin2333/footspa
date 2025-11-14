package com.spread.footspa.db

object SQLConst {

    const val TABLE_NAME_MONEY_NODE = "money_node"

    const val TABLE_NAME_BILL = "bills_all"

    const val TABLE_NAME_MASSAGE_SERVICE = "massage_service"

    const val TABLE_NAME_CARD_TYPE = "card_type"

    const val UNIQUE_INDEX_TYPE_PUBLIC = """
        CREATE UNIQUE INDEX one_public_type
        ON money_node(type)
        WHERE type = 'public';
    """

    const val UNIQUE_INDEX_TYPE_OUTSIDE = """
        CREATE UNIQUE INDEX one_outside_type
        ON money_node(type)
        WHERE type = 'outside';
    """

}