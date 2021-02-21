package cz.dobris.zkousec.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import cz.dobris.zkousec.R
import cz.dobris.zkousec.RecyclerAdapter
import cz.dobris.zkousec.fileStorage.Storage
import kotlinx.android.synthetic.main.activity_q_p_list.*
import kotlinx.android.synthetic.main.activity_question_pack_learning.*
import kotlinx.android.synthetic.main.url_dialog.*
import kotlinx.android.synthetic.main.url_dialog.view.*

class QPListActivity : AppCompatActivity() {

    private var titlesList = mutableListOf<String>()
    private var descriptionList = mutableListOf<String>()
    private var tagList = mutableListOf<String>()
    private var tagColorList = mutableListOf<Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_q_p_list)
        title = "Question Packs"
        val vibrator = this.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        bottomNavigationView.setSelectedItemId(R.id.ic_download)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0, 0)
                    true
                }
                R.id.ic_download -> {
                    true
                }
            }
            false
        }
        postToList()
        recycler_view.layoutManager = LinearLayoutManager(this)
        recycler_view.adapter = RecyclerAdapter(titlesList, descriptionList, tagList, tagColorList, vibrator)
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
                                (recycler_view.adapter as RecyclerAdapter).notifyDataSetChanged()
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
        for (fileName in listOfFiles) {
            addToList(fileName, "Description", "Study mode", 0)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.qp_list_menu, menu)
        val menuItem = menu!!.findItem(R.id.action_search)
        if (menuItem != null) {
            val searchView = menuItem.actionView as SearchView
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return true
                }
            })
        }
        return super.onCreateOptionsMenu(menu)
    }

}
