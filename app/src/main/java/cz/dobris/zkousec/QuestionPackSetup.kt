package cz.dobris.zkousec

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.fileStorage.Storage
import kotlinx.android.synthetic.main.activity_question_pack_setup2.*
import kotlin.concurrent.thread

class QuestionPackSetup : AppCompatActivity() {
    lateinit var fileName : String
    lateinit var session: TestSession

    override fun onSaveInstanceState(outState: Bundle, outPersistentState: PersistableBundle) {
        super.onSaveInstanceState(outState, outPersistentState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onStart() {
        super.onStart()
        fileName = intent.getStringExtra("FILE_NAME") ?: ""

        val qp = Storage.loadQFile(fileName, this)
        Log.d("Zkousec", "Loaded: " + qp.id + ":" + qp.version)
        TitleText.text = fileName.replace(".xml", "")
        QuestionCount.text = "Počet otázek v sadě: " + qp.questions.size

        val handler = Handler()
        thread {
            val db = DBHelper.instance(this)
            val loaded = db.sessionDao().loadAllById(fileName)
            session = if (loaded != null) {
                Log.d("Zkousec", "Reusing a session")
                TestSession.fromSessionEntity(qp, loaded)
            } else {
                val newSession = TestSession(qp)
                db.sessionDao().insert(newSession.toSessionEntity())
                Log.d("Zkousec", "Created new session")
                newSession
            }
            Log.d("Zkousec", "Loaded: " + loaded)
            Log.d("Zkousec", "Total: " + session.totalQuestions())
            Log.d("Zkousec", "To process: " + session.remainingQuestions())
            Log.d("Zkousec", "Correct: " + session.correctlyAnsweredQuestions())
            Log.d("Zkousec", "Incorrect: " + session.incorrectlyAnsweredQuestions())
            handler.post {
                //TODO show the numbers of correctly/incorrectly answered questions as well as the remaining ones
                ToProcessCount.text = "Zbývající otázky: " + session.remainingQuestions().toString()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_setup2)

        StartButton.setOnClickListener {
            val nextQuestion = session.nextQuestion()
            session.evaluateAnswer(nextQuestion.question.answers[0])
            //TODO start a testing activity
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
                            db.sessionDao().delete(loaded)
                        }

                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                    })

                .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()
        }
    }
}
