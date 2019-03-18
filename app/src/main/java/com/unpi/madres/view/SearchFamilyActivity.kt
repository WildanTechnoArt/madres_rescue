package com.unpi.madres.view

import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.location.Location
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.unpi.madres.R
import com.unpi.madres.models.VictimData
import kotlinx.android.synthetic.main.activity_search_family.*

class SearchFamilyActivity : AppCompatActivity() {

    //Deklarasi Variable
    private var mAuth: FirebaseUser? = null
    private var databaseReference: DatabaseReference? = null
    private var familyRefrence: DatabaseReference? = null
    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<VictimData, SearchFamilyActivity.UsersViewHolder>? = null
    private var searchName = true
    private var mLocation: Location? = null
    private var request: LocationRequest? = null
    private var providerClient: FusedLocationProviderClient? = null
    private var locationCallback: LocationCallback? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search_family)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //Inisialisasi dan Instance FirebaseUser(getUser)
        mAuth = FirebaseAuth.getInstance().currentUser

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        //Menentukan bagaimana item pada RecyclerView tampil
        val layout = LinearLayoutManager(this@SearchFamilyActivity)
        familySearch.layoutManager = layout
        familySearch.setHasFixedSize(true)

        //Mendapatkan Instance dari Firebase Realtime Database
        databaseReference = FirebaseDatabase.getInstance().reference.child("VictimList")
        familyRefrence = FirebaseDatabase.getInstance().reference.child("MyFamily").child(mAuth?.uid.toString())

        //Tombol search, untuk mencari teman / kontak baru
        search.setOnClickListener {

            //Menyembunyikan keyboard saat tombol Search Diklik
            val imm: InputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(searchInput.windowToken, 0)

            //Mencari anggota keluarga berdasarkan kategori yang dipilih (Nama / Nomor)
            if(searchName){

                //Mendapatkan Input berupa nama
                val getFamily = searchInput.text.toString()

                if(getFamily.isNotEmpty()){
                    //Mencari keluarga berdasarkan nama
                    firebaseRecyclerSearch(getFamily)
                }else{
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@SearchFamilyActivity, "Masukan Nama Yang Dicari", Toast.LENGTH_SHORT).show()
                }

            }else{

                //Mendapatkan Input berupa nomor telepon
                val getFamily = "+62"+phoneSearch.rawText.toString()

                if(getFamily.isNotEmpty()){

                    if(getFamily.length < 10){
                        progressBar.visibility = View.GONE
                        Toast.makeText(applicationContext, "No. Telepon Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                    }else{
                        //Mencari keluarga berdasrkan nomor kontak
                        firebaseRecyclerSearchNomor(getFamily)
                    }
                }else{
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@SearchFamilyActivity, "Masukan Nomor Yang Dicari", Toast.LENGTH_SHORT).show()
                }
            }
        }

        //Untuk memilih teman berdasarkan Nama / Nomor
        category.setOnCheckedChangeListener { _, checkedId ->
            when(checkedId){
                R.id.src_nama ->{
                    phoneSearch.visibility = View.GONE
                    searchInput.visibility = View.VISIBLE
                    searchName = true
                    searchInput.text = null
                }
                R.id.src_nomor ->{
                    searchInput.visibility = View.GONE
                    phoneSearch.visibility = View.VISIBLE
                    searchName = false
                }
            }
        }

        displayLocation()
    }

    private fun buildLocationRequest() {
        request = LocationRequest()
        request?.smallestDisplacement = 10f
        request?.fastestInterval = 3000
        request?.interval = 5000
        request?.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }

    private fun displayLocation() {
        buildLocationRequest()
        providerClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                if (locationResult == null) {
                    Toast.makeText(applicationContext, "Tidak dapat menemukan lokasi", Toast.LENGTH_SHORT).show()
                } else {
                    for (location in locationResult.locations) {
                        mLocation = location
                    }
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        providerClient!!.requestLocationUpdates(request, locationCallback, null)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    //Class Holder untuk menetukan data yang akan ditampilkan pada RecyclerView (Firebase)
    class UsersViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {

        var mView: View? = mView

        //Menampilkan nama teman yang dicari
        fun setDisplayName(username: String){
            val nama: TextView = mView?.findViewById(R.id.familyName)!!
            nama.text = username
        }

        //Menampilkan status teman yang dicari
        fun setDisplayNomor(nomorUser: String){
            val nomor: TextView = mView?.findViewById(R.id.familyNomor)!!
            nomor.text = nomorUser
        }

    }

    //Method untuk mencari kontak baru berdasarkan nama
    private fun firebaseRecyclerSearch(newText: String){
        progressBar.visibility = View.VISIBLE

        //Mengubah teks yang diinputkan menjadi huruf kecil
        val lowerText = newText.toLowerCase()

        //Mengatur Query database untuk mengurutkan data (Teman) berdasarkan input (Nama) dari User
        val query: Query = databaseReference!!.orderByChild("searchIndex").startAt(lowerText).endAt(lowerText+"\uf8ff")

        //Menerapkan Setelan pada FirebaseRecyclerOptions
        val options = FirebaseRecyclerOptions.Builder<VictimData>()
            .setQuery(query, VictimData::class.java)
            .setLifecycleOwner(this@SearchFamilyActivity)
            .build()

        //Medapatkan Referensi Database, berdasarkan Input (Nama)
        databaseReference!!.orderByChild("searchIndex").startAt(newText).endAt(newText +"\uf8ff")
            .addListenerForSingleValueEvent(object : ValueEventListener {

                override fun onDataChange(p0: DataSnapshot) {
                    //Menambahkan Listener dan mengecek data yang dimasukan, apakah ada atau tidak
                    progressBar.visibility = View.VISIBLE
                    if(!p0.exists()){
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@SearchFamilyActivity, "Nama Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@SearchFamilyActivity, "Pencarian Gagal Dimuat", Toast.LENGTH_SHORT).show()
                }

            })

        //Membuat Instance dan Mengatur Setelan Adapter
        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<VictimData, SearchFamilyActivity.UsersViewHolder>(options) {

            /*
            Menerapkan Layout untuk menampilkan Chat List
            Secara Default Chat List akan tampil di sebelah kiri (Teman)
            */
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchFamilyActivity.UsersViewHolder {
                return SearchFamilyActivity.UsersViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.family_item_layout, parent, false))
            }

            /*
             Menetukan data-data yang akan di tampilkan pada masing-masing View
             Didalam setiap pada RecyclerView
             */
            override fun onBindViewHolder(holder: SearchFamilyActivity.UsersViewHolder, position: Int, model: VictimData) {

                //Mendaptkan data-data user (Keluarga) dan menampilkannya pada masing2 View
                holder.setDisplayName(model.namaUser.toString())
                holder.setDisplayNomor(model.nomorUser.toString())

                progressBar.visibility = View.GONE

                //Mendapatkan kode unik (User ID)
                val getKey: String = getRef(holder.adapterPosition).key.toString()

                holder.mView?.setOnClickListener {
                    toMapActivity(model, getKey)
                }
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                progressBar.visibility = View.GONE
                Toast.makeText(this@SearchFamilyActivity, "Pencarian Gagal Dimuat", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChanged() {
                super.onDataChanged()
                firebaseRecyclerAdapter?.notifyDataSetChanged()
            }
        }

        //Memasang adapter pada RecyclerView didalam Activity AddFriend
        familySearch?.adapter = firebaseRecyclerAdapter
    }

    private fun firebaseRecyclerSearchNomor(newText: String){
        progressBar.visibility = View.VISIBLE

        //Mengatur Query database untuk mengurutkan data (Teman) berdasarkan input (Nomor) dari User
        val query: Query = databaseReference!!.orderByChild("nomorUser").equalTo(newText)

        //Menerapkan Setelan pada FirebaseRecyclerOptions
        val options = FirebaseRecyclerOptions.Builder<VictimData>()
            .setQuery(query, VictimData::class.java)
            .setLifecycleOwner(this@SearchFamilyActivity)
            .build()

        databaseReference!!.orderByChild("nomorUser").equalTo(newText)
            .addListenerForSingleValueEvent(object : ValueEventListener{

                override fun onDataChange(p0: DataSnapshot) {
                    //Menambahkan Listener dan mengecek data yang dimasukan, apakah ada atau tidak
                    progressBar.visibility = View.VISIBLE
                    if(!p0.exists()){
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@SearchFamilyActivity, "Nomor Tidak Ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(p0: DatabaseError) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@SearchFamilyActivity, "Pencarian Gagal Dimuat", Toast.LENGTH_SHORT).show()
                }

            })

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<VictimData, SearchFamilyActivity.UsersViewHolder>(options) {

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchFamilyActivity.UsersViewHolder {
                return SearchFamilyActivity.UsersViewHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.family_item_layout, parent, false))
            }

            override fun onBindViewHolder(holder: SearchFamilyActivity.UsersViewHolder, position: Int, model: VictimData) {

                //Mendaptkan data-data user (Keluarga) dan menampilkannya pada masing2 View
                holder.setDisplayName(model.namaUser.toString())
                holder.setDisplayNomor(model.nomorUser.toString())

                progressBar.visibility = View.GONE

                //Mendapatkan kode unik (User ID)
                val getKey = getRef(holder.adapterPosition).key

                holder.mView?.setOnClickListener {
                    toMapActivity(model, getKey.toString())
                }
            }

            override fun onError(error: DatabaseError) {
                super.onError(error)
                progressBar.visibility = View.GONE
                Toast.makeText(this@SearchFamilyActivity, "Pencarian Gagal Dimuat", Toast.LENGTH_SHORT).show()
            }

            override fun onDataChanged() {
                super.onDataChanged()
                firebaseRecyclerAdapter?.notifyDataSetChanged()
            }
        }

        //Memasang adapter pada RecyclerView didalam Activity AddFriend
        familySearch?.adapter = firebaseRecyclerAdapter
    }

    private fun toMapActivity(model: VictimData, key: String){
        val action = arrayOf("Tambahkan ke daftar keluarga")
        val alert = AlertDialog.Builder(this@SearchFamilyActivity)
        alert.setTitle("Pilih Opsi")
        alert.setItems(action) { _, i ->
            when (i) {
                0 -> {
                    progressBar.visibility = View.VISIBLE
                    val getUserName = model.namaUser.toString()
                    val getUserNomor = model.nomorUser.toString()
                    val getUserStatus = model.victimStatus
                    val getLng = model.lng.toString()
                    val getLat = model.lat.toString()

                    familyRefrence?.orderByChild("nomorUser")?.equalTo(getUserNomor)
                        ?.addListenerForSingleValueEvent(object : ValueEventListener{
                            override fun onCancelled(p0: DatabaseError) {}

                            override fun onDataChange(p0: DataSnapshot) {
                                if(p0.exists()){
                                    progressBar.visibility = View.GONE
                                    Toast.makeText(this@SearchFamilyActivity, "Tidak dapat menambahkan data yang sama", Toast.LENGTH_SHORT).show()
                                }else{
                                    familyRefrence?.child(key)?.setValue(
                                        VictimData(getUserName.toLowerCase(), getUserName, getUserNomor, getLat,getLng, getUserStatus)
                                    )?.addOnSuccessListener {
                                            progressBar.visibility = View.GONE
                                            Toast.makeText(this@SearchFamilyActivity, "Anggota keluarga berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                                        }
                                }
                            }
                        })
                }
            }
        }
        alert.create()
        alert.show()
    }

    override fun onPause() {
        super.onPause()
        providerClient?.removeLocationUpdates(locationCallback)
    }
}