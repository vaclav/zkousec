package cz.dobris.zkousec.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import cz.dobris.zkousec.R
import kotlinx.android.synthetic.main.activity_q_p_list.*

class QPListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_q_p_list)
        title = "Question Packs"

        bottomNavigationView.setSelectedItemId(R.id.ic_download)
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ic_home -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    overridePendingTransition(0,0)
                    true
                }
                R.id.ic_download -> {
                    true
                }
            }
            false
        }

    }
}