package cz.dobris.zkousec

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
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
                setAnswerButtonsText()
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
                    setAnswerButtonsText()
                    QuestionText.text = session.nextQuestion().question.text
                    setTitle(fileName)
                    RemainingQuestionsText.text = "Remaining questions: " + session.remainingQuestions().toString()
                }
            }
        }
    }
            fun setAnswerButtonsText(){
                val numberOfQuestion = session.nextQuestion().question.answers.size
                AnswerButton1.visibility = View.VISIBLE
                AnswerButton2.visibility = View.VISIBLE
                AnswerButton3.visibility = View.VISIBLE
                AnswerButton4.visibility = View.VISIBLE
                when(numberOfQuestion){
                    1 -> {
                        /// TODO: error message
                    }
                    2 -> {
                        setAnswerButtonsTextHelper(AnswerButton1, 0)
                        setAnswerButtonsTextHelper(AnswerButton2, 1)
                        AnswerButton3.visibility = View.GONE;
                        AnswerButton4.visibility = View.GONE;

                    }
                    3 -> {
                        setAnswerButtonsTextHelper(AnswerButton1, 0)
                        setAnswerButtonsTextHelper(AnswerButton2, 1)
                        setAnswerButtonsTextHelper(AnswerButton3, 2)
                        AnswerButton4.visibility = View.GONE;
                    }
                    4 -> {
                        setAnswerButtonsTextHelper(AnswerButton1, 0)
                        setAnswerButtonsTextHelper(AnswerButton2, 1)
                        setAnswerButtonsTextHelper(AnswerButton3, 2)
                        setAnswerButtonsTextHelper(AnswerButton4, 3)
                    }
                    else -> {
                        error("Buttons text setting failed")
                    }
                }
            }
    fun setAnswerButtonsTextHelper(answerButton : Button, questionNumber : Int){

        answerButton.text = session.nextQuestion().question.answers[questionNumber].toString()
    }
}