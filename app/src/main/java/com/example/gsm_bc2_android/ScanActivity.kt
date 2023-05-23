package com.example.gsm_bc2_android


import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.gsm_bc2_android.databinding.ScanBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.zxing.integration.android.IntentIntegrator
import com.google.zxing.integration.android.IntentResult
import com.journeyapps.barcodescanner.CaptureManager
import kr.co.bootpay.android.Bootpay
import kr.co.bootpay.android.events.BootpayEventListener
import kr.co.bootpay.android.models.BootExtra
import kr.co.bootpay.android.models.BootItem
import kr.co.bootpay.android.models.BootUser
import kr.co.bootpay.android.models.Payload
import org.json.JSONObject

class ScanActivity : AppCompatActivity() {

    private lateinit var binding: ScanBinding
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var db: Blockdb
    private var auth: FirebaseAuth? = null
    private lateinit var capture: CaptureManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ScanBinding.inflate(layoutInflater);
        setContentView(binding.root)
        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries()
            .build()

        capture = CaptureManager(this, binding.decoratedBarCodeView)
        // intent와 savedInstanceState를 넣어줍니다.
        capture.initializeFromIntent(intent, savedInstanceState)
        capture.decode() //decode


        binding.cancleButton.setOnClickListener {
            this.finish()
            startActivity(Intent(this, HomeActivity::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        capture.onResume()
    }
    override fun onPause() {
        super.onPause()
        capture.onPause()
    }
    override fun onDestroy() {
        super.onDestroy()
        capture.onDestroy()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        capture.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


}