package cz.dobris.zkousec

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listOfFiles = Storage.listQFiles(this)
        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, listOfFiles
        )
        listOfButtons.adapter = arrayAdapter
        if(listOfFiles.size==0) {
            textView2.visibility = View.GONE
            noQuestionsView.visibility = View.VISIBLE
        }

        /*
        //Třeba opravit staré tlačítko za nový list položek
        val button = findViewById<Button> (R.id.button2)
        button.setOnClickListener {
            val intent = Intent (this, QuestionPackSetup::class.java)
            startActivity (intent)
        }

         */
    }
}