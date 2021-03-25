package cz.dobris.zkousec.activities

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.opengl.Visibility
import android.os.Bundle
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import cz.dobris.zkousec.R
import cz.dobris.zkousec.adapters.QPRecyclerAdapter
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import cz.dobris.zkousec.fileStorage.Storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_question_pack_testing.*
import kotlinx.android.synthetic.main.url_dialog.*
import kotlinx.android.synthetic.main.url_dialog.view.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {

    private var titlesList = mutableListOf<String>()
    private var descriptionList = mutableListOf<String>()
    private var tagList = mutableListOf<String>()
    private var tagColorList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "Home"
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        postToList()
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = QPRecyclerAdapter(titlesList, descriptionList, tagList, tagColorList, vibrator)
        recycler_view.setOnClickListener {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
        }
        floatingActionButton.setOnClickListener {v->
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
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
                            v.post {
                                (recycler_view.adapter as QPRecyclerAdapter).notifyDataSetChanged()
                                postToList()
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            v.post { Toast.makeText(this, "Cannot download the file. " + e.message, Toast.LENGTH_SHORT).show() }
                        }
                    }).start()
                } catch (e: IllegalArgumentException) {
                    AlertDialog.Builder(this)
                        .setTitle("Error")
                        .setMessage(e.message)
                        .setPositiveButton("OK", { dialog, which -> dialog.dismiss() })
                        .show()
                }
            }
            downloadDialogShown.getUrlCancelButton.setOnClickListener {
                downloadDialogShown.dismiss()
            }

        }

    }
    private fun addToList(title: String, description: String, tag: String, tagColor: Int) {
        titlesList.add(title)
        descriptionList.add(description)
        tagList.add(tag)
        tagColorList.add(tagColor)
    }

    private fun postToList() {
        titlesList.clear()
        descriptionList.clear()
        tagList.clear()
        tagColorList.clear()

        val listOfFiles = Storage.listQFiles(this)
        // TODO: 25.03.2021 simplify
        for (fileName in listOfFiles) {
            var studyModeText = "null"
            do {
                thread {
                    if (DBHelper.existsTestSession(this, fileName)) {
                        studyModeText = if (DBHelper.getTestSession(this, fileName).learnMode)
                            "Learn mode"
                        else
                            "Test mode"
                    }else
                        studyModeText = ""
                }
            }while (studyModeText == "null")
            addToList(fileName, Storage.loadQFile(fileName,this).description, studyModeText, 0)


        }
    }






    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }


}