package com.example.gsm_bc2_android

//import BottomSheetFragment

import CustomDialog
import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
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
import com.google.zxing.integration.android.IntentIntegrator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import okio.IOException
import org.json.JSONObject
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.URI
import java.net.URL
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit


class HomeActivity : AppCompatActivity() {
    private lateinit var binding: HomeBinding

    lateinit var db: Blockdb
    private var TAG: String = "HomeActivity"
    private var auth: FirebaseAuth? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    //private lateinit var bottomSheetBehavior: BottomSheetBehavior<ConstraintLayout>
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = HomeBinding.inflate(layoutInflater);
        setContentView(binding.root)
        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries()
            .build()
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
                if (jsonobejct.getString("type") == "pay") {
                    val email = jsonobejct.getString("email")
                    val balance = jsonobejct.getInt("balance")
                    val menu = jsonobejct.getString("menu")
                    val price = jsonobejct.getInt("price")
                    val quantity = jsonobejct.getInt("quantity")
                    val bid = jsonobejct.getInt("bid")
                    val new_block = block_tbl(bid, email, balance, menu, price, quantity)
                    Log.d(TAG, new_block.toString())
                    db.blockDao().insertblock(new_block)
                    Log.d(TAG, db.blockDao().getAllblock().toString())
                    db.userDao().AddAccountByEmail(current_email, 10)
                    val t_dec_up2 = DecimalFormat("#,###")
//                    if (email == current_email) {
//                        db.userDao().UseAccountByEmail(current_email, price)
//                    }
                    var current_account =
                        t_dec_up2.format(db.userDao().getAccountByEmail(current_email))
                    runOnUiThread {

                        binding.currentMoney.text = current_account + "원"
                    }
                } else if (jsonobejct.getString("type") == "charge") {
                    val mid = jsonobejct.getInt("mid")
                    val email = jsonobejct.getString("email")
                    val balance = jsonobejct.getInt("balance")
                    val charged_money = jsonobejct.getInt("charged_money")
                    val new_mining = mining_tbl(mid!!, email, balance, charged_money)
                    Log.d(TAG, "mining : " + new_mining.toString())
                    db.MiningDao().insertMining(new_mining)
                    Log.d(TAG, db.MiningDao().getAllMining().toString())
                    db.userDao().AddAccountByEmail(current_email, 10)
                }


            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                super.onFailure(eventSource, t, response)
                Log.d(TAG, "On Failure -: ${response?.body}")
                Log.d(TAG, t.toString())
                Log.d(TAG, response.toString())
            }
        }

        val client = OkHttpClient.Builder().connectTimeout(50, TimeUnit.SECONDS)
            .readTimeout(100, TimeUnit.MINUTES)
            .writeTimeout(100, TimeUnit.MINUTES)
            .build()

        val request = Request.Builder()
            .url("http://13.125.77.165:3000/receive") // URL
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

        Log.d("curUser", name + current_email)
        //Log.d("name_HomeActivity",name)
        findViewById<TextView>(R.id.xml_username).text = name
        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries()
            .build()
//        Log.d("test getuser-h",db.userDao().getAllUser().toString())
        val t_dec_up = DecimalFormat("#,###")
        var current_account = t_dec_up.format(db.userDao().getAccountByEmail(current_email))
        Log.d("test_getaccount", current_account.toString())
        findViewById<TextView>(R.id.current_money).text = current_account.toString() + "원"
        Log.d("test_testView", findViewById<TextView>(R.id.current_money).text.toString())

        binding.payment.setOnClickListener { // QR Scan
//            val bottomSheet = BottomSheetFragment()
//            bottomSheet.show(supportFragmentManager, BottomSheetFragment.TAG)

//              val intent = Intent(this, ScanActivity::class.java)
//              startActivity(intent)
//              this.finish()
            val integrator = IntentIntegrator(this)
            with(integrator) {
                setBeepEnabled(true)
                setPrompt("QR코드를 스캔해주세요!")//QR 하단 메세지
                captureActivity = ScanActivity::class.java
                initiateScan()
            }
        }



        binding.selectBlock.setOnClickListener() { // 블럭 확인(내역확인)
            val intent = Intent(this, BlockActivity::class.java)
            startActivity(intent)
            this.finish()
        }

//        findViewById<Button>(R.id.select_mining).setOnClickListener(){ // 채굴하러 가기
//            val intent = Intent(this, GameActivity::class.java)
//            startActivity(intent)
//            this.finish()
//        }

//        findViewById<Button>(R.id.select_charge).setOnClickListener(){ // 충전하러 가기(부트페이)
//            val intent = Intent(this, ChargeActivity::class.java)
//            startActivity(intent)
//            this.finish()
//        }

        binding.miningBtn.setOnClickListener() { // 충전하기 바텀시트
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(supportFragmentManager, BottomSheetFragment.TAG)
//            val intent = Intent(this, GameActivity::class.java)
//            startActivity(intent)
//            this.finish()
        }

//        val manager = getSystemService(NFC_SERVICE) as NfcManager
//        nfcAdapter = manager.defaultAdapter
//        nfcPendingIntent = PendingIntent.getActivity(
//            this, 0,
//            Intent(this, javaClass).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0
//        )


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        val curUser = GoogleSignIn.getLastSignedInAccount(this)
        val current_email = curUser?.email.toString()
        if (resultCode == Activity.RESULT_OK) {
            val scanRes = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
            val content = scanRes.contents.toString()
            Log.d("QR", content)

            val url = URL(content)
            val uri = URI(content)

            val host = url.host
            val port = url.port

            println("Host: $host")
            println("Port: $port")

            val query = uri.query
            val queryParams = query.split("&")

            val paramMap = mutableMapOf<String, String>()

            for (param in queryParams) {
                val pair = param.split("=")
                val key = pair[0]
                val value = pair[1]
                paramMap[key] = value
            }

            val item = paramMap["item"]
            val quantity = paramMap["quantity"]
            val price = paramMap["price"]
            val number = paramMap["number"]
            val current_account = db.userDao().getAccountByEmail(current_email)
            if ((current_account - price!!.toInt()) < 0){
                Log.d("QR", "돈 없음!!")
                FailDialog()
            }
            else{
                cuDialog(item!!, quantity!!, price!!, number!!.toInt())
            }

//            val intent = Intent(this, PayActivity::class.java)
//            intent.putExtra("content", content)
//            startActivity(intent)
//            this.finish()


        } else {
            Log.d("QR", "fuck")
        }
    }

    fun cuDialog(item: String, quantity: String, price: String, number: Int) {

        val dialovView = layoutInflater.inflate(R.layout.pay_dialog, null)
        val confirm_btn = dialovView.findViewById<ImageView>(R.id.pay_confirm)
        val cancle_btn = dialovView.findViewById<ImageView>(R.id.pay_cancle)
        val builder = AlertDialog.Builder(this)
            .setView(dialovView)
            .create()

        dialovView.findViewById<TextView>(R.id.price).setText(price + "원")
        dialovView.findViewById<TextView>(R.id.quantity).setText(quantity + "개")
        dialovView.findViewById<TextView>(R.id.item).setText(item + " ")

        confirm_btn.setOnClickListener() {
            Log.d("QR", "OK")
            var retrofit = Retrofit.Builder()
                .baseUrl("http://13.125.77.165:3000")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            var transportservice: paytransportservice =
                retrofit.create(paytransportservice::class.java)
            val googleSignInOptions =
                GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
            mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
            val curUser = GoogleSignIn.getLastSignedInAccount(this)
            val current_email = curUser?.email.toString()
            db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries()
                .build()
            var current_account = db.userDao().getAccountByEmail(current_email)

            transportservice.requestLogin(
                current_email,
                current_account,
                item,
                price.toInt(),
                quantity.toInt(),
                number
            ).enqueue(object :
                retrofit2.Callback<transport> {
                override fun onFailure(call: retrofit2.Call<transport>, t: Throwable) {
                    Log.d("QR", "fail...")
                }

                override fun onResponse(
                    call: retrofit2.Call<transport>,
                    response: retrofit2.Response<transport>
                ) {
                    //var login = response.body()
                    Log.d("QR", response.body().toString())
                }
            })
            db.userDao().UseAccountByEmail(current_email,price!!.toInt())
            this.finish()
            startActivity(Intent(this, PayActivity::class.java))
//            current_account -= price.toInt()
//            val t_dec_up = DecimalFormat("#,###")
//            runOnUiThread {
//                binding.currentMoney.text = t_dec_up.format(current_account).toString()+"원"	// TextView 세팅
//            }
            builder.dismiss()
        }
        cancle_btn.setOnClickListener() {
            Log.d("QR", "NO")
            builder.dismiss()
        }

        builder.window!!.setBackgroundDrawable(ColorDrawable(0))

        builder.show()
    }

    fun FailDialog() {

        val dialovView = layoutInflater.inflate(R.layout.pay_fail_dialog, null)
        val confirm_btn = dialovView.findViewById<ImageView>(R.id.pay_confirm)
        val charge_btn = dialovView.findViewById<ImageView>(R.id.pay_charge)
        val builder = AlertDialog.Builder(this)
            .setView(dialovView)
            .create()

        confirm_btn.setOnClickListener() {
            Log.d("QR", "OK")
            builder.dismiss()
        }
        charge_btn.setOnClickListener() {
            Log.d("QR", "NO")
            val intent = Intent(this, ChargeActivity::class.java)
            startActivity(intent)
            this.finish()
            builder.dismiss()
        }

        builder.window!!.setBackgroundDrawable(ColorDrawable(0))

        builder.show()
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