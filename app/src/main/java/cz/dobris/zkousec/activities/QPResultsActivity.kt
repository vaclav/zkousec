package cz.dobris.zkousec.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cz.dobris.zkousec.R

class QPResultsActivity : AppCompatActivity() {

    lateinit var fileName : String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_results)
        title = "Results"
        fileName = intent.getStringExtra("FILE_NAME") ?: ""
    }

    override fun onBackPressed() {
        intent = Intent(this, QPSetupActivity::class.java)
        intent.putExtra("FILE_NAME", fileName)
        startActivity(intent)
    }

}