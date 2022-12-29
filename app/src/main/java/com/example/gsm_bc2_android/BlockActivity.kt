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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.util.Base64Utils
import com.google.firebase.auth.FirebaseAuth
import org.w3c.dom.Text
import java.security.DigestException
import java.security.MessageDigest

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

        binding.chargeDesc.setOnClickListener() {
            Log.d("BlockActivity", "ChargetDesc click!")
            binding.chargeDesc.setTextColor(Color.parseColor("#000000"))
            binding.pointDesc.setTextColor(Color.parseColor("#66000000"))
            chargedRecycler()
        }
        binding.pointDesc.setOnClickListener() {
            Log.d("BlockActivity", "PointDesc click!")
            binding.chargeDesc.setTextColor(Color.parseColor("#66000000"))
            binding.pointDesc.setTextColor(Color.parseColor("#000000"))
            initRecycler()
        }
    }

    private fun initRecycler() {
        val datas = mutableListOf<ProfileData>()
        profileAdapter = ProfileAdapter(this)
        binding.block.adapter = profileAdapter
        val curUser = GoogleSignIn.getLastSignedInAccount(this)
        val current_email = curUser?.email.toString()
        var range_hash = IntRange(0, 16)
        //db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries()
        //    .build()
        datas.apply {

            db.blockDao().getAllblock().forEach {
                if (current_email == it.email) {
                    add(
                        ProfileData(
                            type = "mine",
                            bid = it.bid!!,
                            email = hash(it.email!!).slice(range_hash),
                            balance = it.balance,
                            menu = it.menu,
                            price = it.price,
                            quantity = it.quantity
                        )
                    )
                } else {
                    add(
                        ProfileData(
                            type = "others",
                            bid = it.bid!!,
                            email = hash(it.email!!).slice(range_hash),
                            balance = it.balance,
                            menu = it.menu,
                            price = it.price,
                            quantity = it.quantity
                        )
                    )
                }
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
        val curUser = GoogleSignIn.getLastSignedInAccount(this)
        val current_email = curUser?.email.toString()
        //db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries()
        //    .build()
        var range_hash = IntRange(0, 16)
        datas2.apply {
            db.MiningDao().getAllMining().forEach {
                if (current_email == it.email) {
                    add(
                        ProfileData2(
                            type = "mine",
                            mid = it.mid!!,
                            email = hash(it.email!!).slice(range_hash),
                            balance = it.balance,
                            charged_money = it.charged_money
                        )
                    )
                } else {
                    add(
                        ProfileData2(
                            type = "others",
                            mid = it.mid!!,
                            email = hash(it.email!!).slice(range_hash),
                            balance = it.balance,
                            charged_money = it.charged_money
                        )
                    )
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
    fun getSign(input: String): String{
        val hash: ByteArray
        try{
            val md = MessageDigest.getInstance("SHA-256")
            md.update(input.toByteArray())
            hash = md.digest()
        }catch (e: CloneNotSupportedException){
            throw DigestException("couldn't make digest of patial content")
        }
        return Base64Utils.encode(hash)
    }
    fun hash(input: String): String {
        val bytes = input.toByteArray()
        val md = MessageDigest.getInstance("SHA-256")
        val digest = md.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}