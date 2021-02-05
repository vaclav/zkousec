package cz.dobris.zkousec.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import cz.dobris.zkousec.R
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.fileStorage.Storage
import kotlinx.android.synthetic.main.activity_question_pack_setup2.*
import layout.QuestionPack
import kotlin.concurrent.thread

class QPSetupActivity : AppCompatActivity() {
    lateinit var fileName : String
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

        fileName = intent.getStringExtra("FILE_NAME") ?: ""
        StartButton.setOnClickListener {

            when(TestingOptions.checkedChipId){
                chipLearn.id -> intent = Intent(this, QPLearningActivity::class.java)
                chipTest.id -> intent = Intent(this, QPTestingActivity::class.java)
            }
            intent.putExtra("FILE_NAME", fileName)

            if (session == null) {
                val qp = Storage.loadQFile(fileName, this)
                session = DBHelper.createTestSession(this, fileName, TestSession(qp))
            }
            startActivity(intent)
        }
        ResetButton.setOnClickListener {
            val handler = Handler()
            thread {
                DBHelper.deleteTestSession(this, fileName)
                val qp = Storage.loadQFile(fileName, this)
                val session = null
                handler.post {
                    updateVisuals(session, qp);
                }
            }
        }
    }

    private fun updateVisuals(session: TestSession?, qp: QuestionPack) {
        QuestionCount.text = "Number of questions in the set: " + if (session==null) qp.questions.size.toString() else session.qp.questions.size
        CorrectlyAnsweredCount.text = if (session==null) "0" else session.correctlyAnsweredQuestions().size.toString();
        IncorrectlyAnsweredCount.text = if (session==null) "0" else session.incorrectlyAnsweredQuestions().size.toString();
        ToProcessCount.text = "Remaining questions: " + if (session==null) qp.questions.size.toString() else session.remainingQuestions().toString()
        StartButton.text = if (session==null) "Start" else "Continue"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId){
                R.id.action_delete -> {
                    AlertDialog.Builder(this)
                        .setTitle("Delete file")
                        .setMessage("Do you really want to delete the questions titled: " + fileName + "?")
                        .setPositiveButton("Yes",
                            DialogInterface.OnClickListener { dialog, which ->
                                Storage.deleteQFile(fileName, this)
                                thread {
                                    val db = DBHelper.deleteTestSession(this, fileName)
                                }
                                val intent = Intent(this, MainActivity::class.java)
                                startActivity(intent)
                            })
                        .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                    return true
                }
            R.id.action_settings -> {
                // TODO
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_qp_setup,menu)
        return true
    }



}
