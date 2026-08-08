package com.example.match

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageButton
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class MainActivity : AppCompatActivity() {
    private lateinit var main_BTN_profile : AppCompatImageButton
    private lateinit var main_RCV_matches : RecyclerView
    private lateinit var matchAdapter: MatchAdapter
    private lateinit var main_BTN_logOut : AppCompatImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        main_BTN_profile = findViewById(R.id.main_BTN_profile)
        main_RCV_matches = findViewById(R.id.main_RCV_matches)
        main_BTN_logOut = findViewById(R.id.main_BTN_logOut)
        matchAdapter = MatchAdapter(emptyList())
        main_RCV_matches.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(this)
        main_RCV_matches.adapter = matchAdapter
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        goToProfile()
        getTeamsAndMatches()
        main_BTN_logOut.setOnClickListener {
            logOut()
        }

    }

    private fun logOut() {
        auth.signOut()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun getTeamsAndMatches() {
        val curUser = auth.currentUser
        val userId = curUser?.uid
        if(userId!=null) {
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nationalTeam = document.getString("national team")
                        val israeliTeam = document.getString("israeli team")
                        fetchUpcomingMatches(israeliTeam, nationalTeam)
                    }
                }
                .addOnFailureListener { exception ->
                    android.util.Log.e("FIREBASE_ERROR", "Error getting documents: ", exception)
                }
        }

    }

    private fun goToProfile() {
        main_BTN_profile.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    private fun fetchUpcomingMatches(israeliTeam:String?,  nationalTeam: String?)
    {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("API_TEST", "1. מתחילים את הפונקציה! עוקפים את פיירבייס")
                val myApiKey = "8ad070b0df14ccc61c2aa8ea28f80c11"
                val allMatches = mutableListOf<MatchData>()
                if (!israeliTeam.isNullOrEmpty()) {
                    val teamId = TeamsMapper.getTeamId(israeliTeam)
                    Log.d("API_TEST", "מחפש משחקים עבור הקבוצה: $israeliTeam שהיא ID: $teamId")
                    Log.d("API_TEST", "2. מנסים לצאת לאינטרנט לשרת של ה-API...")
                    val response =
                        RetrofitClient.apiService.getTeamMatches(myApiKey, teamId, 2023)
                    Log.d("API_TEST", "3. חזרנו מהשרת! האם הבקשה עברה בהצלחה? ${response.isSuccessful}")
                    if (response.isSuccessful && response.body() != null) {
                        allMatches.addAll(response.body()!!.matches)
                        Log.d("API_TEST", "4. תוצאות שחזרו: ${response.body()!!.results}")
                        Log.d("API_TEST", "5. שגיאות מהשרת: ${response.body()!!.errors}")
                    }
                }
                if (!nationalTeam.isNullOrEmpty()) {
                    val teamId = TeamsMapper.getTeamId(nationalTeam)
                    val response =
                        RetrofitClient.apiService.getTeamMatches(myApiKey, teamId,  2023)
                    if (response.isSuccessful && response.body() != null) {
                        allMatches.addAll(response.body()!!.matches)
                        Log.d("API_TEST", "4. הצלחה! מצאנו ${allMatches.size} משחקים. מעבירים למסך.")
                        val sortedMatches = allMatches.sortedBy { it.fixture.date }
                        withContext(Dispatchers.Main){
                            matchAdapter.updateData(sortedMatches)
                        }
                    }
                    else
                    {
                        Log.e("API_TEST", "שגיאה בתוכן מהשרת: ${response.code()}")
                    }
                }

            }
            catch (e: Exception) {
                Log.e("API_ERROR", "Network failure: ${e.message}")
            }

        }
    }

}