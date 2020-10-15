package cz.dobris.zkousec

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.url_dialog.*
import kotlinx.android.synthetic.main.url_dialog.view.*
import org.w3c.dom.Text


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: Add button and img to each item in the list
        // TODO: Make possible to delete Question Packs from MainActivity

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


        //This part of code shows dialog to user with text field for downloading Question Pack from URL.
        //URL is saved in "urlByUser"
        addQuestionPackButton.setOnClickListener{
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.url_dialog, null)
            val downloadDialog = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Download Question Pack")
                val downloadDialogShown = downloadDialog.show()

            downloadDialogShown.getUrlButton.setOnClickListener {
                val urlByUser = mDialogView.getUrlEditText.text.toString()
                // TODO: Some how download files from URL to the device
                downloadDialogShown.dismiss()
                //Toast.makeText(this,urlByUser,Toast.LENGTH_SHORT).show()
            }
            downloadDialogShown.getUrlCancelButton.setOnClickListener {
                downloadDialogShown.dismiss()
            }


        }

    }
}