package cz.dobris.zkousec.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
class SessionEntity(@PrimaryKey val uid: String,
                    @ColumnInfo(name = "answer_handler") val answerHandler: String?,
                    @ColumnInfo(name = "to_process") val toProcess: String?,
                    @ColumnInfo(name = "answered_correctly") val answeredCorrectly: String?,
                    @ColumnInfo(name = "answered_incorrectly") val answeredIncorrectly: String?,
                    @ColumnInfo(name = "learnMode") val learnMode: Boolean?,
                    @ColumnInfo(name = "lastUsed") val lastUsed: Long?
) {
}