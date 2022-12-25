package com.example.gsm_bc2_android

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.gsm_bc2_android.databinding.UserBinding
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseAuth

class UserActivity : AppCompatActivity() {
    lateinit var profileAdapter: ProfileAdapter
    private val datas = mutableListOf<ProfileData>()

    lateinit var db: Blockdb

    private lateinit var binding: UserBinding

    private var auth: FirebaseAuth? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = UserBinding.inflate(layoutInflater);
        setContentView(binding.root)


        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries()
            .build()

        binding.backbutton.setOnClickListener {
            this.finish()
            startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.delete.setOnClickListener {
            Log.d("Before delete table", db.userDao().getAllUser().toString())
            db.userDao().deleteAllUser()
            Log.d("After delete table", db.userDao().getAllUser().toString())
        }

        initRecycler()
    }

    private fun initRecycler() {
        profileAdapter = ProfileAdapter(this)
        binding.rvProfile.adapter = profileAdapter
        //db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries()
        //    .build()
        datas.apply {
//            add(ProfileData(img = R.drawable.profile1, name = "mary", age = 24))
//            add(ProfileData(img = R.drawable.profile3, name = "jenny", age = 26))
//            add(ProfileData(img = R.drawable.profile2, name = "jhon", age = 27))
//            add(ProfileData(img = R.drawable.profile5, name = "ruby", age = 21))
//            add(ProfileData(img = R.drawable.profile4, name = "yuna", age = 23))
//            Log.d("in for each", db.userDao().getAllUser().toString())
            db.userDao().getAllUser().forEach {
                Log.d("in for each", it.toString())
                add(ProfileData(uid = it.uid!!, email = it.email!!, account = it.account))
            }
            //add(ProfileData(uid = 1, email = "hi", account = 10000))
            profileAdapter.datas = datas
            profileAdapter.notifyDataSetChanged()

        }
    }
}