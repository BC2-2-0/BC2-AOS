package com.example.gsm_bc2_android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ProfileAdapter(private val context: Context) : RecyclerView.Adapter<ProfileAdapter.ViewHolder>() {

    var datas = mutableListOf<ProfileData>()
    lateinit var db: Blockdb
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recyclerview,parent,false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        //private val imgProfile: ImageView = itemView.findViewById(R.id.img_rv_photo)
        private val uid: TextView = itemView.findViewById(R.id.uid)
        private val email: TextView = itemView.findViewById(R.id.email)
        private val account: TextView = itemView.findViewById(R.id.balance)

        fun bind(item: ProfileData) {
            uid.text = item.uid.toString()
            email.text = item.email
            account.text = item.account.toString()
            //Glide.with(itemView).load(item.img).into(imgProfile)
        }
    }

    override fun getItemCount() = datas.size


}