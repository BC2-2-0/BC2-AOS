package com.example.gsm_bc2_android

//import BottomSheetFragment

import android.R.attr.path
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
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.gsm_bc2_android.databinding.HomeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okhttp3.sse.EventSources.createFactory
import okio.IOException
import java.util.concurrent.TimeUnit


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomeBinding
    lateinit var db: Blockdb
    private var TAG: String = "HomeActivity"
    private lateinit var nfcPendingIntent: PendingIntent
    private lateinit var nfcAdapter: NfcAdapter
    private var auth : FirebaseAuth? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    //private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeBinding.inflate(layoutInflater);
        setContentView(binding.root)

        //sse
        val eventSourceListener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                super.onOpen(eventSource, response)
                Log.d(TAG, "Connection Opened")
            }

            override fun onClosed(eventSource: EventSource) {
                super.onClosed(eventSource)
                Log.d(TAG, "Connection Closed")
            }

            override fun onEvent(
                eventSource: EventSource,
                id: String?,
                type: String?,
                data: String
            ) {
                super.onEvent(eventSource, id, type, data)
                Log.d(TAG, "On Event Received! Data -: $data")
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                super.onFailure(eventSource, t, response)
                Log.d(TAG, "On Failure -: ${response?.body}")
            }
        }

        val client = OkHttpClient.Builder().connectTimeout(5, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.MINUTES)
            .writeTimeout(10, TimeUnit.MINUTES)
            .build()

        val request = Request.Builder()
            .url("http://10.82.20.0:3000/receive") // URL
            .header("Accept", "application/json; q=0.5")
            .addHeader("Accept", "text/event-stream")
            .build()

        EventSources.createFactory(client)
            .newEventSource(request = request, listener = eventSourceListener)

        lifecycleScope.launchWhenCreated {
            withContext(Dispatchers.IO) {
                client.newCall(request).enqueue(responseCallback = object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        Log.e(TAG, "API Call Failure ${e.localizedMessage}")
                    }

                    override fun onResponse(call: Call, response: Response) {
                        Log.d(TAG, "APi Call Success ${response.body.toString()}")
                    }
                })
            }
        }
        //sse end

//        bottomSheetBehavior.BottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
//        override fun onStateChanged(bottomSheet: View, newState: Int) {
//            // 상태가 변함에 따라서 할일들을 적어줍니다.
//            if (newState == STATE_EXPANDED) {
//                // TODO; 내용을 보여주기 위해 fragment 붙이기...
//            }
//        }

//        bottomSheetBehavior.addBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback(){
//            override fun onStateChanged(bottomSheet: View, newState: Int) {
//                if (newState == STATE_EXPANDED){
//
//                }
//                else if (newState == STATE_HIDDEN){
//
//                }
//            }
//        })


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
        Log.d("curUser",name+current_email)
        //Log.d("name_HomeActivity",name)
        findViewById<TextView>(R.id.xml_username).text = name
        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries().build()
        //db.userDao().insertUser(newUser)
        Log.d("test getuser-h",db.userDao().getAllUser().toString())
        var current_account = db.userDao().getAccountByEmail(current_email)
        Log.d("test_getaccount",current_account.toString())
        findViewById<TextView>(R.id.current_money).text = current_account.toString()+"원"
        Log.d("test_testView",findViewById<TextView>(R.id.current_money).text.toString())

        binding.payment.setOnClickListener { // Bottom sheet
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(supportFragmentManager, BottomSheetFragment.TAG)
        }

        binding.selectBlock.setOnClickListener(){ // 블럭 확인
            val intent = Intent(this, UserActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        binding.miningBtn.setOnClickListener(){ // 채굴하러 가기
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        val manager = getSystemService(NFC_SERVICE) as NfcManager
        nfcAdapter = manager.defaultAdapter
        nfcPendingIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
        )


    }


    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        val detectedTag : Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

        val curUser = GoogleSignIn.getLastSignedInAccount(this)
        val current_email = curUser?.email.toString()
        val current_account = db.userDao().getAccountByEmail(current_email)
        val writeValue : String = "{\"email\":\"${current_email}\", \"account\":${current_account}}"
//        val writeValue : String = "{\"email\":\"${current_email}\", \"account\":10000}"
        Log.d("nfc_write_value",writeValue)
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