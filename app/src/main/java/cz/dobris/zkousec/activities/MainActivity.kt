package cz.dobris.zkousec.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import cz.dobris.zkousec.R
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.fileStorage.Storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.url_dialog.*
import kotlinx.android.synthetic.main.url_dialog.view.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    /*
    TODO:
        - Make card that shows last test at the top functional
        - Add settings button at the top right
    */

    lateinit var arrayAdapter: ArrayAdapter<String>
    var lastQuestionPackId : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listOfButtons.adapter = arrayAdapter
        refreshListOfQuestionPacks(arrayAdapter)

        listOfButtons.setOnItemClickListener { adapterView, view, position, id ->
            val intent = Intent (this, QPSetupActivity::class.java)
            val item = arrayAdapter.getItem(position)
            intent.putExtra("FILE_NAME", item)
            lastQuestionPackId = item
            startActivity (intent)
        }

        //This part of code shows dialog to user with text field for downloading Question Pack from URL.
        addQuestionPackButton.setOnClickListener{v ->
            val mDialogView = LayoutInflater.from(this).inflate(R.layout.url_dialog, null)
            val downloadDialog = AlertDialog.Builder(this)
                .setView(mDialogView)
                .setTitle("Download Question Pack")
                val downloadDialogShown = downloadDialog.show()

            downloadDialogShown.getUrlButton.setOnClickListener {
                val value = mDialogView.getUrlEditText.text.toString()
                val urlByUser = if (value.trim().length > 0) value else "http://gpars.org/sample.xml"
                val testName = mDialogView.getTestName.text.toString().trim()
                downloadDialogShown.dismiss()
                try {
                    Thread(Runnable {
                        Log.d("Zkousec", "Running in a new thread!")
                        try {
                            Storage.saveQFileFromUrl(urlByUser, testName, it.context)
                            v.post { refreshListOfQuestionPacks(arrayAdapter) }
                        }catch (e : Exception) {
                            e.printStackTrace()
                            v.post { Toast.makeText(this, "Cannot download the file. " + e.message ,Toast.LENGTH_SHORT).show() }
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
        refreshListOfQuestionPacks(arrayAdapter)
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

        Log.d("Zkousec", "Last session: " + lastQuestionPackId)
        if (lastQuestionPackId!=null) {
            Card_qp_nameText.text = lastQuestionPackId
            val handler =  Handler()
            thread {
                val session = DBHelper.getTestSession(this, lastQuestionPackId!!)
                handler.post {
                    Card_qp_RAText.text = session.remainingQuestions().toString()
                    Card_qp_CAText.text = session.correctlyAnsweredQuestions().size.toString()
                    Card_qp_ICAText.text = session.incorrectlyAnsweredQuestions().size.toString()
                    //TODO store and recover the time
                    //TODO store the lastQuestionPackId in the db
                }
            }
        }
        arrayAdapter.notifyDataSetChanged()
    }

}