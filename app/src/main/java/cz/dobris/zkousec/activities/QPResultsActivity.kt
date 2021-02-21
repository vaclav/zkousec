package cz.dobris.zkousec.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.ArrayAdapter
import cz.dobris.zkousec.R
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.fileStorage.Storage
import kotlinx.android.synthetic.main.activity_question_pack_results.*
import kotlin.concurrent.thread

class QPResultsActivity : AppCompatActivity() {
    lateinit var arrayAdapter: ArrayAdapter<String>
    lateinit var session: TestSession
    lateinit var fileName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_results)
        title = "Results"
        fileName = intent.getStringExtra("FILE_NAME") ?: ""
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        results_listView.adapter = arrayAdapter

        var handler = Handler()
        thread {
            session = DBHelper.getTestSession(this, fileName)
            handler.post {
                for (question in session.correctlyAnsweredQuestions()){
                    arrayAdapter.add(question.question.text)
                }
                for (question in session.incorrectlyAnsweredQuestions()){
                    arrayAdapter.add(question.question.text)
                }
            }
        }


    }



    override fun onBackPressed() {
        intent = Intent(this, QPSetupActivity::class.java)
        intent.putExtra("FILE_NAME", fileName)
        startActivity(intent)
    }

}