package cz.dobris.zkousec.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cz.dobris.zkousec.R
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.fileStorage.Storage
import kotlinx.android.synthetic.main.activity_question_pack_setup2.*
import layout.QuestionPack
import java.time.format.DateTimeFormatter
import kotlin.concurrent.thread

class QPSetupActivity : AppCompatActivity() {
    lateinit var fileName: String
    var session: TestSession? = null
    override fun onStart() {
        super.onStart()

        val handler = Handler()
        thread {
            val qp = Storage.loadQFile(fileName, this)
            if (DBHelper.existsTestSession(this, fileName)) {
                session = DBHelper.getTestSession(this, fileName)
            }
            handler.post {
                updateVisuals(session, qp)
            }


        }
        TitleText.text = fileName.replace(".xml", "")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_setup2)
        title = ""
        fileName = intent.getStringExtra("FILE_NAME") ?: ""
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

        TestingOptions.setOnCheckedChangeListener { chipGroup, i ->
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            when (TestingOptions.checkedChipId) {
                chipTest.id -> StartButton.visibility = View.VISIBLE
                chipLearn.id -> StartButton.visibility = View.VISIBLE
                else -> StartButton.visibility = View.INVISIBLE
            }
        }

        when (TestingOptions.checkedChipId) {
            chipTest.id -> StartButton.visibility = View.VISIBLE
            chipLearn.id -> StartButton.visibility = View.VISIBLE
            else -> StartButton.visibility = View.INVISIBLE
        }
        StartButton.setOnClickListener {
            val handler = Handler()
            when (TestingOptions.checkedChipId) {
                chipLearn.id -> intent = Intent(this, QPLearningActivity::class.java)
                chipTest.id -> intent = Intent(this, QPTestingActivity::class.java)
            }
            intent.putExtra("FILE_NAME", fileName)

            thread {
                if (session == null || session!!.remainingQuestions() == 0) {
                    val qp = Storage.loadQFile(fileName, this)
                    session = DBHelper.createTestSession(
                        this, fileName, TestSession(
                            qp,
                            learnMode = TestingOptions.checkedChipId == chipLearn.id,
                            initializer = TestSession.AllQuestionsInitializer(), answerHandler =
                            if (TestingOptions.checkedChipId == chipLearn.id) TestSession.RetryIncorrectAnswerHandler() else TestSession.SimpleAnswerHandler()
                        )
                    )
                    handler.post {
                        startActivity(intent)
                    }
                } else {
                    handler.post {
                        startActivity (intent)
                        /*AlertDialog.Builder(this)
                            .setTitle("Do you want to continue?")
                            .setPositiveButton("Continue",
                                DialogInterface.OnClickListener { dialog, which ->
                                    startActivity(intent)
                                })
                            .setNegativeButton("Reset", DialogInterface.OnClickListener { dialog, which ->
                                thread {
                                    DBHelper.deleteTestSession(this, fileName)
                                    session = null
                                    val qp = Storage.loadQFile(fileName, this)
                                    handler.post {
                                        updateVisuals(session, qp);
                                    }
                                }
                            })
                            .show()*/
                    }
                }
            }
        }
        resetButton.setOnClickListener {
            val handler = Handler ()
            thread {
                DBHelper.deleteTestSession(this, fileName)
                session = null
                val qp = Storage.loadQFile(fileName, this)
                handler.post {
                    updateVisuals(session, qp);
                }
            }
        }
        setupCorectlyAnsweredCard.setOnClickListener {
            intent = Intent(this, QPResultsActivity::class.java)
            intent.putExtra("TO_SHOW", "correct")
            intent.putExtra("FILE_NAME", fileName)
            startActivity(intent)
        }
        setupIncorectlyAnsweredCard.setOnClickListener {
            intent = Intent(this, QPResultsActivity::class.java)
            intent.putExtra("TO_SHOW", "incorrect")
            intent.putExtra("FILE_NAME", fileName)
            startActivity(intent)
        }

    }

    private fun updateVisuals(session: TestSession?, qp: QuestionPack) {
        QuestionCount.text = "Number of questions in the set: " + if (session == null) qp.questions.size.toString() else session.qp.questions.size
        CorrectlyAnsweredCount.text = if (session == null) "0" else session.correctlyAnsweredQuestions().size.toString();
        IncorrectlyAnsweredCount.text = if (session == null) "0" else session.incorrectlyAnsweredQuestions().size.toString();
        ToProcessCount.text = "Remaining questions: " + if (session == null) qp.questions.size.toString() else session.remainingQuestions().toString()
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd. MM. yyyy HH:mm")
        LastUsed.text = "Last used: " + if (session == null) "Unknown" else formatter.format(session.lastUsed)
        StartButton.text = if (session == null) "Start" else "Continue"
        resetButton.visibility = if (session == null) View.GONE else View.VISIBLE

        if (session != null) {
            if (session.learnMode) {
                chipLearn.isChecked = true
                chipTest.isChecked = false
            } else {
                chipLearn.isChecked = false
                chipTest.isChecked = true
            }
            chipLearn.isEnabled = false
            chipTest.isEnabled = false
        } else {
            chipLearn.isEnabled = true
            chipTest.isEnabled = true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_delete -> {
                AlertDialog.Builder(this)
                    .setTitle("Delete file")
                    .setMessage("Do you really want to delete the questions titled: " + fileName + "?")
                    .setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialog, which ->
                            Toast.makeText(this, fileName + " has been deleted!", Toast.LENGTH_LONG).show()
                            Storage.deleteQFile(fileName, this)
                            thread {
                                val db = DBHelper.deleteTestSession(this, fileName)
                            }
                            startActivity(Intent(this, MainActivity::class.java))
                        })
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_qp_setup, menu)
        return true
    }

    override fun onBackPressed() {
        startActivity(Intent(this, MainActivity::class.java))
    }

    private fun saveLastQPid() {
        val sharedPreferences = getSharedPreferences("lastQPcardPref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply() {
            putString("lastQPcardString", fileName)
        }.apply()
    }
}
