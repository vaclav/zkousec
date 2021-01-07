package cz.dobris.zkousec

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.google.android.material.chip.Chip
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import kotlinx.android.synthetic.main.activity_question_pack_testing.*
import kotlin.concurrent.thread

class QPLearningActivity : AppCompatActivity() {

    /* TODO:
        - Use code from ModeHelper.kt (if there some)
            and QPTestingActivity.kt to create
            activity just for "learning mode" purposes.
        - Add button that show directly right answer
            without needing an answer from user.
        - Handle image answers, questions
        - Right answer show by changing a tint of the img/btn to red or green
    */
    /*
    lateinit var fileName : String
    lateinit var session: TestSession

    override fun onStart() {
        super.onStart()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_learning)
        fileName = intent.getStringExtra("FILE_NAME") ?: ""
        val handler = Handler()

        thread {
            session = DBHelper.getTestSession(this, fileName)
            handler.post{
                QuestionText.text = session.nextQuestion().question.text
                title = fileName.replace(".xml","")
                RemainingQuestionsText.text = "Remaining questions: " + session.remainingQuestions().toString()
                ContinueButton.text = "Check"
                setAnswerButtonsText()
            }
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
    }*/
}
