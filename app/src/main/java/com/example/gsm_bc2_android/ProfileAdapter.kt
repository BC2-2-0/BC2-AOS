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
        private val bid: TextView = itemView.findViewById(R.id.bid)
        private val email: TextView = itemView.findViewById(R.id.email)
        private val balance: TextView = itemView.findViewById(R.id.balance)
        private val menu: TextView = itemView.findViewById(R.id.menu)
        private val price: TextView = itemView.findViewById(R.id.price)
        private val quantity: TextView = itemView.findViewById(R.id.quantity)

        fun bind(item: ProfileData) {
            bid.text = item.bid.toString()
            email.text = item.email
            balance.text = item.balance.toString()
            menu.text = item.menu
            price.text = item.price.toString()
            quantity.text = item.quantity.toString()
            //Glide.with(itemView).load(item.img).into(imgProfile)
        }
    }

    override fun getItemCount() = datas.size


}