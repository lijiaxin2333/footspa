package com.example.footspa.db

object SQLConst {

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