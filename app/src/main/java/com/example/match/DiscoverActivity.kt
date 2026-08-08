package com.example.match

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.FirebaseFirestore

class DiscoverActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var discover_BTN_back : AppCompatImageButton
    private lateinit var discover_RCV_eventslist : RecyclerView
    private lateinit var discover_BTN_add : AppCompatImageButton
    private lateinit var mMap: GoogleMap
    private lateinit var db : FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_discover)
        discover_BTN_add = findViewById(R.id.discover_BTN_add)
        discover_BTN_back = findViewById(R.id.discover_BTN_back)
        discover_RCV_eventslist = findViewById(R.id.discover_RCV_eventslist)
        db = FirebaseFirestore.getInstance()
        val currentMatchId = intent.getIntExtra("MATCH_ID", -1)
        android.util.Log.d("MatchTrace", "Discover received ID: $currentMatchId")
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        backToMain()
        addParty(currentMatchId)
        discover_RCV_eventslist.layoutManager = LinearLayoutManager(this)
    }

    override fun onResume() {
        super.onResume()
        val currentMatchId = intent.getIntExtra("MATCH_ID",-1)
        if (currentMatchId!=-1)
        {
            loadPartiesFromFirebase(currentMatchId)
        }
    }

    private fun addParty(currentMatchId:Int) {
        discover_BTN_add.setOnClickListener {
            val intent = Intent(this, CreatePartyActivity::class.java)
            android.util.Log.d("MatchTrace", "Discover is passing ID: $currentMatchId to Create")
            intent.putExtra("MATCH_ID", currentMatchId)
            startActivity(intent)
            finish()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

    }
    private fun getLatLngFromAddress(address: String): LatLng? {
        val geocoder = android.location.Geocoder(this)
        return try {
            val addresses = geocoder.getFromLocationName(address, 1)
            if (!addresses.isNullOrEmpty()) {
                val location = addresses[0]
                LatLng(location.latitude, location.longitude)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun backToMain() {
        discover_BTN_back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun loadPartiesFromFirebase(matchId: Int){
        db.collection( "watch_parties").whereEqualTo("matchId", matchId)
            .get().addOnSuccessListener {documents ->
                if(documents.isEmpty)
                {
                    Toast.makeText(this,"No events yet", Toast.LENGTH_SHORT).show()
                    discover_RCV_eventslist.adapter = EventAdapter(emptyList())
                    if (::mMap.isInitialized) {
                        mMap.clear()
                    }
                }
                else
                {
                    val partyList= mutableListOf<WatchParty>()
                    for(document in documents)
                    {
                        val party = document.toObject(WatchParty::class.java)
                        party.documentId = document.id
                        partyList.add(party)
                    }
                    discover_RCV_eventslist.adapter = EventAdapter(partyList)
                    if(::mMap.isInitialized)
                    {
                        mMap.clear()
                        var firstLocation: LatLng? = null
                        for (party in partyList)
                        {
                            if(party.address.isNotEmpty()) {
                                val location = getLatLngFromAddress(party.address)
                                if (location != null) {
                                    mMap.addMarker(
                                        MarkerOptions()
                                            .position(location)
                                            .title(party.title)
                                            .snippet(party.venueType)
                                    )
                                    if (firstLocation == null) {
                                        firstLocation = location
                                    }
                                }
                            }
                        }
                        if (firstLocation != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(firstLocation, 12f))
                        }

                    }

                }


            }
            .addOnFailureListener { exception ->
                Toast.makeText(this , "Error loading data" , Toast.LENGTH_SHORT).show()

            }

    }
}