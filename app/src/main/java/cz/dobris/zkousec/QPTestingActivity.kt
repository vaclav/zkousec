package cz.dobris.zkousec

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.mode.ModeHelper
import kotlinx.android.synthetic.main.activity_question_pack_testing.*
import layout.Answer
import layout.Question
import kotlin.concurrent.thread

class QPTestingActivity : AppCompatActivity() {


    /*todo:
        - Simplify code.
        - Find blocks of code that are usable in both modes (test, learn) and add them to ModeHelper.kt
        - Make this activity just for "testing mode" purposes.
        - Connect with QPTestResultsActivity.kt
        - Remove showing correct answer after answering. We want to know correct answers only at the end of session
    */

    lateinit var fileName : String
    lateinit var session: TestSession
    var continueFinal = false;
    override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_testing)

        fileName = intent.getStringExtra("FILE_NAME") ?: ""

        val testChipFromIntent = intent.getBooleanExtra("TEST", false)
        val learnChipFromIntent = intent.getBooleanExtra("LEARN", false)
        val handler = Handler()

        thread {
            session = DBHelper.getTestSession(this, fileName)
            handler.post{
                QuestionText.text = session.nextQuestion().question.text
                title = fileName.replace(".xml","")
                RemainingQuestionsText.text = "Remaining questions: " + session.remainingQuestions().toString()
                ContinueButton.text = "Check"
                setAnswerButtonsText()
                val modeHelper = ModeHelper(session, AnswerChip1,AnswerChip2,AnswerChip3,AnswerChip4,QuestionText,RemainingQuestionsText)
            }
        }
        ContinueButton.setOnClickListener {
            val modeHelper = ModeHelper(session, AnswerChip1,AnswerChip2,AnswerChip3,AnswerChip4,QuestionText,RemainingQuestionsText)
            when(true){
                testChipFromIntent -> {
                    if (!continueFinal){
                        handler.post(){
                            showCorrectAnswer() //TODO show answer for last question in the set
                            ContinueButton.text = "CONTINUE"
                            continueFinal = true
                        }
                    }else{
                        thread {
                            session.evaluateAnswer(session.nextQuestion().question.answers[getNumberOfCheckedChipById()])
                            DBHelper.saveTestSession(this, session)
                            handler.post(){
                                if (session.remainingQuestions() > 0){
                                    showNextQuestion()
                                    ContinueButton.text = "Check"
                                    AnswerChip1.isClickable = true
                                    AnswerChip2.isClickable = true
                                    AnswerChip3.isClickable = true
                                    AnswerChip4.isClickable = true
                                    ContinueButton.isVisible = false
                                    continueFinal = false
                                }else{
                                    QuestionText.text = "No more questions"
                                    RemainingQuestionsText.text = "Remaining questions: 0"
                                    AnswerChips.isVisible = false
                                    ContinueButton.isVisible = false
                                }
                            }
                        }
                    modeHelper.setAnswerChipsColorToDefault()
                    //setAnswerChipsColorToDefault()
                    }
                }
                learnChipFromIntent -> {
                    // TODO: learn mode
                }
                else -> throw IllegalArgumentException("None of the chips were checked")
            }


        }
        AnswerChips.setOnCheckedChangeListener { AnswerChips, i ->
            ContinueButton.isVisible = true
        }
    }

    private fun setAnswerButtonsText(){
        val numberOfAnswers = session.nextQuestion().question.answers.size
        AnswerChip1.visibility = View.VISIBLE
        AnswerChip2.visibility = View.VISIBLE
        AnswerChip3.visibility = View.VISIBLE
        AnswerChip4.visibility = View.VISIBLE
        when(numberOfAnswers){
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

    private fun showCorrectAnswer(){
        val correctAnswer = findAnswer(session.nextQuestion().question, true).toString()
        AnswerChip1.isClickable = false
        AnswerChip2.isClickable = false
        AnswerChip3.isClickable = false
        AnswerChip4.isClickable = false

        if (correctAnswer == AnswerChip1.text) AnswerChip1.background.setTint(Color.GREEN)
            else AnswerChip1.background.setTint(Color.RED)

        if (correctAnswer == AnswerChip2.text) AnswerChip2.background.setTint(Color.GREEN)
            else AnswerChip2.background.setTint(Color.RED)

        if (correctAnswer == AnswerChip3.text) AnswerChip3.background.setTint(Color.GREEN)
            else AnswerChip3.background.setTint(Color.RED)

        if (correctAnswer == AnswerChip4.text) AnswerChip4.background.setTint(Color.GREEN)
            else AnswerChip4.background.setTint(Color.RED)
        //if (session.nextQuestion().question.answers[getNumberOfCheckedChipById()].correct)
    }


    // TODO: handle multiple answers
    private fun findAnswer(q: Question, correct: Boolean) : Answer {
        for (answer in q.answers) {
            if (answer.correct == correct)
                return answer

        }
        throw java.lang.IllegalArgumentException("Question '${q.text}', position: ${q.position} has no ${if (correct) "right" else "wrong"} answers")
    }

    private fun showNextQuestion(){
        QuestionText.text = session.nextQuestion().question.text
        RemainingQuestionsText.text = "Remaining questions: " + session.remainingQuestions().toString()
        setAnswerButtonsText()
        AnswerChip1.isChecked = false
        AnswerChip2.isChecked = false
        AnswerChip3.isChecked = false
        AnswerChip4.isChecked = false
    }

    /*
    private fun setAnswerChipsColorToDefault(){
        AnswerChip1.background.setTintList(null)
        AnswerChip2.background.setTintList(null)
        AnswerChip3.background.setTintList(null)
        AnswerChip4.background.setTintList(null)
    }*/


}