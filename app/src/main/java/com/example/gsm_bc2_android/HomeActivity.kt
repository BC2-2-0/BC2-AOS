package com.example.gsm_bc2_android

//import BottomSheetFragment

import android.app.PendingIntent
import android.content.Intent
import android.nfc.*
import android.nfc.tech.Ndef
import android.os.Bundle
import android.util.Log
import android.view.View
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
import kr.co.bootpay.android.Bootpay
import kr.co.bootpay.android.events.BootpayEventListener
import kr.co.bootpay.android.models.BootExtra
import kr.co.bootpay.android.models.BootItem
import kr.co.bootpay.android.models.BootUser
import kr.co.bootpay.android.models.Payload
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okio.IOException
import org.json.JSONObject
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomeBinding

    private val application_id = "64237fd23049c8001c178ad3" // bootpay key

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
        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries().build()
        auth = Firebase.auth
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        val curUser = GoogleSignIn.getLastSignedInAccount(this)
        val name = curUser?.displayName.toString()
        val current_email = curUser?.email.toString()

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
                var jsonobejct = JSONObject(data)
                if(jsonobejct.getString("type") == "block"){
                    val email = jsonobejct.getString("email")
                    val balance = jsonobejct.getInt("balance")
                    val menu = jsonobejct.getString("menu")
                    val price = jsonobejct.getInt("price")
                    val quantity = jsonobejct.getInt("quantity")
                    val bid = jsonobejct.getInt("bid")
                    val new_block = block_tbl(bid,email,balance,menu,price,quantity)
                    Log.d(TAG,new_block.toString())
                    db.blockDao().insertblock(new_block)
                    Log.d(TAG,db.blockDao().getAllblock().toString())
                    db.userDao().AddAccountByEmail(current_email,10)
                    val t_dec_up2 = DecimalFormat("#,###")
                    if(email == current_email){
                        db.userDao().UseAccountByEmail(current_email, price)
                    }
                    var current_account = t_dec_up2.format(db.userDao().getAccountByEmail(current_email))
                    runOnUiThread {

                        binding.currentMoney.text = current_account+"원"
                    }
                }
                else if(jsonobejct.getString("type") == "mining"){
                    val mid = jsonobejct.getInt("mid")
                    val email = jsonobejct.getString("email")
                    val balance = jsonobejct.getInt("balance")
                    val charged_money = jsonobejct.getInt("charged_money")
                    val new_mining = mining_tbl(mid!!,email,balance,charged_money)
                    Log.d(TAG,"mining : " + new_mining.toString())
                    db.MiningDao().insertMining(new_mining)
                    Log.d(TAG,db.MiningDao().getAllMining().toString())
                    db.userDao().AddAccountByEmail(current_email,10)
                }


            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                super.onFailure(eventSource, t, response)
                Log.d(TAG, "On Failure -: ${response?.body}")
                Log.d(TAG, t.toString())
                Log.d(TAG,response.toString())
            }
        }

        val client = OkHttpClient.Builder().connectTimeout(50, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.MINUTES)
            .writeTimeout(100, TimeUnit.MINUTES)
            .build()

        val request = Request.Builder()
            .url("http://13.209.74.86:3000/receive") // URL
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

        Log.d("curUser",name+current_email)
        //Log.d("name_HomeActivity",name)
        findViewById<TextView>(R.id.xml_username).text = name
        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries().build()
//        Log.d("test getuser-h",db.userDao().getAllUser().toString())
        val t_dec_up = DecimalFormat("#,###")
        var current_account = t_dec_up.format(db.userDao().getAccountByEmail(current_email))
        Log.d("test_getaccount",current_account.toString())
        findViewById<TextView>(R.id.current_money).text = current_account.toString()+"원"
        Log.d("test_testView",findViewById<TextView>(R.id.current_money).text.toString())

        binding.payment.setOnClickListener { // Bottom sheet
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(supportFragmentManager, BottomSheetFragment.TAG)
        }

        binding.selectBlock.setOnClickListener(){ // 블럭 확인
            val intent = Intent(this, BlockActivity::class.java)
            startActivity(intent)
            this.finish()
        }

        binding.miningBtn.setOnClickListener(){ // 채굴하러 가기
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)
            this.finish()
        }

//        val manager = getSystemService(NFC_SERVICE) as NfcManager
//        nfcAdapter = manager.defaultAdapter
//        nfcPendingIntent = PendingIntent.getActivity(
//            this, 0,
//            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
//        )


    }
    fun PaymentTest(v: View?) {
        Log.d("bootpay","hi")
        val extra = BootExtra()
            .setCardQuota("0,2,3") // 일시불, 2개월, 3개월 할부 허용, 할부는 최대 12개월까지 사용됨 (5만원 이상 구매시 할부허용 범위)
        val items: MutableList<BootItem> = ArrayList()
        val item1 = BootItem().setName("마우's 스").setId("ITEM_CODE_MOUSE").setQty(1).setPrice(5000.0)
        val item2 = BootItem().setName("키보드").setId("ITEM_KEYBOARD_MOUSE").setQty(1).setPrice(5000.0)
        items.add(item1)
        items.add(item2)
        val payload = Payload()

        payload.setApplicationId(application_id)
            .setOrderName("부트페이 결제테스트")
            .setPg("토스")
            .setMethod("계좌이체")
            .setOrderId("1234")
            .setPrice(10000.0)
            .setUser(getBootUser())
            .setExtra(extra).items = items

        val map: MutableMap<String, Any> = HashMap()
        map["1"] = "abcdef"
        map["2"] = "abcdef55"
        map["3"] = 1234
        payload.metadata = map
        //        payload.setMetadata(new Gson().toJson(map));
        Bootpay.init(supportFragmentManager, applicationContext)
            .setPayload(payload)
            .setEventListener(object : BootpayEventListener {
                override fun onCancel(data: String) {
                    Log.d("bootpay", "cancel: $data")
                }

                override fun onError(data: String) {
                    Log.d("bootpay", "error: $data")
                }

                override fun onClose() {
                    Bootpay.removePaymentWindow()
                }

                override fun onIssued(data: String) {
                    Log.d("bootpay", "issued: $data")
                }

                override fun onConfirm(data: String): Boolean {
                    Log.d("bootpay", "confirm: $data")
                    //                        Bootpay.transactionConfirm(data); //재고가 있어서 결제를 진행하려 할때 true (방법 1)
                    return true //재고가 있어서 결제를 진행하려 할때 true (방법 2)
                    //                        return false; //결제를 진행하지 않을때 false
                }

                override fun onDone(data: String) {
                    Log.d("done", data)
                }
            }).requestPayment()
    }

    fun getBootUser(): BootUser? {
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        val curUser = GoogleSignIn.getLastSignedInAccount(this)
        val userId = "123411aaaaaaaaaaaabd4ss121"
        val user = BootUser()
        user.id = curUser?.id.toString()
        user.area = "서울"
        user.gender = 1 //1: 남자, 0: 여자
        user.email = curUser?.email.toString()
        user.phone = "010-1234-4567"
        user.birth = "1988-06-10"
        user.username = curUser?.displayName.toString()
        return user
    }

//    override fun onNewIntent(intent: Intent) {
//        super.onNewIntent(intent)
//        val detectedTag : Tag? = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
//
//        val curUser = GoogleSignIn.getLastSignedInAccount(this)
//        val current_email = curUser?.email.toString()
//        val current_account = db.userDao().getAccountByEmail(current_email)
//        val writeValue : String = "{\"email\":\"${current_email}\", \"account\":${current_account}}"
////        val writeValue : String = "{\"email\":\"${current_email}\", \"account\":10000}"
//        Log.d("nfc_write_value",writeValue)
//        val message: NdefMessage = createTagMessage(writeValue);
//
//        if (detectedTag != null) {
//            writeTag(message, detectedTag)
//        };
//    }
//    override fun onResume() {
//        super.onResume()
//        nfcAdapter.enableForegroundDispatch(this, nfcPendingIntent, null, null);
//    }
//
//    override fun onPause() {
//        super.onPause()
//        nfcAdapter.disableForegroundDispatch(this);
//    }
//    private fun createTagMessage(msg: String): NdefMessage {
//        return NdefMessage(NdefRecord.createUri(msg))
//    }
//
//    fun writeTag(message: NdefMessage, tag: Tag) {
//        val size = message.toByteArray().size
//        try {
//            val ndef = Ndef.get(tag)
//            if (ndef != null) {
//                ndef.connect()
//                if (!ndef.isWritable) {
//                    Toast.makeText(applicationContext, "can not write NFC tag", Toast.LENGTH_SHORT).show()
//                }
//                if (ndef.maxSize < size) {
//                    Toast.makeText(applicationContext, "NFC tag size too large", Toast.LENGTH_SHORT).show()
//                }
//                ndef.writeNdefMessage(message)
//                Toast.makeText(applicationContext, "NFC tag is writted", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: Exception) {
//            //Log.i(TAG,e.message);
//        }
//    }

}