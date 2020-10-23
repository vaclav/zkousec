package cz.dobris.zkousec

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_question_pack_setup2.*

class QuestionPackSetup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_setup2)

        val fileName = intent.getStringExtra("FILE_NAME")


        if(fileName!=null) {
            val qp = Storage.loadQFile(fileName, this)
            Log.d("Zkousec", "Loaded: " + qp.id + ":" + qp.version)
            TitleText.text = fileName.replace(".xml","")
            StartButton.setOnClickListener {
                //TODO
            }
            DeleteButton.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete file")
                    .setMessage("Do you really want to delete the questions titled: " + fileName + "?")
                    .setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialog, which ->
                            Storage.deleteQFile(fileName, it.context)
                            val intent = Intent (this, MainActivity::class.java)
                            startActivity(intent)
                        })

                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()


            }
        }
    }
}