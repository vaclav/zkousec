package cz.dobris.zkousec.activities

import android.app.NotificationManager
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.opengl.Visibility
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.preference.PreferenceManager
import cz.dobris.zkousec.R
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.mode.ModeHelper
import kotlinx.android.synthetic.main.activity_question_pack_setup2.*
import kotlinx.android.synthetic.main.activity_question_pack_testing.*
import layout.QuestionPack
import kotlin.concurrent.thread

class QPTestingActivity : AppCompatActivity() {
    lateinit var fileName: String
    lateinit var session: TestSession
    lateinit var modeHelper: ModeHelper
    var dndSET = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_testing)
        fileName = intent.getStringExtra("FILE_NAME") ?: ""
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        val handler = Handler()
        thread {
            session = DBHelper.getTestSession(this, fileName)
            modeHelper = ModeHelper(session)
            handler.post {
                updateVisuals()
            }
        }
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val dnd = sharedPreferences.getString("reply","")

        if (dnd.equals("only_test_mode") || dnd.equals("both_modes")){
            dndSET = true
            if (!(getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted) {
                AlertDialog.Builder(this)
                    .setTitle("DND access is required")
                    .setNegativeButton(
                        "No", DialogInterface.OnClickListener{dialog, which ->
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

        AnswerChip1.setOnClickListener {vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))}
        AnswerChip2.setOnClickListener {vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))}
        AnswerChip3.setOnClickListener {vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))}
        AnswerChip4.setOnClickListener {vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))}
        AnswerChips.setOnCheckedChangeListener { AnswerChips, i ->
            ContinueButton.isVisible = AnswerChip1.isChecked == true || AnswerChip2.isChecked == true || AnswerChip3.isChecked == true || AnswerChip4.isChecked == true
        }

        ContinueButton.setOnClickListener {
            thread {
                session.evaluateAnswer(session.nextQuestion().question.answers[getNumberOfCheckedChipById()])
                DBHelper.saveTestSession(this, session)
                handler.post {
                    vibrator.vibrate(VibrationEffect.createOneShot(40, VibrationEffect.DEFAULT_AMPLITUDE))
                    updateVisuals()
                }
            }
        }

    }


    private fun updateVisuals() {
        if (session.remainingQuestions() != 0) {
            if ((getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).isNotificationPolicyAccessGranted) {
                val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
            ContinueButton.isVisible = false
            modeHelper.setAnswerButtonsText(AnswerChip1, AnswerChip2, AnswerChip3, AnswerChip4)
            QuestionText.text = session.nextQuestion().question.text
            RemainingQuestionsText.text = "Remaining questions: " + session.remainingQuestions().toString()
            AnswerChip1.isChecked = false
            AnswerChip2.isChecked = false
            AnswerChip3.isChecked = false
            AnswerChip4.isChecked = false
        } else {
            intent = Intent(this, QPResultsActivity::class.java)
            intent.putExtra("TO_SHOW", "all")
            intent.putExtra("FILE_NAME", fileName)
            startActivity(intent)
        }

    }

    private fun getNumberOfCheckedChipById(): Int {
        when (AnswerChips.checkedChipId) {
            AnswerChip1.id -> return 0
            AnswerChip2.id -> return 1
            AnswerChip3.id -> return 2
            AnswerChip4.id -> return 3
            else -> throw IllegalArgumentException("Unknown chip ID")
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