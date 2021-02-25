package cz.dobris.zkousec.adapters

import android.content.Intent
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import cz.dobris.zkousec.R
import cz.dobris.zkousec.activities.QPSetupActivity

class QPRecyclerAdapter (private var titles: List<String>, private var description: List<String>, private var tag: List<String>, private var tagColor: List<Int>, val vibrator: Vibrator) :
    RecyclerView.Adapter<QPRecyclerAdapter.ViewHolder>(){
   inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView){
       val itemTitle: TextView = itemView.findViewById(R.id.qp_card_title)
       val itemDescription: TextView = itemView.findViewById(R.id.qp_card_desription)
       val itemTag: Chip = itemView.findViewById(R.id.qp_card_tag)

       init {
           itemView.setOnClickListener {
               vibrator.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
               val intent = Intent(itemView.context,QPSetupActivity::class.java)
               intent.putExtra("FILE_NAME",titles[position])
               startActivity(itemView.context,intent,null)
           }

       }

   }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.qp_card,parent,false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.itemTitle.text = titles[position]
        holder.itemDescription.text = description[position]
        holder.itemTag.text = tag[position]
        /*
        holder.itemTag.background.setTint(
            when(tagColor[position]){

            }
        )
        */
    }

    override fun getItemCount(): Int {
        return titles.size
    }
}