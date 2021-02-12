package cz.dobris.zkousec.mode

import android.os.Handler
import android.util.Log
import android.view.View
import com.google.android.material.chip.Chip
import cz.dobris.zkousec.db.DBHelper
import cz.dobris.zkousec.domain.TestSession
import kotlinx.android.synthetic.main.activity_question_pack_testing.*
import kotlin.concurrent.thread

class ModeHelper(
    var session: TestSession
) {
    fun setAnswerButtonsText(chip1: Chip, chip2: Chip, chip3: Chip, chip4: Chip) {
        chip1.visibility = View.GONE
        chip2.visibility = View.GONE
        chip3.visibility = View.GONE
        chip4.visibility = View.GONE
        for (i in 1..session.nextQuestion().question.answers.size) {
            Log.d("ModeHelper", i.toString())
            when (i) {
                1 -> {
                    chip1.text = session.nextQuestion().question.answers[0].toString()
                    chip1.visibility = View.VISIBLE
                }
                2 -> {
                    chip2.text = session.nextQuestion().question.answers[1].toString()
                    chip2.visibility = View.VISIBLE
                }
                3 -> {
                    chip3.text = session.nextQuestion().question.answers[2].toString()
                    chip3.visibility = View.VISIBLE
                }
                4 -> {
                    chip4.text = session.nextQuestion().question.answers[3].toString()
                    chip4.visibility = View.VISIBLE
                }
            }
        }
    }
}