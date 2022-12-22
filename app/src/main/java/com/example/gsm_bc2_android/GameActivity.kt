package com.example.gsm_bc2_android

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game)
        val gif = findViewById<ImageView>(R.id.minigame_gif)
        Glide.with(this).load(R.raw.pickaxe).into(gif)
    }
}