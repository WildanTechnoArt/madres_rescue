package com.unpi.madres.view

import android.content.Intent
import android.content.pm.ActivityInfo
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.widget.Toast
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.unpi.madres.R

@Suppress("CAST_NEVER_SUCCEEDS")
class LoginActivity : AppCompatActivity() {

    //Deklarasi Variable
    private var analytics: FirebaseAnalytics? = null
    private var auth: FirebaseAuth? = null
    private var checkUser: FirebaseAuth.AuthStateListener? = null
    private val PLAY_SERVICES_RESOLUTION_REQUEST = 9000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        checkPlayServices()

        //Menginisialisasi Firebase Analytics
        analytics = FirebaseAnalytics.getInstance(this@LoginActivity)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        invite.setOnClickListener {
            RegUser()
        }

        /*
          Mendapatkan Instance Autentikasi dan Menambahkan Listener
          Untuk menangani kejadian saat ada user yang masuk
         */
        auth = FirebaseAuth.getInstance()
        checkUser = FirebaseAuth.AuthStateListener { firebaseAuth ->
            if (firebaseAuth.currentUser != null){
                startActivity(Intent(this@LoginActivity, ChooseUser::class.java))
                finish()
            }
        }
    }

    //Mengecek Layanan GooglePlay Service pada Perangkat Android
    private fun checkPlayServices(): Boolean {
        val googleAPI = GoogleApiAvailability.getInstance()
        val result = googleAPI.isGooglePlayServicesAvailable(this)
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(this, result,
                    PLAY_SERVICES_RESOLUTION_REQUEST).show()
            }
            return false
        }
        return true
    }

    override fun onResume() {
        super.onResume()
        checkPlayServices()
    }

    //Menambahkan Listener Autentikasi saat aplikasi dijalankan
    override fun onStart() {
        super.onStart()
        auth?.addAuthStateListener(checkUser as FirebaseAuth.AuthStateListener)
    }

    //Mencopot Listener Autentikasi saat aplikasi dihentikan
    override fun onStop() {
        super.onStop()
        if(checkUser != null){
            auth?.removeAuthStateListener(checkUser as FirebaseAuth.AuthStateListener)
        }
    }

    private fun RegUser(){
        val nama = name.text.toString()
        val nomor_t = "+62"+phone_number.rawText.toString()
        val nomor_t2 = "+62 "+phone_number.text.toString()
        //Mengecek Nama dan Nomor Telepon
        if(TextUtils.isEmpty(nama) || nomor_t == "+62"){
            Toast.makeText(applicationContext, "Masukan Nama dan Nomor Telepon", Toast.LENGTH_SHORT).show()
        }else{

            if(nomor_t.length < 10){
                Toast.makeText(applicationContext, "Nomor Telepon Tidak Valid", Toast.LENGTH_SHORT).show()
            }else{
                //Mengirim Nama dan Nomor User pada Activity selanjutnya, untuk Verifikasi
                val bundle = Bundle()
                val intent = Intent(this@LoginActivity, VerifyActivity::class.java)
                bundle.putString("namaID", nama)
                bundle.putString("nomorID", nomor_t)
                bundle.putString("nomorID2", nomor_t2)
                intent.putExtras(bundle)
                startActivity(intent)
                finish()
            }
        }
    }
}
