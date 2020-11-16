package cz.dobris.zkousec

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_question_pack_setup2.*

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
                val session = TestSession(qp)
                val nextQuestion1 = session.nextQuestion()
                val nextQuestion2 = session.nextQuestion()
                Log.d("Zkousec", "Same question: " + (nextQuestion1 == nextQuestion2))
                session.evaluateAnswer(nextQuestion1.question.answers[1])
                Log.d("Zkousec", "Total: " + session.totalQuestions())
                Log.d("Zkousec", "To process: " + session.remainingQuestions())
                Log.d("Zkousec", "Correct: " + session.correctlyAnsweredQuestions())
                Log.d("Zkousec", "Incorrect: " + session.incorrectlyAnsweredQuestions())

            }
            DeleteButton.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete file")
                    .setMessage("Do you really want to delete the questions titled: " + fileName + "?")
                    .setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialog, which ->
                            Storage.deleteQFile(fileName, it.context)
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