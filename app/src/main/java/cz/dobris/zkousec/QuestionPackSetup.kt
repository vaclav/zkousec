package cz.dobris.zkousec

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
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

    override fun onStart() {
        super.onStart()

        val handler = Handler()
        thread {
            session = DBHelper.getTestSession(this, fileName)
            handler.post {
                QuestionCount.text = "Počet otázek v sadě: " + session.qp.questions.size
                //TODO show the numbers of correctly/incorrectly answered questions as well as the remaining ones
                CorrectlyAnsweredCount.text = session.correctlyAnsweredQuestions().size.toString();
                IncorrectlyAnsweredCount.text = session.incorrectlyAnsweredQuestions().size.toString();
                ToProcessCount.text = "Zbývající otázky: " + session.remainingQuestions().toString()
            }
        }
        TitleText.text = fileName.replace(".xml", "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_setup2)

        fileName = intent.getStringExtra("FILE_NAME") ?: ""
        StartButton.setOnClickListener {
            val nextQuestion = session.nextQuestion()
            session.evaluateAnswer(nextQuestion.question.answers[0])
            intent = Intent(this, QuestionPackTesting::class.java)
           // val intent = Intent (this, QuestionPackTesting::class.java)
            intent.putExtra("FILE_NAME", fileName)
            startActivity(intent)
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
}
