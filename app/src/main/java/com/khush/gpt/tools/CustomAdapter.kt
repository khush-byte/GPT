package com.khush.gpt.tools

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.khush.gpt.R

class CustomAdapter(context: Context, var list: ArrayList<MyData>) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val yourContext: Context = context

    private inner class ViewHolder1(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var text: TextView = itemView.findViewById(R.id.chatText)
        fun bind(position: Int) {
            val recyclerViewModel = list[position]
            text.text = recyclerViewModel.text
        }
    }

    private inner class ViewHolder2(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var text: TextView = itemView.findViewById(R.id.chatText)
        fun bind(position: Int) {
            val recyclerViewModel = list[position]
            text.text = recyclerViewModel.text
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1) {
            return ViewHolder1(
                LayoutInflater.from(yourContext).inflate(R.layout.chat_bot, parent, false)
            )
        }
        return ViewHolder2(
            LayoutInflater.from(yourContext).inflate(R.layout.chat_user, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (list[position].type == 1) {
            (holder as ViewHolder1).bind(position)
        } else {
            (holder as ViewHolder2).bind(position)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return list[position].type
    }
}