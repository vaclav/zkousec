package cz.dobris.zkousec.mode

import android.widget.TextView
import com.google.android.material.chip.Chip
import cz.dobris.zkousec.domain.TestSession
import kotlinx.android.synthetic.main.activity_question_pack_testing.*

class ModeHelper(
    var session: TestSession,
    //Text answer
    var AnswerChip1 : Chip,
    var AnswerChip2 : Chip,
    var AnswerChip3 : Chip,
    var AnswerChip4 : Chip,
    //todo img answer
    var QuestionText : TextView,
    var RemainingQuestionsText : TextView
) {
    // TODO:
    fun setAnswerChipsColorToDefault(){
        AnswerChip1.background.setTintList(null)
        AnswerChip2.background.setTintList(null)
        AnswerChip3.background.setTintList(null)
        AnswerChip4.background.setTintList(null)
    }




}