package cz.dobris.zkousec.activities

import android.app.Activity
import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.opengl.Visibility
import android.os.*
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import com.google.android.material.chip.Chip
import cz.dobris.zkousec.OnSwipeTouchListener
import cz.dobris.zkousec.R
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_question_pack_learning.*
import kotlinx.android.synthetic.main.activity_question_pack_testing.*
import layout.Answer
import layout.Question
import kotlin.concurrent.thread

class QPLearningActivity : AppCompatActivity() {

    /* TODO:
        - Handle image answers, questions
    */
    lateinit var fileName: String
    lateinit var session: TestSession
    lateinit var vibrator: Vibrator


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_learning)

        fileName = intent.getStringExtra("FILE_NAME") ?: ""
        vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator


        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val dnd = sharedPreferences.getString("reply","")
        if (dnd.equals("only_learn_mode") || dnd.equals("both_modes")){
            if (!(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted) {
                AlertDialog.Builder(this)
                    .setTitle("DND access is required")
                    .setNegativeButton(
                        "No", DialogInterface.OnClickListener{ dialog, which ->
                            Toast.makeText(this, "Change DND settings!", Toast.LENGTH_LONG)
                            startActivity(Intent(this,SettingsActivity::class.java).putExtra("FILE_NAME",fileName))
                        }
                    )
                    .setPositiveButton(
                        "Allow access",
                        DialogInterface.OnClickListener { dialog, which ->
                            val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                            startActivity(intent)
                        })
                    .show()
            }else{
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }
        }

        val handler = Handler()
        thread {
            session = DBHelper.getTestSession(this, fileName)
            handler.post {
                updateVisuals()
                updateButtons(true)
            }
        }

        learnShowAnswerButton.setOnClickListener {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            learnAnswerText.visibility = View.VISIBLE
            updateButtons(false)
        }
        learnIKbutton.setOnClickListener {
            processAnswer (true)
        }
        learnIDKbutton.setOnClickListener {
            processAnswer (false)
        }
        qplearnlayout.setOnTouchListener(object : OnSwipeTouchListener (this@QPLearningActivity) {
            override fun onSwipeLeft() {
                super.onSwipeLeft()
                if (learnShowAnswerButton.visibility == View.GONE) {
                    processAnswer(false)
                }
            }

            override fun onSwipeRight() {
                super.onSwipeRight()
                if (learnShowAnswerButton.visibility == View.GONE) {
                    processAnswer(true)
                }
            }
        })
        val typeOfAnswer = sharedPreferences.getString("typeOfAnswer","")
        if (typeOfAnswer.equals("buttons")){
            wrongArrow.visibility = View.INVISIBLE
            rightArrow.visibility = View.INVISIBLE
        }
        if (typeOfAnswer.equals("swipe")){
            learnIDKbutton.visibility = View.INVISIBLE
            learnIKbutton.visibility = View.INVISIBLE
        }


    }

    private fun processAnswer (know: Boolean) {
        vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
        if (session.remainingQuestions() != 0) {
            session.evaluateAnswer(findAnswer(session.nextQuestion().question, know))
            thread {
                DBHelper.saveTestSession(this, session)
            }
        }
        updateVisuals()
        updateButtons(true)
    }

    private fun updateVisuals() {
        Log.d("Zkousec", "Remaining questions when updating visuals: " + session.remainingQuestions())
        if (session.remainingQuestions() == 0) {
            if ((getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
            val intent = Intent(this, QPSetupActivity::class.java)
            intent.putExtra("FILE_NAME", fileName)
            startActivity(intent)
        } else {
            Log.d("Zkousec", "Else block. Remaining " + session.remainingQuestions())
            learnQuestionText.text = session.nextQuestion().question.text
            learnAnswerText.text = findAnswer(session.nextQuestion().question, true).toString()
            learnAnswerText.visibility = View.GONE
            remainingQuestionsText.text = "Remaining: " + session.remainingQuestions().toString()
        }
    }

    private fun findAnswer(q: Question, correct: Boolean): Answer {
        for (answer in q.answers) {
            if (answer.correct == correct)
                return answer
        }
        throw java.lang.IllegalArgumentException("Question '${q.text}', position: ${q.position} has no ${if (correct) "right" else "wrong"} answers")
    }


    private fun updateButtons(showButtonVisibility: Boolean) {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val typeOfAnswer = sharedPreferences.getString("typeOfAnswer","")
        if (showButtonVisibility) {
            if (typeOfAnswer.equals("swipe")){
                learnShowAnswerButton.visibility = View.VISIBLE
                rightArrow.visibility = View.GONE
                wrongArrow.visibility = View.GONE
            }else{
                learnIDKbutton.visibility = View.GONE
                learnIKbutton.visibility = View.GONE

                learnShowAnswerButton.visibility = View.VISIBLE
            }

        } else {
            if (typeOfAnswer.equals("swipe")){
                learnShowAnswerButton.visibility = View.GONE
                rightArrow.visibility = View.VISIBLE
                wrongArrow.visibility = View.VISIBLE
            }else{
                learnIDKbutton.visibility = View.VISIBLE
                learnIKbutton.visibility = View.VISIBLE
                learnShowAnswerButton.visibility = View.GONE
            }
        }
    }

    override fun onBackPressed() {
        if ((getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted) {
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
        super.onBackPressed()
    }

}
