package com.example.gsm_bc2_android

import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.bumptech.glide.Glide
import com.example.gsm_bc2_android.databinding.GameBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.skydoves.balloon.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.DecimalFormat
import java.util.*

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
        binding.xmlUsername.text = curUser?.displayName.toString()
        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries().build()
        val t_dec_up = DecimalFormat("#,###")
        var current_account = t_dec_up.format(db.userDao().getAccountByEmail(current_email))
        binding.currentMoney.text = current_account+"원"

        //while(true) {
//        val condition_num = random.nextInt(5)
//        val condition_list = arrayOf("0이 4개 이상 포함", "1,2,3,4 전부 포함", "7이 3개 이상", "4가 4개", "1,2가 들어가지 않음")
        //findViewById<TextView>(R.id.condition).text = condition_list[condition_num]
        val condition_hash = "1,2,3,4,5,6,7,8,9가 모두 포함되어야 한다"

        binding.goHome.setOnClickListener(){
            this.finish()
            timer.cancel()
            startActivity(Intent(this,HomeActivity::class.java))
        }
        val balloon = Balloon.Builder(this)
            .setHeight(BalloonSizeSpec.WRAP)
            .setWidth(BalloonSizeSpec.WRAP)
            .setText(condition_hash)
            .setTextColorResource(R.color.background_color)
            .setTextSize(15f)
            .setTextTypeface(Typeface.BOLD)
            .setArrowPositionRules(ArrowPositionRules.ALIGN_ANCHOR)
            .setArrowSize(10)
            .setArrowSize(10)
            .setArrowPosition(0.5f)
            .setPadding(12)
            .setBackgroundColorResource(R.color.semipurple)
            .setBalloonAnimation(BalloonAnimation.OVERSHOOT)
            .setBalloonHighlightAnimation(BalloonHighlightAnimation.SHAKE)
            .build()

        binding.info.setOnClickListener(){
            Log.d("info button", Build.VERSION.SDK_INT.toString())
            balloon.showAlignTop(binding.info)
        }

        var retrofit = Retrofit.Builder()
            .baseUrl("http://10.82.20.85:3000")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        var transportservice: transportservice = retrofit.create(transportservice::class.java)

        var numbers = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        val random = Random()
        var temp_cnt = 2
        timer.schedule(object : TimerTask() {
            override fun run() {
                if(temp_cnt >=2 ){
                    runOnUiThread {
                        binding.blockcoin.visibility = View.GONE
                        numbers = arrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
                        var hash_msg = ""
                        for (i: Int in 0..20) {
                            val temp = random.nextInt(16)
                            numbers[temp] += 1
                            hash_msg += Integer.toHexString(temp)
                        }
                        Log.d("hash data : ", hash_msg)

                        binding.hash.text = hash_msg
//                        if(numbers[1] > 0 && numbers[2] > 0 && numbers[3] > 0 && numbers[4] > 0 && numbers[5] > 0 && numbers[6] > 0 && numbers[7] > 0 && numbers[8] > 0 && numbers[9] > 0) {
                        if(numbers[1] > 0 && numbers[2] > 0 && numbers[3] > 0) {
                            //timer.cancel()
                            //binding.blockcoin.visibility = View.VISIBLE
                            db.userDao().AddAccountByEmail(current_email,100)
                            val t_dec_up = DecimalFormat("#,###")
                            var current_account = db.userDao().getAccountByEmail(current_email)
                            binding.currentMoney.text = t_dec_up.format(current_account).toString()+"원"
                            binding.blockcoin.visibility = View.VISIBLE
                            temp_cnt = 0
                            Log.d("GameActivity","Hello")
                            transportservice.requestLogin(current_email, current_account, 100).enqueue(object:
                                Callback<transport>{
                                override fun onFailure(call: Call<transport>, t: Throwable) {
                                    Log.d("GameActivity","fail...")
                                }

                                override fun onResponse(call: Call<transport>, response: Response<transport>) {
                                    //var login = response.body()
                                    Log.d("GameActivity", response.body().toString())
                                }
                            })
                            Log.d("GameActivity","bye")
//                            val new_mining = block_tbl(mid,email,balance,menu,price,quantity)
//                            db.MiningDao().insertMining()

                        }

                    }
                }
                else{
                    temp_cnt += 2
                }
            }
        },                                                                                                                                                                                                                       0, 1000)
    }
}