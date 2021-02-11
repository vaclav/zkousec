package cz.dobris.zkousec.activities

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.core.view.isVisible
import com.google.android.material.chip.Chip
import cz.dobris.zkousec.R
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import kotlinx.android.synthetic.main.activity_question_pack_learning.*
import kotlinx.android.synthetic.main.activity_question_pack_testing.*
import layout.Answer
import layout.Question
import kotlin.concurrent.thread

class QPLearningActivity : AppCompatActivity() {

    /* TODO:
        - Handle image answers, questions
    */
    lateinit var fileName : String
    lateinit var session: TestSession


    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_learning)

        fileName = intent.getStringExtra("FILE_NAME") ?: ""

        val handler = Handler()
        thread {
            session = DBHelper.getTestSession(this,fileName)
            handler.post{
                updateVisuals()
                updateButtons(true)
            }
        }

        learnShowAnswerButton.setOnClickListener {
            learnAnswerText.visibility = View.VISIBLE
            updateButtons(false)
        }
        learnIKbutton.setOnClickListener {
            if(session.remainingQuestions() != 0){
                session.evaluateAnswer(findAnswer(session.nextQuestion().question, true))
                thread {
                    DBHelper.saveTestSession(this, session)
                }
            }
            updateVisuals()
            updateButtons(true)
        }
        learnIDKbutton.setOnClickListener {
            if (session.remainingQuestions() != 0){
                session.evaluateAnswer(findAnswer(session.nextQuestion().question, false))
                thread {
                    DBHelper.saveTestSession(this, session)
                }
            }
            updateVisuals()
            updateButtons(true)
        }
    }

    private fun updateVisuals(){
        Log.d("Zkousec", "Remaining questions when updating visuals: " + session.remainingQuestions())
        if (session.remainingQuestions()==0) {
            val intent = Intent (this, QPSetupActivity::class.java)
            intent.putExtra("FILE_NAME", fileName)
            startActivity (intent)
        } else {
            Log.d("Zkousec", "Else block. Remaining " + session.remainingQuestions())
            learnQuestionText.text = session.nextQuestion().question.text
            learnAnswerText.text = findAnswer(session.nextQuestion().question,true).toString()
            learnAnswerText.visibility = View.GONE
        }
    }
    private fun findAnswer(q: Question, correct: Boolean) : Answer {
        for (answer in q.answers) {
            if (answer.correct == correct)
                return answer
        }
        throw java.lang.IllegalArgumentException("Question '${q.text}', position: ${q.position} has no ${if (correct) "right" else "wrong"} answers")
    }
    private fun updateButtons(showButtonVisibility: Boolean){
        if (showButtonVisibility){
            learnIDKbutton.visibility = View.GONE
            learnIKbutton.visibility = View.GONE
            learnShowAnswerButton.visibility = View.VISIBLE
        } else {
            learnIDKbutton.visibility = View.VISIBLE
            learnIKbutton.visibility = View.VISIBLE
            learnShowAnswerButton.visibility = View.GONE
        }
    }

}
