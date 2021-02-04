package cz.dobris.zkousec.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.view.isVisible
import cz.dobris.zkousec.R
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.mode.ModeHelper
import kotlinx.android.synthetic.main.activity_question_pack_setup2.*
import kotlinx.android.synthetic.main.activity_question_pack_testing.*
import layout.QuestionPack
import kotlin.concurrent.thread

class QPTestingActivity : AppCompatActivity() {
    lateinit var fileName : String
    lateinit var session: TestSession
    lateinit var modeHelper: ModeHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_testing)

        fileName = intent.getStringExtra("FILE_NAME") ?: ""
        val handler = Handler()
        thread {
            session = DBHelper.getTestSession(this, fileName)
            modeHelper = ModeHelper(session)
            handler.post{
                //updateVisuals()
            }
        }

        AnswerChips.setOnCheckedChangeListener{ AnswerChips, i ->
                ContinueButton.isVisible = true
        }

        ContinueButton.setOnClickListener {
            thread {
                session.evaluateAnswer(session.nextQuestion().question.answers[0])
                DBHelper.saveTestSession(this, session)
                handler.post{
                    updateVisuals()
                }
            }
        }


    }
    private fun updateVisuals(){
        ContinueButton.isVisible = false
        modeHelper.setAnswerButtonsText(AnswerChip1,AnswerChip2,AnswerChip3,AnswerChip4)
        QuestionText.text = session.nextQuestion().question.text
        RemainingQuestionsText.text = "Remaining questions: " + session.remainingQuestions().toString()
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







}