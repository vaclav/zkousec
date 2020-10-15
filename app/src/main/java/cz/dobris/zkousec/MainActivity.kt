package cz.dobris.zkousec

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import org.w3c.dom.Text


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
            QuestionPacksOnTheDeviceText.text = "No question packs installed yet!"
            QuestionPacksOnTheDeviceText.setTextColor(Color.RED)
        }

        listOfButtons.setOnItemClickListener { adapterView, view, position, id ->
            val intent = Intent (this, QuestionPackSetup::class.java)
            startActivity (intent)
        }


    }
}