package com.example.match

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatRadioButton
import androidx.appcompat.widget.AppCompatToggleButton
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.play.integrity.internal.ac
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreatePartyActivity : AppCompatActivity() {
    private var matchIdForFirebase: Int = -1
    private lateinit var create_ETXT_name : AppCompatEditText
    private lateinit var create_ETXT_address : AppCompatEditText
    private lateinit var create_ETXT_capacity : AppCompatEditText
    private lateinit var create_RBTN_pub : AppCompatRadioButton
    private lateinit var create_RBTN_private : AppCompatRadioButton
    private lateinit var create_ETXT_hour : AppCompatEditText
    private lateinit var create_BTN_create : AppCompatButton
    private lateinit var create_BTN_back : AppCompatImageButton
    private lateinit var auth: FirebaseAuth
    private lateinit var db : FirebaseFirestore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_create_party)
        matchIdForFirebase = intent.getIntExtra("MATCH_ID", -1)
        android.util.Log.d("MatchTrace", "CreateParty received ID: $matchIdForFirebase")
        create_ETXT_name = findViewById(R.id.create_ETXT_name)
        create_ETXT_address = findViewById(R.id.create_ETXT_address)
        create_ETXT_capacity = findViewById(R.id.create_ETXT_capacity)
        create_RBTN_pub = findViewById(R.id.create_RBTN_pub)
        create_RBTN_private = findViewById(R.id.create_RBTN_private)
        create_ETXT_hour = findViewById(R.id.create_ETXT_hour)
        create_BTN_create = findViewById(R.id.create_BTN_create)
        create_BTN_back = findViewById(R.id.create_BTN_back)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setUpCreateButton()
        back()

    }

    private fun back() {
        create_BTN_back.setOnClickListener {
            finish()
        }
    }

    private fun setUpCreateButton() {
        create_BTN_create.setOnClickListener {
            val eventName = create_ETXT_name.text.toString()
            val eventAddress = create_ETXT_address.text.toString()
            val capacity = create_ETXT_capacity.text.toString()
            val hour = create_ETXT_hour.text.toString()
            val venueType = if (create_RBTN_pub.isChecked) "Pub" else "Private"
            if (eventName.isEmpty() || eventAddress.isEmpty() || capacity.isEmpty() || hour.isEmpty()) {
                Toast.makeText(this, "Please fill in all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val curUser = auth.currentUser
            if (curUser != null)
            {
                db.collection("users").document(curUser.uid).get()
                    .addOnSuccessListener { document ->
                        val actualHostName = document.getString("full name")
                        val newParty = hashMapOf(
                            "matchId" to matchIdForFirebase,
                            "title" to eventName,
                            "address" to eventAddress,
                            "capacity" to capacity,
                            "hour" to hour,
                            "venueType" to venueType,
                            "hostName" to actualHostName,
                            "hostId" to curUser.uid,
                            "joined" to 0
                        )
                        db.collection("watch_parties")
                            .add(newParty)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "The event was successfully created",
                                    Toast.LENGTH_SHORT
                                ).show()
                                finish()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error creating the event", Toast.LENGTH_SHORT)
                                    .show()
                            }
                    }

            }

        }

    }


}