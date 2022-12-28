package com.example.gsm_bc2_android

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.gsm_bc2_android.databinding.BlockBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

class BlockActivity : AppCompatActivity() {
    lateinit var profileAdapter: ProfileAdapter
    private val datas = mutableListOf<ProfileData>()

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

        initRecycler()
    }

    private fun initRecycler() {
        profileAdapter = ProfileAdapter(this)
        binding.block.adapter = profileAdapter
        //db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries()
        //    .build()
        datas.apply {
//            add(ProfileData(img = R.drawable.profile1, name = "mary", age = 24))
//            add(ProfileData(img = R.drawable.profile3, name = "jenny", age = 26))
//            add(ProfileData(img = R.drawable.profile2, name = "jhon", age = 27))
//            add(ProfileData(img = R.drawable.profile5, name = "ruby", age = 21))
//            add(ProfileData(img = R.drawable.profile4, name = "yuna", age = 23))
//            Log.d("in for each", db.userDao().getAllUser().toString())
            db.blockDao().getAllblock().forEach {
                add(ProfileData(bid = it.bid!!, email = it.email!!, balance = it.balance, menu=it.menu, price=it.price, quantity = it.quantity))
            }
            //add(ProfileData(bid = 1, email = "s21020@gsm.hs.kr", balance = 10000, menu="doritos", price=1500, quantity=2))
            profileAdapter.datas = datas
            profileAdapter.notifyDataSetChanged()

        }
    }
}