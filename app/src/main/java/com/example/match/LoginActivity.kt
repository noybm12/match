package com.example.match

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class LoginActivity : AppCompatActivity() {
    private lateinit var login_ETXT_Email: AppCompatEditText
    private lateinit var login_ETXT_Password : AppCompatEditText
    private lateinit var login_BTN_Login : AppCompatButton
    private lateinit var login_TXT_SignUp : AppCompatTextView
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        login_ETXT_Email = findViewById(R.id.login_ETXT_Email)
        login_ETXT_Password = findViewById(R.id.login_ETXT_Password)
        login_BTN_Login = findViewById(R.id.login_BTN_login)
        login_TXT_SignUp = findViewById(R.id.login_TXT_SignUp)
        auth = FirebaseAuth.getInstance()
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        setUpLoginButton()
        toSignUp()

    }

    private fun toSignUp() {
        login_TXT_SignUp.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun setUpLoginButton() {
        login_BTN_Login.setOnClickListener {
            val email = login_ETXT_Email.text.toString().trim()
            val password = login_ETXT_Password.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
                .addOnFailureListener { exception ->
                    when (exception) {
                        is FirebaseNetworkException -> {
                            Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
                        }
                        is FirebaseAuthInvalidUserException, is FirebaseAuthInvalidCredentialsException -> {
                            Toast.makeText(this, "Wrong email or password", Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            Toast.makeText(this, "Login failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
        }

    }
}