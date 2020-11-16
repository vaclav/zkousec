package cz.dobris.zkousec

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.db.SessionEntity
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.fileStorage.Storage
import kotlinx.android.synthetic.main.activity_question_pack_setup2.*
import kotlin.concurrent.thread

class QuestionPackSetup : AppCompatActivity() {

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_setup2)

        val fileName = intent.getStringExtra("FILE_NAME")


        if(fileName!=null) {
            val qp = Storage.loadQFile(fileName, this)
            Log.d("Zkousec", "Loaded: " + qp.id + ":" + qp.version)
            TitleText.text = fileName.replace(".xml","")
            QuestionCount.text = "Počet otázek v sadě: " + qp.questions.size
            //TODO show the numbers of correctly/incorrectly answered questions as well as the reaining ones
            StartButton.setOnClickListener {

                //TODO start a testing activity
                thread {
                    val db = DBHelper.instance(this)
                    val loaded = db.sessionDao().loadAllById(fileName)
                    val session = if (loaded != null) {
                        Log.d("Zkousec", "Reusing a session")
                        TestSession.fromSession(qp, loaded)
                    } else {
                        val newSession = TestSession(fileName, qp)
                        db.sessionDao().insert(newSession.toSessionEntity())
                        Log.d("Zkousec", "Created new session")
                        newSession
                    }
                    Log.d("Zkousec", "Loaded: " + loaded)
                    Log.d("Zkousec", "Total: " + session.totalQuestions())
                    Log.d("Zkousec", "To process: " + session.remainingQuestions())
                    Log.d("Zkousec", "Correct: " + session.correctlyAnsweredQuestions())
                    Log.d("Zkousec", "Incorrect: " + session.incorrectlyAnsweredQuestions())
                }
            }
            DeleteButton.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete file")
                    .setMessage("Do you really want to delete the questions titled: " + fileName + "?")
                    .setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialog, which ->
                            Storage.deleteQFile(fileName, it.context)
                            thread {
                                val db = DBHelper.instance(this)
                                val loaded = db.sessionDao().loadAllById(fileName)
                                if(loaded != null) db.sessionDao().delete(loaded)
                            }

                            val intent = Intent (this, MainActivity::class.java)
                            startActivity(intent)
                        })

                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()


            }
        }
    }
}