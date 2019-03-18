package com.unpi.madres.database

import android.annotation.SuppressLint
import android.content.Context

//Digunakan untuk mengatur penyimpanan data pada SharedPreference
class SharedPrefManager private constructor(context: Context) {

    init {
        mContext = context
    }

    companion object {

        //Nama File untuk SharedPreferenxe
        private const val DATA_USER = "dataUser"
        private const val APP_DATABASE = "Madres"

        //Key untuk mengambil Value pada SharedPreference
        private const val GET_USER_NAME = "userName"
        private const val GET_USER_LOCATION = "locationCondition"

        @SuppressLint("StaticFieldLeak")
        private lateinit var mContext: Context
        @SuppressLint("StaticFieldLeak")
        private var mInstance: SharedPrefManager? = null

        @Synchronized
        fun getInstance(context: Context): SharedPrefManager {
            if (mInstance == null)
                mInstance = SharedPrefManager(context)
            return mInstance!!
        }
    }

    val user: String?
        get() {
            val preferences = mContext.getSharedPreferences(DATA_USER, Context.MODE_PRIVATE)
            return preferences.getString(GET_USER_NAME, null)
        }

    val location: Boolean?
        get() {
            val preferences = mContext.getSharedPreferences(APP_DATABASE, Context.MODE_PRIVATE)
            return preferences.getBoolean(GET_USER_LOCATION, false)
        }

    //Method untuk meyimpan Token pada SharedPreference
    fun storeUserName(data: String): Boolean {
        val preferences = mContext.getSharedPreferences(DATA_USER, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(GET_USER_NAME, data)
        editor.apply()
        return true
    }

    fun saveLocationCondition(data: Boolean): Boolean {
        val preferences = mContext.getSharedPreferences(APP_DATABASE, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(GET_USER_LOCATION, data)
        editor.apply()
        return true
    }
}