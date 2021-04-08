package cz.dobris.zkousec.activities

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import cz.dobris.zkousec.R
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.fileStorage.Storage
import kotlinx.android.synthetic.main.activity_question_pack_setup2.*
import layout.QuestionPack
import java.lang.Exception
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
                saveLastQPid()
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

        //Obtain question pack
        val qpHandler = Handler()
        thread {
            val qp = Storage.loadQFile(fileName, this)
            qpHandler.post {
                editTextNumberStart.setText("1")
                editTextNumberEnd.setText(qp.questions.size.toString())
                editTextNumberStart.addTextChangedListener (object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        validateRangeInput(qp)
                    }

                    override fun afterTextChanged(s: Editable?) {}

                })
                editTextNumberEnd.addTextChangedListener (object : TextWatcher {
                    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                        validateRangeInput(qp)
                    }

                    override fun afterTextChanged(s: Editable?) {}

                })
            }
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
                            initializer = TestSession.RangeQuestionsInitializer (editTextNumberStart.text.toString().toInt(), editTextNumberEnd.text.toString().toInt()),
                            answerHandler = if (TestingOptions.checkedChipId == chipLearn.id) TestSession.RetryIncorrectAnswerHandler() else TestSession.SimpleAnswerHandler()
                        )
                    )
                    handler.post {
                        startActivity(intent)
                    }
                } else {
                    handler.post {
                        startActivity (intent)
                    }
                }
            }
        }
        resetButton.setOnClickListener {
            val handler = Handler ()
            AlertDialog.Builder(this)
                .setTitle("Are you sure you want to reset?")
                .setPositiveButton("Yes") { dialog, which ->
                    thread {
                        DBHelper.deleteTestSession(this, fileName)
                        session = null
                        val qp = Storage.loadQFile(fileName, this)
                        handler.post {
                            updateVisuals(session, qp);
                        }
                    }
                }
                .setNegativeButton("No") { dialog, which ->
                    dialog.cancel()
                }
                .show()
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
        editTextNumberStart.setText("1")
        editTextNumberEnd.setText(session?.qp?.questions?.size?.toString() ?: qp.questions.size.toString())
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
            editTextNumberStart.isEnabled = false
            editTextNumberEnd.isEnabled = false
        } else {
            chipLearn.isEnabled = true
            chipTest.isEnabled = true
            editTextNumberStart.isEnabled = true
            editTextNumberEnd.isEnabled = true
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

    private fun validateRangeInput (qp: QuestionPack) {
        val start = try { editTextNumberStart.text.toString().toInt() } catch (e : Exception) { -1 }
        val end = try { editTextNumberEnd.text.toString().toInt() } catch (e : Exception) { -1 }
        var valid = true
        if (start < 1 || start > qp.questions.size || start > end) {
            editTextNumberStart.setTextColor(Color.RED)
            valid = false
        } else {
            editTextNumberStart.setTextColor(Color.BLACK)
        }
        if (end < 1 || end > qp.questions.size || start > end) {
            editTextNumberEnd.setTextColor(Color.RED)
            valid = false
        } else {
            editTextNumberEnd.setTextColor(Color.BLACK)
        }
        StartButton.isEnabled = valid
    }
}
