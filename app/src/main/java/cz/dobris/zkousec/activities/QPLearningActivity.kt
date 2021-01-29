package cz.dobris.zkousec.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.view.View
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import kotlinx.android.synthetic.main.activity_question_pack_learning.*
import kotlinx.android.synthetic.main.activity_question_pack_testing.*
import layout.Answer
import layout.Question
import kotlin.concurrent.thread

class QPLearningActivity : AppCompatActivity() {

    /* TODO:
        - Use code from ModeHelper.kt
            and QPTestingActivity.kt to create
            activity just for "learning mode" purposes.
        - Add button that show directly right answer
            without needing an answer from user.
        - Handle image answers, questions
        - Right answer show by changing a tint of the img/btn to red or green
    */
    lateinit var fileName : String
    lateinit var session: TestSession


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        fileName = intent.getStringExtra("FILE_NAME") ?: ""
        val handler = Handler()
        thread {
            session = DBHelper.getTestSession(this,fileName)
            handler.post{
                updateVisuals()
            }
        }

        learnShowAnswerButton.setOnClickListener {
            learnAnswerText.visibility = View.VISIBLE
        }


    }

    private fun updateVisuals(){
        learnQuestionText.text = session.nextQuestion().question.text
        learnAnswerText.text = findAnswer(session.nextQuestion().question,true).toString()
        learnAnswerText.visibility = View.GONE
    }
    private fun findAnswer(q: Question, correct: Boolean) : Answer {
        for (answer in q.answers) {
            if (answer.correct == correct)
                return answer

        }
        throw java.lang.IllegalArgumentException("Question '${q.text}', position: ${q.position} has no ${if (correct) "right" else "wrong"} answers")
    }

}
