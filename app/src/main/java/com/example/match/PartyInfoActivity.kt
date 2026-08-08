package com.example.match

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import okhttp3.internal.userAgent

class PartyInfoActivity : AppCompatActivity() {
    private lateinit var partyinfo_TXT_name :   AppCompatTextView
    private lateinit var partyinfo_TXT_address: AppCompatTextView
    private lateinit var partyinfo_TXT_time  : AppCompatTextView
    private lateinit var partyinfo_TXT_host : AppCompatTextView
    private lateinit var partyinfo_BTN_back : AppCompatImageButton
    private lateinit var partyinfo_BTN_join : AppCompatButton
    private lateinit var matchDetails : CardView
    private lateinit var partyinfo_RV_participants:  RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_party_info)
        partyinfo_TXT_name = findViewById(R.id.partyinfo_TXT_name)
        partyinfo_TXT_address = findViewById(R.id.partyinfo_TXT_address)
        partyinfo_TXT_time = findViewById(R.id.partyinfo_TXT_time)
        partyinfo_TXT_host = findViewById(R.id.partyinfo_TXT_host)
        matchDetails = findViewById(R.id.single_match_card)
        partyinfo_BTN_back = findViewById(R.id.partyinfo_BTN_back)
        partyinfo_BTN_join = findViewById(R.id.partyinfo_BTN_join)
        partyinfo_RV_participants = findViewById(R.id.partyinfo_RV_participants)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val title = intent.getStringExtra("PARTY_TITLE")?:""
        val host = intent.getStringExtra("PARTY_HOST")?:""
        val address = intent.getStringExtra("PARTY_ADDRESS")?:""
        val hour = intent.getStringExtra("PARTY_HOUR") ?: ""
        val matchId = intent.getIntExtra("MATCH_ID",-1)
        val partyDocId = intent.getStringExtra("PARTY_DOC_ID")
        val eventHostId = intent.getStringExtra("PARTY_HOST_ID")

        partyinfo_TXT_name.text = title
        partyinfo_TXT_host.text = "Host: $host"
        partyinfo_TXT_address.text = address
        partyinfo_TXT_time.text = hour
        if(matchId != -1)
        {
            fetchMatchDetailsFromApi(matchId)
        }
        if (partyDocId != null) {
            loadParticipants(partyDocId)
        }
        back()
        joinParty(partyDocId, eventHostId )
    }

    private fun joinParty(partyId : String?, hostId: String?) {
        val curUser = auth.currentUser
        val userId = curUser?.uid

        // בדיקה 1: אם אני ה-Host, אני לא יכול להצטרף
        if(userId!=null && userId == hostId)
        {
            partyinfo_BTN_join.text = "My Event"
            partyinfo_BTN_join.isEnabled = false
        }
        else {
            partyinfo_BTN_join.setOnClickListener {
                if (userId == null) {
                    Toast.makeText(this, "user not found", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (partyId != null) {
                    val partyRef = db.collection("watch_parties").document(partyId)
                    partyRef.get().addOnSuccessListener { document ->
                        if (document.exists()) {
                            val capacityStr = document.getString("capacity") ?: "0"
                            val capacity = capacityStr.toIntOrNull() ?: 0
                            val participants = document.get("participants") as? List<String> ?: emptyList()
                            // שולפים גם את רשימת הממתינים כדי לבדוק אותה
                            val pendingRequests = document.get("pendingRequests") as? List<String> ?: emptyList()

                            // בדיקה 2: אם אני כבר רשום כמשתתף
                            if (participants.contains(userId)) {
                                Toast.makeText(this, "You are already registered", Toast.LENGTH_SHORT).show()
                                partyinfo_BTN_join.text = "Joined"
                                partyinfo_BTN_join.isEnabled = false
                                return@addOnSuccessListener
                            }

                            // בדיקה חדשה: האם כבר שלחתי בקשה?
                            if (pendingRequests.contains(userId)) {
                                Toast.makeText(this, "Your request is pending approval", Toast.LENGTH_SHORT).show()
                                partyinfo_BTN_join.text = "Pending"
                                partyinfo_BTN_join.isEnabled = false
                                return@addOnSuccessListener
                            }

                            // בדיקה 3: האם הרשימה מלאה?
                            if (participants.size < capacity) {
                                // שינוי מרכזי: מוסיפים לרשימת הממתינים, ולא מעלים את מספר ה-joined!
                                partyRef.update("pendingRequests", FieldValue.arrayUnion(userId))
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "Request sent successfully!", Toast.LENGTH_SHORT).show()
                                        partyinfo_BTN_join.text = "Pending"
                                        partyinfo_BTN_join.isEnabled = false
                                        // הורדנו מכאן את רענון רשימת המשתתפים, כי הוא עדיין לא אושר ולכן אין מה לרענן למטה
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "error ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            } else {
                                Toast.makeText(this, "The event is full, joining is not possible", Toast.LENGTH_SHORT).show()
                                partyinfo_BTN_join.text = "Full"
                                partyinfo_BTN_join.isEnabled = false
                            }
                        }
                    }
                        .addOnFailureListener {
                            Toast.makeText(this, "Connection Error", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(this, "ERROR, Event not found", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    private fun loadParticipants(partyId : String){
        partyinfo_RV_participants.layoutManager = LinearLayoutManager(this)
        db.collection("watch_parties").document(partyId).get()
            .addOnSuccessListener { documnet ->
                val participantIds = documnet.get("participants") as? List<String> ?: emptyList()
                if (participantIds.isEmpty()) {
                    partyinfo_RV_participants.adapter = ParticipantAdapter(emptyList())
                    return@addOnSuccessListener
                }
                val chunks = participantIds.chunked(10)
                val participantsList = mutableListOf<Participant>()
                var completedQueries = 0
                for (chunk in chunks) {

                    db.collection("users").whereIn(FieldPath.documentId(), chunk)
                        .get()
                        .addOnSuccessListener { usersResult ->
                            for (userDoc in usersResult) {
                                val name =
                                    userDoc.getString("name") ?: userDoc.getString("full name")
                                    ?: "Unknown"
                                val photoUrl =
                                    userDoc.getString("profileImage") ?: userDoc.getString("image")
                                    ?: ""
                                participantsList.add(Participant(userDoc.id, name, photoUrl))
                            }
                            completedQueries++
                            if (completedQueries == chunks.size){
                                partyinfo_RV_participants.adapter = ParticipantAdapter(participantsList)
                            }
                        }

                }


            }

    }


    private fun back() {
        partyinfo_BTN_back.setOnClickListener {
            finish()
        }
    }

    private fun fetchMatchDetailsFromApi(matchId: Int)
    {
        lifecycleScope.launch {
            try {
                val myApiKey = "8ad070b0df14ccc61c2aa8ea28f80c11"
                val response = RetrofitClient.apiService.getMatchById(myApiKey,matchId)
                if (response.isSuccessful && response.body() != null)
                {
                    val apiResponse = response.body()!!
                    if(apiResponse.matches.isNotEmpty())
                    {
                        val matchData = apiResponse.matches[0]
                        val homeTeamTxt = findViewById<TextView>(R.id.match_TXT_team1_name)
                        val awayTeamTxt = findViewById<TextView>(R.id.match_TXT_team2_name)
                        val dateTxt = findViewById<TextView>(R.id.match_TXT_date)
                        val homeTeamImg = findViewById<ImageView>(R.id.match_IMG_team1)
                        val awayTeamImg = findViewById<ImageView>(R.id.match_IMG_team2)
                        val league = findViewById<TextView>(R.id.match_TXT_league)
                        homeTeamTxt.text = matchData.teams.home.name
                        awayTeamTxt.text = matchData.teams.away.name
                        dateTxt.text = matchData.fixture.date
                        homeTeamImg.load(matchData.teams.home.logo)
                        awayTeamImg.load(matchData.teams.away.logo)
                        league.text = matchData.league.name

                    }
                    else
                    {
                        Toast.makeText(this@PartyInfoActivity,"Empty response from api", Toast.LENGTH_SHORT).show()
                    }
                }

            } catch (e: Exception)
            {
                Toast.makeText(this@PartyInfoActivity,"Service Error", Toast.LENGTH_SHORT).show()
            }
        }
    }

}