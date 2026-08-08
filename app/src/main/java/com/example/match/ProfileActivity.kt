package com.example.match

import android.content.Intent
import android.media.Image
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.E

class ProfileActivity : AppCompatActivity() {
    private lateinit var profile_TXT_name: AppCompatTextView
    private lateinit var profile_TXT_israeli_team: AppCompatTextView
    private lateinit var profile_TXT_national_team: AppCompatTextView
    private lateinit var profile_IMG_israeli_team: AppCompatImageView
    private lateinit var profile_IMG_national_team: AppCompatImageView
    private lateinit var profile_BTN_back: AppCompatImageButton
    private lateinit var profile_IMG_photo: AppCompatImageView
    private lateinit var profile_BTN_add: AppCompatImageButton
    private lateinit var profile_RCV_parties : RecyclerView
    private lateinit var profile_BTN_host : AppCompatButton
    private lateinit var profile_BTN_joined : AppCompatButton
    private lateinit var profile_BTN_notifications : AppCompatImageButton
    private lateinit var profile_IMG_dot : AppCompatImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private var imageUri: android.net.Uri? = null
    private lateinit var storage: com.google.firebase.storage.FirebaseStorage



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        profile_TXT_name = findViewById(R.id.profile_TXT_name)
        profile_TXT_israeli_team = findViewById(R.id.profile_TXT_israeli_team)
        profile_TXT_national_team = findViewById(R.id.profile_TXT_national_team)
        profile_IMG_israeli_team = findViewById(R.id.profile_IMG_israeli_team)
        profile_IMG_national_team = findViewById(R.id.profile_IMG_national_team)
        profile_BTN_back = findViewById(R.id.profile_BTN_back)
        profile_IMG_photo = findViewById(R.id.profile_IMG_photo)
        profile_BTN_add = findViewById(R.id.profile_BTN_add)
        profile_BTN_host = findViewById(R.id.profile_BTN_host)
        profile_BTN_joined =findViewById(R.id.profile_BTN_joined)
        profile_BTN_notifications = findViewById(R.id.profile_BTN_notifications)
        profile_IMG_dot = findViewById(R.id.profile_IMG_dot)
        storage = com.google.firebase.storage.FirebaseStorage.getInstance()
        profile_RCV_parties = findViewById(R.id.profile_RCV_parties)
        profile_RCV_parties.layoutManager = LinearLayoutManager(this)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.profile)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        getUserName()
        getIsraeliTeam()
        getNationalTeam()
        getProfilePicture()
        loadHostedParties()
        back()
        addProfilePhoto()
        profile_BTN_host.setOnClickListener {
            loadHostedParties()
        }
        profile_BTN_joined.setOnClickListener {
            loadJoinedParties()
        }
        goToNotifications()

    }
    override fun onResume() {
        super.onResume()
        checkForPendingRequests()
    }

    private fun checkForPendingRequests() {
        val curUserId = auth.currentUser?.uid ?: return

        db.collection("watch_parties")
            .whereEqualTo("hostId", curUserId)
            .get()
            .addOnSuccessListener { partiesResult ->
                var hasPending = false

                // עוברים על כל המסיבות שאני המארח שלהן
                for (partyDoc in partiesResult) {
                    val pendingIds = partyDoc.get("pendingRequests") as? List<String> ?: emptyList()

                    // ברגע שמצאנו לפחות בקשה אחת, משנים את הדגל ל-true ועוצרים את הלולאה (אין טעם להמשיך לחפש)
                    if (pendingIds.isNotEmpty()) {
                        hasPending = true
                        break
                    }
                }
                if(hasPending)
                {
                    profile_IMG_dot.visibility = View.VISIBLE
                }
                else
                {
                    profile_IMG_dot.visibility = View.GONE

                }
            }

    }

    private fun goToNotifications() {
       profile_BTN_notifications.setOnClickListener {
           val intent = Intent(this, NotificationsActivity::class.java)
           startActivity(intent)
           finish()
       }
    }

    fun loadHostedParties() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("watch_parties")
            .whereEqualTo("hostId",userId).get()
            .addOnSuccessListener { result ->
                val partiesList = mutableListOf<WatchParty>()
                for (document in result) {
                    val party = document.toObject(WatchParty::class.java)
                    party.documentId = document.id
                    partiesList.add(party)
                }
                profile_RCV_parties.adapter = EventAdapter(partiesList)
            }
            .addOnFailureListener {
                Toast.makeText(this,"Error loading parties", Toast.LENGTH_SHORT).show()
            }



    }

    private fun loadJoinedParties() {
        val userId = auth.currentUser?.uid ?: return
        db.collection("watch_parties")
            .whereArrayContains("participants",userId).get()
            .addOnSuccessListener { result ->
                val partiesList = mutableListOf<WatchParty>()
                for (document in result) {
                    val party = document.toObject(WatchParty::class.java)
                    party.documentId = document.id
                    partiesList.add(party)
                }
                profile_RCV_parties.adapter = EventAdapter(partiesList)
            }
            .addOnFailureListener {
                Toast.makeText(this,"Error loading parties", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addProfilePhoto() {
        profile_BTN_add.setOnClickListener { pickImageLauncher.launch("image/*") }
    }

    private fun back() {
        profile_BTN_back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getNationalTeam() {
        val curUser = auth.currentUser
        if (curUser != null) {
            val userId = curUser.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val nationalTeam = document.getString("national team")
                        if (nationalTeam != null) {
                            profile_IMG_national_team.visibility = View.VISIBLE
                            profile_IMG_national_team.visibility = View.VISIBLE
                            profile_TXT_national_team.text = nationalTeam
                            val formatedName = nationalTeam.lowercase().replace(" ", "_")
                            val resourceId =
                                resources.getIdentifier(formatedName, "drawable", packageName)
                            if (resourceId != 0) {
                                profile_IMG_national_team.setImageResource(resourceId)
                            } else {
                                profile_IMG_national_team.setImageResource(R.drawable.match_logo)
                            }
                        } else {
                            profile_IMG_national_team.visibility = View.GONE
                            profile_TXT_national_team.visibility = View.GONE
                        }

                    }

                }
        }
    }

    private fun getIsraeliTeam() {
        val curUser = auth.currentUser
        if (curUser != null) {
            val userId = curUser.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val israeliTeam = document.getString("israeli team")
                        if (israeliTeam != null) {
                            profile_IMG_israeli_team.visibility = View.VISIBLE
                            profile_TXT_israeli_team.visibility = View.VISIBLE
                            profile_TXT_israeli_team.text = israeliTeam
                            val formatedName = israeliTeam.lowercase().replace(" ", "_")
                            val resourceId =
                                resources.getIdentifier(formatedName, "drawable", packageName)
                            if (resourceId != 0) {
                                profile_IMG_israeli_team.setImageResource(resourceId)
                            } else {
                                profile_IMG_israeli_team.setImageResource(R.drawable.match_logo)
                            }
                        } else {
                            profile_IMG_israeli_team.visibility = View.GONE
                            profile_TXT_israeli_team.visibility = View.GONE
                        }

                    }

                }
        }
    }

    private fun getUserName() {
        val curUser = auth.currentUser
        if (curUser != null) {
            val userId = curUser.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val fullName = document.getString("full name")
                        if (fullName != null) {
                            profile_TXT_name.text = fullName
                        }
                    } else {
                        Toast.makeText(this, "document not found", Toast.LENGTH_SHORT).show()
                    }

                }
        } else {
            Toast.makeText(this, "Error retrieving data", Toast.LENGTH_SHORT).show()
        }

    }

    private val pickImageLauncher = registerForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            imageUri = uri
            com.bumptech.glide.Glide.with(this)
                .load(uri)
                .skipMemoryCache(true)
                .diskCacheStrategy(com.bumptech.glide.load.engine.DiskCacheStrategy.NONE)
                .circleCrop()
                .into(profile_IMG_photo)
            uploadImageToFirebase(uri)
        } else {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase(imageUri: android.net.Uri) {
        val curUser = auth.currentUser
        if (curUser != null) {
            val userId = curUser.uid
            val storageRef = storage.reference.child("profile_images/$userId.jpg")
            storageRef.putFile(imageUri)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        db.collection("users").document(userId).update("profileImage", imageUrl)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    this, "The profile picture has been successfully updated",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                        .addOnFailureListener {
                            Toast.makeText(this, "Error uploading the image", Toast.LENGTH_SHORT)
                                .show()
                        }
                }
        }
    }

    private fun getProfilePicture() {
        val curUser = auth.currentUser
        if (curUser != null) {
            val userId = curUser.uid
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val imageUrl = document.getString("profileImage")

                        if (imageUrl != null) {
                            com.bumptech.glide.Glide.with(this)
                                .load(imageUrl)
                                .circleCrop()
                                .into(profile_IMG_photo)
                        }
                    }
                }
        }
    }
}