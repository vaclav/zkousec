package cz.dobris.zkousec.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = arrayOf(SessionEntity::class), version = 2)
abstract class ZkousecDatabase : RoomDatabase() {
    abstract fun sessionDao(): SessionDao
}