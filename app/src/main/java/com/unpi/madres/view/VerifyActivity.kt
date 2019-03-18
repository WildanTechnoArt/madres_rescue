package com.unpi.madres.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import com.google.firebase.FirebaseException
import kotlinx.android.synthetic.main.activity_verify.*
import java.util.concurrent.TimeUnit
import com.google.firebase.auth.*
import com.google.firebase.database.FirebaseDatabase
import com.unpi.madres.R
import com.unpi.madres.database.SharedPrefManager
import com.unpi.madres.models.DataUser

@Suppress("DEPRECATION")
class VerifyActivity : AppCompatActivity(), View.OnClickListener {

    //Deklarasi Variable
    private var mAuth: FirebaseAuth? = null
    private var mVertificationId: String? = null
    private var mToken: PhoneAuthProvider.ForceResendingToken? = null
    private var mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks? = null
    private var noTelpon: String? = null
    private var noTelpon2: String? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        setSupportActionBar(toolbar)
        resend_code.isEnabled = false
        resend_code.setTextColor(Color.parseColor("#FFD2D2D2"))
        progress.visibility = View.VISIBLE
        progmsg.text = "Mohon Tunggu Sebentar...."
        verify.setOnClickListener(this)
        resend_code.setOnClickListener(this)
        resend_code.visibility = View.GONE
        mAuth = FirebaseAuth.getInstance()
        mAuth?.setLanguageCode("id")
        getDataReg()
        sendCode()
    }

    @SuppressLint("SetTextI18n")
    private fun getDataReg(){
        noTelpon = intent.getStringExtra("nomorID")
        noTelpon2 = intent.getStringExtra("nomorID2")
        PhoneNumber.text = noTelpon2
    }

    private fun sendCode(){
        CallBack()
        // Meminta Firebase Untuk Memverifikasi Nomor Telepon Pengguna
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            noTelpon!!, //Nomor Telepon Untuk Varifikasi
            45, // Durasi Waktu Habis
            TimeUnit.SECONDS, // Unit Timeout
            this@VerifyActivity, // Activity
            mCallBack as PhoneAuthProvider.OnVerificationStateChangedCallbacks) // OnVerificationStateChangedCallbacks
    }

    @SuppressLint("SetTextI18n")
    private fun resendCode(){
        getDataReg()
        CallBack()
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
            noTelpon!!,
            45,
            TimeUnit.SECONDS,
            this@VerifyActivity,
            mCallBack as PhoneAuthProvider.OnVerificationStateChangedCallbacks,
            mToken) // Digunakan untuk mengirim ulang kembali kode vertifikasi
        progress.visibility = View.VISIBLE
        progmsg.text = "Mengirim ulang kode verifikasi"
        resend_code.visibility = View.GONE
    }

    // Membuat Instance Yang Berisi Implementasi Dan Fungsi Callback Untuk Menangani Hasil Permintaan
    private fun CallBack(){
        mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){

            @SuppressLint("SetTextI18n")
            override fun onCodeSent(vertificationId: String?, token: PhoneAuthProvider.ForceResendingToken?) {
                // Callback didalam sini akan dipanggil/dieksekusi saat terjadi proses pengiriman kode
                // Dan User Diminta untuk memasukan kode vertifikasi

                // Untuk Menyimpan ID verifikasi dan kirim ulang token
                mVertificationId = vertificationId
                mToken = token
                progress.visibility = View.VISIBLE
                progmsg.text = "Kode verifikasi sedang diproses...."
            }

            @SuppressLint("SetTextI18n")
            override fun onVerificationCompleted(credential: PhoneAuthCredential?) {
                // Callback disini akan dipanggil saat Verifikasi Selseai atau Berhasil
                progmsg.text = ""
                progress.visibility = View.GONE
                signInWithPhoneAuthCredential(credential as PhoneAuthCredential)
            }

            override fun onCodeAutoRetrievalTimeOut(p0: String?) {
                super.onCodeAutoRetrievalTimeOut(p0)
                Toast.makeText(applicationContext, "Verification Time Out!", Toast.LENGTH_SHORT).show()
                progress.visibility = View.GONE
                progmsg.text = ""
                resend_code.visibility = View.VISIBLE
                resend_code.isEnabled = true
            }

            override fun onVerificationFailed(exception: FirebaseException?) {
                // Callback disini akan dipanggil saat permintaan tidak valid atau terdapat kesalahan
                Toast.makeText(applicationContext, "Verifikasi Gagal, Silakan coba lagi", Toast.LENGTH_SHORT).show()
                progress.visibility = View.GONE
                progmsg.text = ""
                resend_code.visibility = View.VISIBLE
                resend_code.isEnabled = true
                Log.e("VerifyActivity.kt", exception?.message)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun Number1(){
        try{
            val code = vertify_num.text.toString()
            if(TextUtils.isEmpty(code)){
                Toast.makeText(applicationContext,"Masukan Kode Verifikasi", Toast.LENGTH_SHORT).show()
            }else{
                // Digunakan Untuk Memvertifikasi Nomor Telepon, Saat Tombol Vertifikasi Ditekan
                val credential: PhoneAuthCredential = PhoneAuthProvider.getCredential(mVertificationId as String, code)
                signInWithPhoneAuthCredential(credential)
                progress.visibility = View.VISIBLE
                progmsg.text = "Sedang diproses, mohon tunggu...."
            }
        }catch (ex: Exception){
            progress.visibility = View.GONE
            progmsg.text = ""
            resend_code.visibility = View.VISIBLE
            resend_code.isEnabled = true
            Toast.makeText(applicationContext,"Kode yang anda masukan salah", Toast.LENGTH_SHORT).show()
        }
    }

    //Menyimpan data user pada Database SharedPreferences dan Firebase RealtimeDatabase
    @SuppressLint("CommitPrefEdits")
    private fun profileUser(){
        //Meyimpan data user pada Database didalam Firebase
        val getUser = mAuth!!.currentUser
        val database = FirebaseDatabase.getInstance()
        val reference = database.reference

        //Data probadi dari User dan Provider
        val getUserID = getUser!!.uid
        val getNama = intent.getStringExtra("namaID").toString()
        val getNomor = getUser.phoneNumber
        val setData = DataUser(getNama.toLowerCase(), getNama, getNomor.toString())
        reference.child("Users").child(getUserID).setValue(setData).addOnCompleteListener {
            if(it.isSuccessful){
                SharedPrefManager.getInstance(this).storeUserName(getNama)
                val intent = Intent(this@VerifyActivity, ChooseUser::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
                finish()
            }else{
                Toast.makeText(applicationContext, "Terjadi Kesalahan", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //Menangani kejadian jika user berhasil atau gagal saat autentikasi
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth?.signInWithCredential(credential)
            ?.addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in Berhasil.
                    profileUser()
                } else {
                    // Sign in Gagal.
                    Log.w("Task Error", "Terjadi Kesalahan Saat Masuk")
                    if(task.exception is FirebaseAuthInvalidCredentialsException){
                        // Kode Yang Dimasukan tidal Valid.
                        Toast.makeText(applicationContext, "Kode yang dimasukkan tidak valid", Toast.LENGTH_SHORT).show()
                    }
                    progress.visibility = View.INVISIBLE
                    progmsg.text = ""
                }
            }
    }

    override fun onClick(v: View?) {
        when(v?.id){
            R.id.verify -> {
                Number1()
                val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(verify.windowToken, 0)
            }
            R.id.resend_code -> {
                resendCode()
                val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(resend_code.windowToken, 0)
            }
        }
    }
}