package com.example.gsm_bc2_android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import com.example.gsm_bc2_android.databinding.MainBinding



class MainActivity : AppCompatActivity() {
    lateinit var db: Blockdb

    private lateinit var binding: MainBinding

    private var auth : FirebaseAuth? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = MainBinding.inflate(layoutInflater);
        setContentView(binding.root)
        auth = Firebase.auth
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        binding.googleLogin.setOnClickListener(){
            val gsa = GoogleSignIn.getLastSignedInAccount(this)
            if (gsa != null) {
                println("안녕 아ㅓㄹ올")
            } else {
                signIn()
            }
        }
    }

    // onStart. 유저가 앱에 이미 구글 로그인을 했는지 확인
    public override fun onStart() {
        super.onStart()
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if(account!==null){ // 이미 로그인 되어있을시 바로 홈 액티비티로 이동
            val intent = Intent(this, HomeActivity::class.java)
            this.finish()
            startActivity(intent)
        }
    } //onStart End


    private fun signIn() {
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data!!)
        if (requestCode == 1) {
            try {
                if (result?.isSuccess == true) {
                    val account = result.signInAccount
                    handleSignInResult(account!!)
//                    val curUser = GoogleSignIn.getLastSignedInAccount(this)
//                    val name = curUser?.displayName.toString()
//                    Log.d("username",name)
                    val intent = Intent(this, HomeActivity::class.java)



                    lateinit var db: Blockdb
                    this.finish()
                    startActivity(intent)
                }
            } catch (e: ApiException) {
                println("안녕1 ${e.statusCode}")
            }
        }
    }

    private fun handleSignInResult(account: GoogleSignInAccount) {
        try {
            val credentials = GoogleAuthProvider.getCredential(account.idToken, null)
            auth?.signInWithCredential(credentials)
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // 로그인 처음 성공했을 때 로컬DB에 계정 추가 ( auto_increment - uid, 이메일, 잔액(처음 0원) )
                        val curUser = GoogleSignIn.getLastSignedInAccount(this) // 현재 유저
                        val name = curUser?.email.toString() // 이름 불러옴
                        Log.d("username main",name) // 로그 남기고
                        var newUser = UserInfo(null,name,0) // 유저객체 만들고
                        db = Room.databaseBuilder(this, Blockdb::class.java, "Blockdb").allowMainThreadQueries().build() // 디비 설정
                        db.userDao().insertUser(newUser) // 유저 row 생성

                        startActivity(Intent(this,HomeActivity::class.java))
                        this.finish()
                    }
                }
        } catch (e: ApiException) {
            println("안녕 ${e.statusCode}")
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String?) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth!!.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful()) {
                    Toast.makeText(this@MainActivity, "로그인 성공", Toast.LENGTH_SHORT)
                        .show()
                    val user: FirebaseUser? = auth?.getCurrentUser()
                } else {
                    Toast.makeText(this@MainActivity, "로그인 실패", Toast.LENGTH_SHORT)
                        .show()
                }
            }
    }
}
