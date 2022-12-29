package com.example.gsm_bc2_android

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView

class ProfileAdapter2(private val context: Context) : RecyclerView.Adapter<ProfileAdapter2.ViewHolder>() {

    var datas = mutableListOf<ProfileData2>()
    lateinit var db: Blockdb
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recyclerview2,parent,false)
        return ViewHolder(view)
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(datas[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

        //private val imgProfile: ImageView = itemView.findViewById(R.id.img_rv_photo)
        private val mid: TextView = itemView.findViewById(R.id.mid)
        private val email: TextView = itemView.findViewById(R.id.email)
        private val balance: TextView = itemView.findViewById(R.id.balance)
        private val charged_money: TextView = itemView.findViewById(R.id.charged_money)
        private val block: ConstraintLayout = itemView.findViewById(R.id.block)

        fun bind(item: ProfileData2) {
            mid.text = item.mid.toString()
            email.text = item.email
            balance.text = item.balance.toString()
            charged_money.text = item.charged_money.toString()
            if(item.type == "mine"){
                block.setBackgroundResource(R.drawable.button2)
            }
            else if(item.type == "others"){
                block.setBackgroundResource(R.drawable.square2)
            }
        }
    }

    override fun getItemCount() = datas.size


}