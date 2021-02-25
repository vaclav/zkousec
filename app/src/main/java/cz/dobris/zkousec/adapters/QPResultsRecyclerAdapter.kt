package cz.dobris.zkousec.adapters

import android.content.Context
import android.os.Vibrator
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.os.persistableBundleOf
import androidx.recyclerview.widget.RecyclerView
import cz.dobris.zkousec.R
import kotlinx.android.synthetic.main.qp_result_card.view.*
import layout.Answer
import layout.Question

class QPResultsRecyclerAdapter(private var question: List<String>, private var type: List<String>, val vibrator: Vibrator?) :
    RecyclerView.Adapter<QPResultsRecyclerAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val itemQuestion: TextView = itemView.findViewById(R.id.qp_result_card_Q_text)
        val itemImgCorrect:ImageView = itemView.findViewById(R.id.qp_result_card_correctIMG)
        val itemImgInCorrect:ImageView = itemView.findViewById(R.id.qp_result_card_inCorrectIMG)

        init {
            itemView.setOnClickListener {
                // TODO: 25.02.2021 Show correct answer
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.qp_result_card, parent, false)
        return ViewHolder(v)
    }

    override fun getItemCount(): Int {
        return question.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemQuestion.text = question[position]
        if (type[position].equals("correct")){
            holder.itemImgInCorrect.visibility = View.INVISIBLE
            holder.itemImgCorrect.visibility = View.VISIBLE

            Log.d("Results","img invisible")
        }
        if (type[position].equals("incorrect")){
            holder.itemImgCorrect.visibility = View.INVISIBLE
            holder.itemImgInCorrect.visibility = View.VISIBLE
        }
    }
    private fun findAnswer(q: Question, correct: Boolean): Answer {
        for (answer in q.answers) {
            if (answer.correct == correct)
                return answer
        }
        throw java.lang.IllegalArgumentException("Question '${q.text}', position: ${q.position} has no ${if (correct) "right" else "wrong"} answers")
    }

}