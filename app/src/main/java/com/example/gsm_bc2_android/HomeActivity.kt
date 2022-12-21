package com.example.gsm_bc2_android

import BottomSheetFragment
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        //왼쪽 위에 구글 사용자 이름+안녕하세요 띄어주기
        val user_name = intent.getStringExtra("username")
        Log.d("name_HomeActivity",user_name.toString())
        findViewById<TextView>(R.id.xml_username).text = user_name+"님\n안녕하세요!!"

        findViewById<Button>(R.id.payment).setOnClickListener {
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(supportFragmentManager, BottomSheetFragment.TAG)
        }
    }
}