package com.sogoamobile.dvtweatherapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.sogoamobile.dvtweatherapp.R
import com.sogoamobile.dvtweatherapp.data.notes.Notes
import kotlinx.android.synthetic.main.item_notes.view.*


class NotesAdapter(
    var context: Context,
) :
    RecyclerView.Adapter<NotesAdapter.MyViewHolder>() {

    private var notesList = emptyList<Notes>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.item_notes, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        val currentItem = notesList[position]
        holder.itemView.txt_notes.text = currentItem.notes
        holder.itemView.txt_date_time.text = currentItem.date
    }

    fun setData(notes: List<Notes>) {
        this.notesList = notes
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return notesList.size
    }

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

}
