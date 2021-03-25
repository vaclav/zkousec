package cz.dobris.zkousec.activities

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.ArrayAdapter
import androidx.recyclerview.widget.LinearLayoutManager
import cz.dobris.zkousec.R
import cz.dobris.zkousec.adapters.QPRecyclerAdapter
import cz.dobris.zkousec.adapters.QPResultsRecyclerAdapter
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.fileStorage.Storage
import kotlinx.android.synthetic.main.activity_question_pack_results.*
import layout.Answer
import layout.Question
import kotlin.concurrent.thread

class QPResultsActivity : AppCompatActivity() {
    lateinit var session: TestSession
    lateinit var fileName: String

    private var questionList = mutableListOf<String>()
    private var typeList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_results)
        title = "Results"
        fileName = intent.getStringExtra("FILE_NAME") ?: ""


        qp_results_recyclerView.layoutManager = LinearLayoutManager(this)
        qp_results_recyclerView.adapter = QPResultsRecyclerAdapter(questionList,typeList,null)

        var handler = Handler()
        thread {
            session = DBHelper.getTestSession(this, fileName)
            handler.post{
                postToList()
            }
        }
    }



    override fun onBackPressed() {
        intent = Intent(this, QPSetupActivity::class.java)
        intent.putExtra("FILE_NAME", fileName)
        startActivity(intent)
    }

    private fun addToList(question:String,type:String){
        questionList.add(question)
        typeList.add(type)
    }
    private fun postToList(){
        questionList.clear()
        typeList.clear()
        for (aCorrectly in session.correctlyAnsweredQuestions()){
            addToList(aCorrectly.question.text,"correct")
        }
        for (aICorrectly in session.incorrectlyAnsweredQuestions()){
            addToList(aICorrectly.question.text,"incorrect")
        }
        (qp_results_recyclerView.adapter as QPResultsRecyclerAdapter).notifyDataSetChanged()
    }

}