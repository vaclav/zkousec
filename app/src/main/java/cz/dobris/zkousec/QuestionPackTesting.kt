package cz.dobris.zkousec

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import com.google.android.material.chip.Chip
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
                setTitle(fileName.replace(".xml",""))
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
                AnswerChip1.visibility = View.VISIBLE
                AnswerChip2.visibility = View.VISIBLE
                AnswerChip3.visibility = View.VISIBLE
                AnswerChip4.visibility = View.VISIBLE
                when(numberOfQuestion){
                    1 -> {
                        /// TODO: error message
                    }
                    2 -> {
                        setAnswerButtonsTextHelper(AnswerChip1, 0)
                        setAnswerButtonsTextHelper(AnswerChip2, 1)
                        AnswerChip3.visibility = View.GONE;
                        AnswerChip4.visibility = View.GONE;

                    }
                    3 -> {
                        setAnswerButtonsTextHelper(AnswerChip1, 0)
                        setAnswerButtonsTextHelper(AnswerChip2, 1)
                        setAnswerButtonsTextHelper(AnswerChip3, 2)
                        AnswerChip4.visibility = View.GONE;
                    }
                    4 -> {
                        setAnswerButtonsTextHelper(AnswerChip1, 0)
                        setAnswerButtonsTextHelper(AnswerChip2, 1)
                        setAnswerButtonsTextHelper(AnswerChip3, 2)
                        setAnswerButtonsTextHelper(AnswerChip4, 3)
                    }
                    else -> {
                        error("Buttons text setting failed")
                    }
                }
            }
    fun setAnswerButtonsTextHelper(answerChip: Chip, questionNumber : Int){
        answerChip.text = session.nextQuestion().question.answers[questionNumber].toString()
    }
}