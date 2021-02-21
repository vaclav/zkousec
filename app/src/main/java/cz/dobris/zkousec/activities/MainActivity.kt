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
import com.google.android.material.bottomnavigation.BottomNavigationView
import cz.dobris.zkousec.R
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.fileStorage.Storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_question_pack_testing.*
import kotlinx.android.synthetic.main.url_dialog.*
import kotlinx.android.synthetic.main.url_dialog.view.*
import kotlin.concurrent.thread


class MainActivity : AppCompatActivity() {
    /*
    TODO:
        - Add settings button at the top right
    */

    lateinit var arrayAdapter: ArrayAdapter<String>
    var lastQuestionPackId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        loadLastQPid()
        title = "Home"
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        refreshListOfQuestionPacks()
        /*
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        listOfButtons.adapter = arrayAdapter

        listOfButtons.setOnItemClickListener { adapterView, view, position, id ->
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            val intent = Intent(this, QPSetupActivity::class.java)
            val item = arrayAdapter.getItem(position)
            intent.putExtra("FILE_NAME", item)
            lastQuestionPackId = item
            saveLastQPid()
            startActivity(intent)
        }*/

        //This part of code shows dialog to user with text field for downloading Question Pack from URL.
        /*
        addQuestionPackButton.setOnClickListener { v ->
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
                            //v.post { refreshListOfQuestionPacks(arrayAdapter) }
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
        }*/
        bottomNavigationView.setSelectedItemId(R.id.ic_home)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_home -> {
                    true
                }
                R.id.ic_download -> {
                    startActivity(Intent(this, QPListActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
            }
            false
        }
        cardView_recentlyUsedQP.setOnClickListener {
            vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            val intent = Intent(this, QPSetupActivity::class.java)
            intent.putExtra("FILE_NAME", lastQuestionPackId)
            startActivity(intent)
        }
    }

    private fun saveLastQPid() {
        val sharedPreferences = getSharedPreferences("lastQPcardPref", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply() {
            putString("lastQPcardString", lastQuestionPackId)
        }.apply()
    }

    private fun loadLastQPid() {
        val sharedPreferences = getSharedPreferences("lastQPcardPref", Context.MODE_PRIVATE)
        lastQuestionPackId = sharedPreferences.getString("lastQPcardString", null)
        if (Storage.listQFiles(this).size == 0) lastQuestionPackId = null
    }


    override fun onStart() {
        super.onStart()
    }

        fun refreshListOfQuestionPacks() {
        val listOfFiles = Storage.listQFiles(this)
        //arrayAdapter.clear()
        if (listOfFiles.size == 0) {
            QuestionPacksOnTheDeviceText.text = "No question packs installed yet!"
            QuestionPacksOnTheDeviceText.setTextColor(Color.RED)
            QuestionPacksOnTheDeviceText.visibility = View.VISIBLE
            imageView.visibility = View.VISIBLE
        } else {
            QuestionPacksOnTheDeviceText.visibility = View.GONE
            imageView.visibility = View.GONE
        }

        if (lastQuestionPackId != null && listOfFiles.any { it.equals(lastQuestionPackId) }) {
            Card_qp_nameText.text = lastQuestionPackId
            val handler = Handler()
            thread {
                val session = DBHelper.getTestSession(this, lastQuestionPackId!!)
                handler.post {
                    Card_qp_RAText.text = session.remainingQuestions().toString()
                    Card_qp_CAText.text = session.correctlyAnsweredQuestions().size.toString()
                    Card_qp_ICAText.text = session.incorrectlyAnsweredQuestions().size.toString()
                    recentlyUsedQPHeaderTextView.visibility = View.VISIBLE
                    cardView_recentlyUsedQP.visibility = View.VISIBLE
                    //TODO store and recover the time
                    //TODO store the lastQuestionPackId in the db
                }
            }
        } else {
            recentlyUsedQPHeaderTextView.visibility = View.GONE
            cardView_recentlyUsedQP.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_activity_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_main_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }

    }
}