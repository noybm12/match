package com.example.match

import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatSpinner
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class RegisterActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var register_ETXT_FullName: AppCompatEditText
    private lateinit var register_ETXT_Password: AppCompatEditText
    private lateinit var register_ETXT_Email: AppCompatEditText

    private lateinit var register_Spin_Israeli_Team: AppCompatSpinner
    private lateinit var register_Spin_National_Team: AppCompatSpinner

    private lateinit var register_BTN_register: AppCompatButton
    private lateinit var register_BTN_back: AppCompatImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        register_ETXT_FullName = findViewById(R.id.register_ETXT_FullName)
        register_ETXT_Email = findViewById(R.id.register_ETXT_EmailAddress)
        register_ETXT_Password = findViewById(R.id.register_ETXT_Password)
        register_Spin_Israeli_Team = findViewById(R.id.register_Spin_Israeli_Team)
        register_Spin_National_Team = findViewById(R.id.register_Spin_National_Team)
        register_BTN_register = findViewById(R.id.register_BTN_register)
        register_BTN_back = findViewById(R.id.register_BTN_back)
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        /*
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets

        }
         */
        setUpRgisterButton()
        setUpBackButton()
    }

    private fun setUpBackButton() {
        register_BTN_back.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun getFullName(): String?
    {
        val fullName = register_ETXT_FullName.text.toString().trim()
        if(fullName.isEmpty())
        {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
            return null;
        }
        if(!fullName.contains(" "))
        {
            Toast.makeText(this, "Please enter your full name", Toast.LENGTH_SHORT).show()
            return null;
        }
        val nameRegex = "^[a-zA-Z ]+$".toRegex()
        if(!fullName.matches(nameRegex))
        {
            Toast.makeText(this, "Name must be only letters", Toast.LENGTH_SHORT).show()
            return null;
        }
        return fullName
    }
    private fun getEmail(): String?
    {
        val email = register_ETXT_Email.text.toString().trim()
        if(email.isEmpty())
        {
            Toast.makeText(this, "Please enter your Email", Toast.LENGTH_SHORT).show()
            return null;
        }
        if(!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches())
        {
            Toast.makeText(this, "Invalid email", Toast.LENGTH_SHORT).show()
            return null;
        }
        return email
    }
    private fun getPassword(): String?
    {
        val password = register_ETXT_Password.text.toString().trim()
        if(password.isEmpty())
        {
            Toast.makeText(this, "Please enter password", Toast.LENGTH_SHORT).show()
            return null;
        }
        if(password.length<8)
        {
            Toast.makeText(this, "Password must contains at least 8 characters", Toast.LENGTH_SHORT).show()
            return null;
        }
        return password
    }
    private fun getIsraeliTeam(): String?
    {
        val israeliTeam = register_Spin_Israeli_Team.selectedItem.toString()
        if(israeliTeam.equals("Choose a team"))
        {
            return null;
        }
        return israeliTeam;
    }
    private fun getNationalTeam(): String?
    {
        val nationalTeam = register_Spin_National_Team.selectedItem.toString()
        if(nationalTeam.equals("Choose a team"))
        {
            return null;
        }
        return nationalTeam;
    }

    private fun setUpRgisterButton() {
        register_BTN_register.setOnClickListener {
            val validFullName = getFullName()
            val validEmail = getEmail()
            val validPassword = getPassword()
            val nationalTeam = getNationalTeam()
            val israeliTeam = getIsraeliTeam()
            if (validFullName == null || validEmail == null || validPassword == null) {
                return@setOnClickListener
            }
            if (nationalTeam == null && israeliTeam == null) {
                Toast.makeText(this, "Please choose a team", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            auth.createUserWithEmailAndPassword(validEmail, validPassword)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            val userMap = mutableMapOf<String, Any>(
                                "full name" to validFullName,
                                "email" to validEmail,
                            )
                            if (israeliTeam != null) {
                                userMap["israeli team"] = israeliTeam
                            }
                            if (nationalTeam != null) {
                                userMap["national team"] = nationalTeam
                            }
                            db.collection("users").document(userId).set(userMap)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Registration was successful",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    val intent = Intent(this, MainActivity::class.java)
                                    startActivity(intent)
                                    finish()
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(
                                        this,
                                        "error in save user${e.message}",
                                        Toast.LENGTH_LONG).show()
                                }
                        }
                        }
                    else {
                        Toast.makeText(
                            this,
                            "Registration error ${task.exception?.message}",
                            Toast.LENGTH_LONG).show()
                    }

                }
        }




        }
    }


