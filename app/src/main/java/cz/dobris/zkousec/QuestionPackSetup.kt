package cz.dobris.zkousec

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_question_pack_setup2.*

class QuestionPackSetup : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_question_pack_setup2)

        val fileName = intent.getStringExtra("FILE_NAME")
        if(fileName!=null) {
            StartButton.setOnClickListener {
                Storage.loadQFile(fileName, it.context)
                //TODO
            }

            DeleteButton.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete file")
                    .setMessage("Do you really want to delete the questions titled: " + fileName + "?")
                    .setPositiveButton("Yes",
                        DialogInterface.OnClickListener { dialog, which ->
                            Storage.deleteQFile(fileName, it.context)
                        })
                    .setNegativeButton("No", DialogInterface.OnClickListener { dialog, which ->
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        }
    }
}