package com.example.simpandata

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.simpandata.model.JsonModel
import com.google.android.material.imageview.ShapeableImageView

class PersonAdapter(private val personList: List<JsonModel>) :
    RecyclerView.Adapter<PersonAdapter.MyViewHolder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =  LayoutInflater.from(parent.context).inflate(R.layout.item_layout,
            parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return personList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val person = personList.get(position)
        holder.tvName.text = (person.name)
        holder.tvAge.text = (person.age.toString())
        holder.tvJob.text = (person.job)
    }

    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){

        val tvName : TextView = itemView.findViewById(R.id.tvName)
        val tvAge : TextView = itemView.findViewById(R.id.tvAge)
        val tvJob : TextView = itemView.findViewById(R.id.tvJob)
    }

}