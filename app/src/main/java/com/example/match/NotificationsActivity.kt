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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class NotificationsActivity : AppCompatActivity() {
    private lateinit var notify_BTN_back : AppCompatImageButton
    private lateinit var notify_RV_requests : RecyclerView
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore
    private lateinit var adapter: RequestAdapter
    private val requestsList = mutableListOf<RequestItem>()
    private val processingRequests = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notifications)

        notify_BTN_back = findViewById(R.id.notify_BTN_back)
        notify_RV_requests = findViewById(R.id.notify_RV_requests)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        notify_RV_requests.layoutManager = LinearLayoutManager(this)
        adapter = RequestAdapter(requestsList) { request, position ->
            approveRequest(request, position)
        }
        notify_RV_requests.adapter = adapter
        loadPendingRequests()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        back()
    }

    private fun loadPendingRequests() {
        val curUserId = auth.currentUser?.uid ?: return
        db.collection("watch_parties")
            .whereEqualTo("hostId",curUserId)
            .get()
            .addOnSuccessListener {partiesResult->
                requestsList.clear()
                for(partyDoc in partiesResult){
                    val partyId = partyDoc.id
                    val partyTitle = partyDoc.getString("title") ?: "Unknown Party"
                    val pendingIds = partyDoc.get("pendingRequests") as? List<String> ?: emptyList()
                    if(pendingIds.isNotEmpty())
                    {
                        val chunks = pendingIds.chunked(10)
                        for(chunk in chunks)
                        {
                            db.collection("users")
                                .whereIn(FieldPath.documentId(),chunk)
                                .get()
                                .addOnSuccessListener { usersResult ->
                                    for (userDoc in usersResult) {
                                        val userName = userDoc.getString("full name") ?: "Unknown"
                                        val photoUrl = userDoc.getString("profileImage") ?: ""
                                        requestsList.add(
                                            RequestItem(
                                                userDoc.id,
                                                userName,
                                                photoUrl,
                                                partyId,
                                                partyTitle
                                            )
                                        )
                                    }
                                    adapter.notifyDataSetChanged()
                                }
                        }
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this,"Error loading requsts", Toast.LENGTH_SHORT).show()
            }

    }

    fun approveRequest(request: RequestItem, position: Int) {
        if (processingRequests.contains(request.userId)) {
            return
        }
        processingRequests.add(request.userId)
        val partyRef = db.collection("watch_parties").document(request.partyId)

        partyRef.update(
            "pendingRequests", FieldValue.arrayRemove(request.userId),
            "participants", FieldValue.arrayUnion(request.userId),
            "joined", FieldValue.increment(1)
        ).addOnSuccessListener {
            val currentIndex = requestsList.indexOfFirst { it.userId == request.userId }
            if (currentIndex != -1) {
                adapter.removeItem(currentIndex) // קורא לפונקציה המקורית שבנית באדפטר
                Toast.makeText(this, "you approve ${request.userName}!", Toast.LENGTH_SHORT).show()
            }


        }.addOnFailureListener { e ->
            processingRequests.remove(request.userId)
            Toast.makeText(this, "Error ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }



    private fun back() {
        notify_BTN_back.setOnClickListener {
           val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}