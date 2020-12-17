package cz.dobris.zkousec

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import kotlinx.android.synthetic.main.activity_question_pack_testing.*
import kotlin.concurrent.thread

class QuestionPackTesting : AppCompatActivity() {

    lateinit var fileName : String
    lateinit var session: TestSession
    override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_testing)

        fileName = intent.getStringExtra("FILE_NAME") ?: ""
        val handler = Handler()
        thread {
            session = DBHelper.getTestSession(this, fileName)
            handler.post{
                QuestionText.text = session.nextQuestion().question.text
                setTitle(fileName)
                RemainingQuestionsText.text = "Remaining questions: " + session.remainingQuestions().toString()
            }
        }
        NextQuestionButton.setOnClickListener {
            val handler = Handler()
            thread {
                session.evaluateAnswer(session.nextQuestion().question.answers[0])
                val nextQuestion = session.nextQuestion()
                DBHelper.saveTestSession(this, session)
                // TODO
                handler.post{
                    QuestionText.text = session.nextQuestion().question.text
                    setTitle(fileName)
                    RemainingQuestionsText.text = "Remaining questions: " + session.remainingQuestions().toString()
                }
            }
        }
    }
}