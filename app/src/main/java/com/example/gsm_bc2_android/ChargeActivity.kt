package com.example.gsm_bc2_android


import android.content.Intent
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.example.gsm_bc2_android.databinding.ChargeBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kr.co.bootpay.android.Bootpay
import kr.co.bootpay.android.events.BootpayEventListener
import kr.co.bootpay.android.models.BootExtra
import kr.co.bootpay.android.models.BootItem
import kr.co.bootpay.android.models.BootUser
import kr.co.bootpay.android.models.Payload
import org.json.JSONObject

class ChargeActivity : AppCompatActivity() {

    private lateinit var binding: ChargeBinding
    private val application_id = "64237fd23049c8001c178ad3" // bootpay key
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var db: Blockdb
    private var auth : FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ChargeBinding.inflate(layoutInflater);
        setContentView(binding.root)
        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries().build()
        auth = Firebase.auth
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
        val curUser = GoogleSignIn.getLastSignedInAccount(this)
        val current_email = curUser?.email.toString()
        binding.backbutton.setOnClickListener {
            this.finish()
            startActivity(Intent(this, HomeActivity::class.java))
        }

        binding.deleteButton.setOnClickListener(){
            binding.inputTextBox.setText("")
        }

        binding.add1000.setOnClickListener(){
            if (binding.inputTextBox.text.isEmpty()){
                binding.inputTextBox.setText("1000")
            }
            else{
                binding.inputTextBox.setText((binding.inputTextBox.text.toString().toInt() + 1000).toString())
            }
        }

        binding.add5000.setOnClickListener(){
            if (binding.inputTextBox.text.isEmpty()){
                binding.inputTextBox.setText("5000")
            }
            else{
                binding.inputTextBox.setText((binding.inputTextBox.text.toString().toInt() + 5000).toString())
            }
        }

        binding.add10000.setOnClickListener(){
            if (binding.inputTextBox.text.isEmpty()){
                binding.inputTextBox.setText("10000")
            }
            else{
                binding.inputTextBox.setText((binding.inputTextBox.text.toString().toInt() + 10000).toString())
            }
        }

        binding.chargeButton.setOnClickListener(){
            if(binding.inputTextBox.text.isEmpty()){
                binding.inputTextBox.requestFocus()
            }else{
                val money = binding.inputTextBox.text
                PaymentTest(money.toString().toInt(), current_email)
            }
        }
    }


    fun PaymentTest(money: Int, current_email: String) {
        Log.d("bootpay","hi")
        val extra = BootExtra()
            .setCardQuota("0,2,3") // 일시불, 2개월, 3개월 할부 허용, 할부는 최대 12개월까지 사용됨 (5만원 이상 구매시 할부허용 범위)
        val items: MutableList<BootItem> = ArrayList()
        val item1 = BootItem().setName("포인트").setId("POINT").setQty(1).setPrice(money.toDouble())
        items.add(item1)
        val payload = Payload()

        payload.setApplicationId(application_id)
            .setOrderName("부트페이 결제테스트")
            .setPg("토스")
            .setMethod("계좌이체")
            .setOrderId("1234")
            .setPrice(money.toDouble())
            .setUser(getBootUser())
            .setExtra(extra).items = items

//        val map: MutableMap<String, Any> = HashMap()
//        map["1"] = "abcdef"
//        map["2"] = "abcdef55"
//        map["3"] = 1234
//        payload.metadata = map
//        //        payload.setMetadata(new Gson().toJson(map));
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
                    Log.d("done",data)
                    val json_data = JSONObject(data).getJSONObject("data")
                    Log.d("done", json_data.getInt("price").toString())
                    db.userDao().AddAccountByEmail(current_email,json_data.getInt("price"))

                    this@ChargeActivity.startActivity(Intent(this@ChargeActivity, HomeActivity::class.java))
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
        user.area = "광주"
        user.gender = 1 //1: 남자, 0: 여자
        user.email = curUser?.email.toString()
        user.phone = "010-1234-5678"
        user.birth = "2005-06-27"
        user.username = curUser?.displayName.toString()
        return user
    }

}