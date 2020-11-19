package cz.dobris.zkousec.db

import androidx.room.*

@Dao
interface SessionDao {
    @Query("SELECT * FROM SessionEntity")
    fun getAll(): List<SessionEntity>

    @Query("SELECT * FROM SessionEntity WHERE uid == :sessionId")
    fun loadAllById(sessionId: String): SessionEntity

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg sessions: SessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(sessions: SessionEntity)

    @Update
    fun update(vararg sessions: SessionEntity)

    @Delete
    fun delete(user: SessionEntity)
}