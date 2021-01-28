package cz.dobris.zkousec

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.fileStorage.Storage
import kotlinx.android.synthetic.main.activity_question_pack_setup2.*
import layout.QuestionPack
import kotlin.concurrent.thread

class QPSetupActivity : AppCompatActivity() {
    lateinit var fileName : String
    var session: TestSession? = null

    override fun onStart() {
        super.onStart()

        val handler = Handler()
        thread {
            val qp = Storage.loadQFile(fileName, this)
            if (DBHelper.existsTestSession(this, fileName)) {
                session = DBHelper.getTestSession(this, fileName)
            }
            handler.post {
                updateVisuals(session, qp)
            }
        }
        TitleText.text = fileName.replace(".xml", "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_setup2)

        fileName = intent.getStringExtra("FILE_NAME") ?: ""
        StartButton.setOnClickListener {
            intent = Intent(this, QPTestingActivity::class.java)
            intent.putExtra("FILE_NAME", fileName)
            intent.putExtra("LEARN", TestingOptions.checkedChipId==chipLearn.id)
            intent.putExtra("TEST", TestingOptions.checkedChipId==chipTest.id)

            if (session == null) {
                val qp = Storage.loadQFile(fileName, this)
                session = DBHelper.createTestSession(this, fileName, TestSession(qp))
            }
            startActivity(intent)
        }
        ResetButton.setOnClickListener {
            val handler = Handler()
            thread {
                DBHelper.deleteTestSession(this, fileName)
                val qp = Storage.loadQFile(fileName, this)
                val session = null
                handler.post {
                    updateVisuals(session, qp);
                }
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
                            val db = DBHelper.deleteTestSession(this, fileName)
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

    private fun updateVisuals(session: TestSession?, qp: QuestionPack) {
        QuestionCount.text = "Number of questions in the set: " + if (session==null) qp.questions.size.toString() else session.qp.questions.size
        CorrectlyAnsweredCount.text = if (session==null) "0" else session.correctlyAnsweredQuestions().size.toString();
        IncorrectlyAnsweredCount.text = if (session==null) "0" else session.incorrectlyAnsweredQuestions().size.toString();
        ToProcessCount.text = "Remaining questions: " + if (session==null) qp.questions.size.toString() else session.remainingQuestions().toString()
        StartButton.text = if (session==null) "Start" else "Continue"
    }
}
