package cz.dobris.zkousec

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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

    var arrayAdapter: ArrayAdapter<String>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // TODO: Add button and img to each item in the list

        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listOfButtons.adapter = arrayAdapter
        refreshListOfQuestionPacks(arrayAdapter!!)

        listOfButtons.setOnItemClickListener { adapterView, view, position, id ->
            val intent = Intent (this, QuestionPackSetup::class.java)
            val item = arrayAdapter!!.getItem(position)
            intent.putExtra("FILE_NAME", item);
            startActivity (intent)
        }

        //This part of code shows dialog to user with text field for downloading Question Pack from URL.
        //URL is saved in "urlByUser"
        addQuestionPackButton.setOnClickListener{v ->
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.url_dialog, null)
            val downloadDialog = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Download Question Pack")
                val downloadDialogShown = downloadDialog.show()

            downloadDialogShown.getUrlButton.setOnClickListener {
                val urlByUser = mDialogView.getUrlEditText.text.toString()
                downloadDialogShown.dismiss()
                //Toast.makeText(this,urlByUser,Toast.LENGTH_SHORT).show()
                try {
                    Thread(Runnable {
                        Log.d("Zkousec", "Running in a new thread!")
                        try {
                            Storage.saveQFileFromUrl(urlByUser, it.context)
                            v.post { refreshListOfQuestionPacks(arrayAdapter!!) }
                        }catch (e : Exception) {
                            //TODO handle the exception
                            e.printStackTrace()
                        }
                    }).start()
                } catch (e: IllegalArgumentException) {
                    AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(e.message)
                        .setPositiveButton("OK", {dialog, which -> dialog.dismiss()})
                        .show()
                }
            }
            downloadDialogShown.getUrlCancelButton.setOnClickListener {
                downloadDialogShown.dismiss()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        refreshListOfQuestionPacks(arrayAdapter!!)
    }

    fun refreshListOfQuestionPacks(arrayAdapter: ArrayAdapter<String>) {
        val listOfFiles = Storage.listQFiles(this)
        arrayAdapter.clear()
        if(listOfFiles.size==0) {
            QuestionPacksOnTheDeviceText.text = "No question packs installed yet!"
            QuestionPacksOnTheDeviceText.setTextColor(Color.RED)
            QuestionPacksOnTheDeviceText.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
        } else {
            QuestionPacksOnTheDeviceText.visibility = View.GONE
            imageView.visibility = View.GONE
        }
        for (fileName in listOfFiles) {
            arrayAdapter.add(fileName)
            Log.d("Zkousec", fileName + ":" + arrayAdapter.javaClass)
        }
        arrayAdapter.notifyDataSetChanged()
    }

}