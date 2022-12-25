package com.example.gsm_bc2_android

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.bumptech.glide.Glide
import java.time.LocalDateTime
import java.util.*
import kotlin.concurrent.schedule
import com.example.gsm_bc2_android.databinding.GameBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn

class GameActivity : AppCompatActivity() {
    lateinit var db: Blockdb
    private lateinit var binding: GameBinding
    private val timer = Timer()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        setContentView(R.layout.game)
//        val hash_data = findViewById<TextView>(R.id.hash)
        binding = GameBinding.inflate(layoutInflater);
        setContentView(binding.root)

        val curUser = GoogleSignIn.getLastSignedInAccount(this)
        val current_email = curUser?.email.toString()

        val gif = findViewById<ImageView>(R.id.minigame_gif)
        Glide.with(this).load(R.raw.pickaxe).into(gif)

        //while(true) {
        val random = Random()
        val condition_num = random.nextInt(5)
        val condition_list = arrayOf("0이 4개 이상 포함", "1,2,3,4 전부 포함", "7이 3개 이상", "4가 4개", "1,2가 들어가지 않음")
        findViewById<TextView>(R.id.condition).text = condition_list[condition_num]

        binding.backbutton.setOnClickListener(){
            this.finish()
            timer.cancel()
            startActivity(Intent(this,HomeActivity::class.java))
        }

        var numbers = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)

        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries().build()

        timer.schedule(object : TimerTask() {
            override fun run() {
                numbers = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                var hash_msg = ""
                for (i: Int in 0..20) {
                    val temp = random.nextInt(16)
                    numbers[temp] += 1
                    hash_msg += Integer.toHexString(temp)
                }
                Log.d("hash data : ", hash_msg)
                runOnUiThread {
                    binding.hash.text = hash_msg

                    if(condition_num == 0){
                        if(numbers[0] >= 4) {
                            timer.cancel()
                            binding.reset.visibility = View.VISIBLE
                            binding.blockcoin.visibility = View.VISIBLE
                            db.userDao().AddAccountByEmail(current_email,10)
                        }
                    }
                    else if(condition_num == 1){
                        if(numbers[1] != 0 && numbers[2] != 0 && numbers[3] != 0 && numbers[4] != 0) {
                            timer.cancel()
                            binding.reset.visibility = View.VISIBLE
                            binding.blockcoin.visibility = View.VISIBLE
                            db.userDao().AddAccountByEmail(current_email,10)
                        }
                    }
                    else if(condition_num == 2){
                        if(numbers[7] >= 3) {
                            timer.cancel()
                            binding.reset.visibility = View.VISIBLE
                            binding.blockcoin.visibility = View.VISIBLE
                            db.userDao().AddAccountByEmail(current_email,10)
                        }
                    }
                    else if(condition_num == 3){
                        if(numbers[4] == 4) {
                            timer.cancel()
                            binding.reset.visibility = View.VISIBLE
                            binding.blockcoin.visibility = View.VISIBLE
                            db.userDao().AddAccountByEmail(current_email,10)
                        }
                    }
                    else if(condition_num == 4){
                        if(numbers[1] == 0 && numbers[2] == 0) {
                            timer.cancel()
                            binding.reset.visibility = View.VISIBLE
                            binding.blockcoin.visibility = View.VISIBLE
                            db.userDao().AddAccountByEmail(current_email,10)
                        }
                    }
                }
            }
        }, 1000, 1000)
        binding.reset.setOnClickListener(){
            this.finish()
            startActivity(Intent(this, GameActivity::class.java))
        }
    }
}