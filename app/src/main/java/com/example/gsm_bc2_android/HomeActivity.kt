package com.example.gsm_bc2_android

//import BottomSheetFragment

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.*
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.gsm_bc2_android.databinding.HomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomeBinding
    lateinit var db: Blockdb
    private var TAG: String = "HomeActivity"
    private lateinit var nfcPendingIntent: PendingIntent
    private lateinit var nfcAdapter: NfcAdapter
    private var auth : FirebaseAuth? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeBinding.inflate(layoutInflater);
        setContentView(binding.root)

        //왼쪽 위에 구글 사용자 이름+안녕하세요!! 띄어주기
        auth = Firebase.auth
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        val curUser = GoogleSignIn.getLastSignedInAccount(this)
        val name = curUser?.displayName.toString()
        val current_email = curUser?.email.toString()

        //Log.d("name_HomeActivity",name)
        findViewById<TextView>(R.id.xml_username).text = name+"님\n안녕하세요!!"

        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries().build()
        //db.userDao().insertUser(newUser)
        Log.d("test getuser",db.userDao().getAllUser().toString())
        var current_account = db.userDao().getAccountByEmail(current_email)
        findViewById<TextView>(R.id.current_money).text = current_account.toString()


        binding.payment.setOnClickListener {
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(supportFragmentManager, BottomSheetFragment.TAG)
        }

        binding.userPage.setOnClickListener(){
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        binding.game.setOnClickListener(){
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        val manager = getSystemService(Context.NFC_SERVICE) as NfcManager
        nfcAdapter = manager.defaultAdapter
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )


    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val detectedTag : Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        val writeValue : String = "{email:'s21020@gsm.hs.kr', account=50000}"
        val message: NdefMessage = createTagMessage(writeValue);

        if (detectedTag != null) {
            writeTag(message, detectedTag)
        };
    }
    override fun onResume() {
        super.onResume()
        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, null, null);
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter.disableForegroundDispatch(this);
    }
    private fun createTagMessage(msg: String): NdefMessage {
        return NdefMessage(NdefRecord.createUri(msg))
    }

    fun writeTag(message: NdefMessage, tag: Tag) {
        val size = message.toByteArray().size
        try {
            val ndef = Ndef.get(tag)
            if (ndef != null) {
                ndef.connect()
                if (!ndef.isWritable) {
                    Toast.makeText(applicationContext, "can not write NFC tag", Toast.LENGTH_SHORT).show()
                }
                if (ndef.maxSize < size) {
                    Toast.makeText(applicationContext, "NFC tag size too large", Toast.LENGTH_SHORT).show()
                }
                ndef.writeNdefMessage(message)
                Toast.makeText(applicationContext, "NFC tag is writted", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            //Log.i(TAG,e.message);
        }
    }

}