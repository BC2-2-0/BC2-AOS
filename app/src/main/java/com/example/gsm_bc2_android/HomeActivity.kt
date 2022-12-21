package com.example.gsm_bc2_android

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.TextView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val user_name = intent.getStringExtra("username")
        Log.d("name_HomeActivity",user_name.toString())

        findViewById<TextView>(R.id.xml_username).text = user_name+"님\n안녕하세요!!"
    }
}

//class MainActivity : AppCompatActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.main)
//    }
//
//
//    }
//}