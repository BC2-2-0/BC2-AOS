package com.example.gsm_bc2_android

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import androidx.room.Room
import com.example.gsm_bc2_android.databinding.BlockBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text

class BlockActivity : AppCompatActivity() {
    lateinit var profileAdapter: ProfileAdapter
    lateinit var profileAdapter2: ProfileAdapter2
//    private val datas = mutableListOf<ProfileData>()
//    private val datas2 = mutableListOf<ProfileData2>()

    lateinit var db: Blockdb

    private lateinit var binding: BlockBinding

    private var auth: FirebaseAuth? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = BlockBinding.inflate(layoutInflater);
        setContentView(binding.root)


        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries()
            .build()
        binding.backbutton.setOnClickListener {
            this.finish()
            startActivity(Intent(this, HomeActivity::class.java))
        }

        //chargedRecycler()
        initRecycler()

        binding.chargeDesc.setOnClickListener(){
            Log.d("BlockActivity","ChargetDesc click!")
            binding.chargeDesc.setTextColor(Color.parseColor("#000000"))
            binding.pointDesc.setTextColor(Color.parseColor("#66000000"))
            chargedRecycler()
        }
        binding.pointDesc.setOnClickListener(){
            Log.d("BlockActivity","PointDesc click!")
            binding.chargeDesc.setTextColor(Color.parseColor("#66000000"))
            binding.pointDesc.setTextColor(Color.parseColor("#000000"))
            initRecycler()
        }
    }

    private fun initRecycler() {
        val datas = mutableListOf<ProfileData>()
        profileAdapter = ProfileAdapter(this)
        binding.block.adapter = profileAdapter
        //db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries()
        //    .build()
        datas.apply {
            db.blockDao().getAllblock().forEach {
                add(ProfileData(bid = it.bid!!, email = it.email!!, balance = it.balance, menu=it.menu, price=it.price, quantity = it.quantity))
            }
            //add(ProfileData(bid = 1, email = "s21020@gsm.hs.kr", balance = 10000, menu="doritos", price=1500, quantity=2))
            profileAdapter.datas = datas
            profileAdapter.notifyDataSetChanged()

        }
    }

    private fun chargedRecycler() {
        val datas2 = mutableListOf<ProfileData2>()
        profileAdapter2 = ProfileAdapter2(this)
        binding.block.adapter = profileAdapter2
        //db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries()
        //    .build()
        datas2.apply {
            db.MiningDao().getAllMining().forEach {
                add(ProfileData2(mid = it.mid!!, email = it.email!!, balance = it.balance, charged_money = it.charged_money))
            }
//            add(ProfileData2(mid = 1, email = "s21020@gsm.hs.kr", balance = 10000, charged_money = 5000))
//            add(ProfileData2(mid = 1, email = "s21020@gsm.hs.kr", balance = 10000, charged_money = 5000))
//            add(ProfileData2(mid = 1, email = "s21020@gsm.hs.kr", balance = 10000, charged_money = 5000))
//            add(ProfileData2(mid = 1, email = "s21020@gsm.hs.kr", balance = 10000, charged_money = 5000))
            profileAdapter2.datas = datas2
            profileAdapter2.notifyDataSetChanged()

        }
    }
}