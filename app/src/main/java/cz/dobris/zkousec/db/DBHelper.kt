package cz.dobris.zkousec.db

import android.content.Context
import android.util.Log
import androidx.room.Room
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.fileStorage.Storage

class DBHelper {
    companion object {
        var db : ZkousecDatabase? = null

        private fun buildDatabase(context : Context) : ZkousecDatabase {
            if (db==null) {
                Log.d("Zkousec", "Building database ${db!=null}")
                db = Room.databaseBuilder(context, ZkousecDatabase::class.java, "database-name").build()
            }
            return db!!
        }

        fun existsTestSession(context: Context, fileName : String) : Boolean = buildDatabase(context).sessionDao().loadAllById(fileName) != null

        fun createTestSession(context: Context, fileName : String, session : TestSession) : TestSession {
            val sessionDatabase = buildDatabase(context).sessionDao()
            val loadedSessionEntity = sessionDatabase.loadAllById(fileName)
            if (loadedSessionEntity != null) {
                deleteTestSession(context, fileName)
            }
            sessionDatabase.insert(session.toSessionEntity())
            return session
        }

        fun getTestSession(context: Context, fileName : String) : TestSession {
            val sessionDatabase = buildDatabase(context).sessionDao()
            val loadedSessionEntity = sessionDatabase.loadAllById(fileName)
            val qp = Storage.loadQFile(fileName, context)
            return if (loadedSessionEntity != null) {
                Log.d("Zkousec", "Successfully loaded a session!!!")
                TestSession.fromSessionEntity(qp, loadedSessionEntity)
            } else {
                Log.d("Zkousec", "Creating a new session!!!")
                val newSession = TestSession(qp)
                sessionDatabase.insert(newSession.toSessionEntity())
                newSession
            }
        }

        fun saveTestSession(context: Context, session : TestSession) {
            val sessionDatabase = DBHelper.buildDatabase(context).sessionDao()
            sessionDatabase.update(session.toSessionEntity())
        }

        fun deleteTestSession(context: Context, fileName : String) {
            val sessionDatabase = DBHelper.buildDatabase(context).sessionDao()
            val loadedSessionEntity = sessionDatabase.loadAllById(fileName)
            if (loadedSessionEntity != null) sessionDatabase.delete(loadedSessionEntity)
        }
    }
}