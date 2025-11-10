package com.example.footspa.db

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.footspa.MainApplication

@Database(
    entities = [
        MoneyNode::class,
        Bills::class,
        MassageService::class
    ],
    version = 1
)
abstract class FSDB : RoomDatabase() {

    companion object {

        private val sqlist = listOf(
            SQLConst.UNIQUE_INDEX_TYPE_OUTSIDE,
            SQLConst.UNIQUE_INDEX_TYPE_PUBLIC
        )

        private val cb = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                sqlist.forEach { db.execSQL(it) }
            }
        }

        val db by lazy {
            Room.databaseBuilder(
                context = MainApplication.instance,
                klass = FSDB::class.java,
                name = "fengshedb"
            )
                .addCallback(cb)
                .build()
        }
    }

}