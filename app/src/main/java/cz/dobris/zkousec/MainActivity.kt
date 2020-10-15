package cz.dobris.zkousec

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val listView = findViewById<ListView>(R.id.listView)
        val seznam = arrayOf("dfasdfasdfas", "asdfasdfasdf","dfasdfasdfas", "asdfasdfasdf","dfasdfasdfas", "asdfasdfasdf","dfasdfasdfas", "asdfasdfasdf")


        val arrayAdapter: ArrayAdapter<String> = ArrayAdapter(
            this, android.R.layout.simple_list_item_1, seznam/*Storage.listQFiles(this)*/
        )
        listView.adapter = arrayAdapter


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