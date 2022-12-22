package com.example.gsm_bc2_android

//import BottomSheetFragment
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.*
import android.nfc.tech.Ndef
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog

class HomeActivity : AppCompatActivity() {

    private var TAG: String = "HomeActivity"
    private lateinit var nfcPendingIntent: PendingIntent
    private lateinit var nfcAdapter: NfcAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        //왼쪽 위에 구글 사용자 이름+안녕하세요!! 띄어주기
        val user_name = intent.getStringExtra("username")
        Log.d("name_HomeActivity",user_name.toString())
        findViewById<TextView>(R.id.xml_username).text = user_name+"님\n안녕하세요!!"

        findViewById<Button>(R.id.payment).setOnClickListener {
            val bottomSheet = BottomSheetFragment()
            bottomSheet.show(supportFragmentManager, BottomSheetFragment.TAG)
        }

        findViewById<Button>(R.id.nfcpage).setOnClickListener(){
            val intent = Intent(this, NfcActivity::class.java)
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
        val writeValue : String = "50000";
        val message: NdefMessage = createTagMessage(writeValue);

        if (detectedTag != null) {
            writeTag(message, detectedTag)
        };
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