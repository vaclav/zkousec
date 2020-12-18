package cz.dobris.zkousec

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.core.view.get
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import kotlinx.android.synthetic.main.activity_question_pack_testing.*
import layout.Answer
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
        title = fileName
        val handler = Handler()
        val continueButtonFinal = false

        thread {
            session = DBHelper.getTestSession(this, fileName)
            handler.post{
                QuestionText.text = session.nextQuestion().question.text
                title = fileName.replace(".xml","")
                RemainingQuestionsText.text = "Remaining questions: " + session.remainingQuestions().toString()
                setAnswerButtonsText()
            }
        }
        AnswerChips.setOnCheckedChangeListener { AnswerChips, i ->
            ContinueButton.isVisible = true
        }
        ContinueButton.setOnClickListener {
            val handler = Handler()
            thread {
                session.evaluateAnswer(session.nextQuestion().question.answers[getNumberOfCheckedChipById()])
                DBHelper.saveTestSession(this, session)
                // TODO

                handler.post{
                    if (1 >= session.remainingQuestions()) ContinueButton.isVisible = false
                    if (continueButtonFinal){
                        QuestionText.text = session.nextQuestion().question.text
                        RemainingQuestionsText.text = "Remaining questions: " + session.remainingQuestions().toString()
                    }else{
                        setAnswerButtonsText()
                    }
                }
            }
        }

    }
    private fun setAnswerButtonsText(){
        val numberOfQuestion = session.nextQuestion().question.answers.size
        AnswerChip1.visibility = View.VISIBLE
        AnswerChip2.visibility = View.VISIBLE
        AnswerChip3.visibility = View.VISIBLE
        AnswerChip4.visibility = View.VISIBLE
        when(numberOfQuestion){
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
            else -> throw IllegalArgumentException("Buttons text setting failed")

        }
    }
    private fun setAnswerButtonsTextHelper(answerChip: Chip, questionNumber : Int){
        answerChip.text = session.nextQuestion().question.answers[questionNumber].toString()
    }
    private fun getNumberOfCheckedChipById():Int{
        when (AnswerChips.checkedChipId) {
            AnswerChip1.id -> return 0
            AnswerChip2.id -> return 1
            AnswerChip3.id -> return 2
            AnswerChip4.id -> return 3
            else -> throw IllegalArgumentException("Unknown chip ID")
        }
    }
    // TODO: Show correct answer
    /*
    private fun getChipByNumber(int: Int):Chip{
        when(int){
            0 -> return AnswerCh ip1
            1 -> return AnswerChip2
            2 -> return AnswerChip3
            3 -> return AnswerChip4
            else -> throw IllegalArgumentException("Unknow Number Of Chip")
        }
    }*/

    /*
    private fun showCorrectAnswer(){
        if (session.nextQuestion().question.answers[getNumberOfCheckedChipById()].correct)
    }
    */


}