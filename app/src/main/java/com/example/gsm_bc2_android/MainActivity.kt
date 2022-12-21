package com.example.gsm_bc2_android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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


class MainActivity : AppCompatActivity() {
    private var auth : FirebaseAuth? = null
    private lateinit var mGoogleSignInClient: GoogleSignInClient
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        auth = Firebase.auth
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)

        findViewById<ImageView>(R.id.google_login).setOnClickListener(){
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
        if(account!==null){ // 이미 로그인 되어있을시 바로 메인 액티비티로 이동
            val curUser = GoogleSignIn.getLastSignedInAccount(this)
            val name = curUser?.displayName.toString()
            Log.d("username",name)
            val intent = Intent(this, HomeActivity::class.java)
            intent.putExtra("username",name)
            startActivity(intent)
            this.finish()
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
                    val curUser = GoogleSignIn.getLastSignedInAccount(this)
                    val name = curUser?.displayName.toString()
                    Log.d("username",name)
                    val intent = Intent(this, HomeActivity::class.java)
                    intent.putExtra("username",name)
                    startActivity(intent)
                    this.finish()
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
