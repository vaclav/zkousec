package cz.dobris.zkousec

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_question_pack_setup2.*

class QuestionPackSetup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_setup2)

        val fileName = intent.getStringExtra("FILE_NAME")
        if(fileName!=null) {
            StartButton.setOnClickListener {
                Storage.loadQFile(fileName, it.context)
                //TODO
            }

            DeleteButton.setOnClickListener {
                Storage.deleteQFile(fileName, it.context)
            }
        }
    }
}