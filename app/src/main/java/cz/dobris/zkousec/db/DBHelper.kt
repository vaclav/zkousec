package cz.dobris.zkousec.db

import android.content.Context
import androidx.room.Room

class DBHelper {
    companion object {
        private var db : ZkousecDatabase? = null

        fun instance(context : Context) : ZkousecDatabase {
            if (db == null) {
                val databaseBuilder =
                    Room.databaseBuilder(context, ZkousecDatabase::class.java, "database-name")
                db = databaseBuilder.build()
            }
            return db!!
        }
    }
}