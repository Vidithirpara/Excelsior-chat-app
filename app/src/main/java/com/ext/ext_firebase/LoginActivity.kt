package com.ext.ext_firebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ext.ext_firebase.databinding.ActivityLoginBinding
import com.ext.ext_firebase.utils.validateEmailAddress
import com.ext.ext_firebase.utils.validatePassword
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var firebaseAuth: FirebaseAuth
    val Req_Code: Int = 123
    private lateinit var database: DatabaseReference
    lateinit var user : User

    companion object {
        const val REQ_CODE = 123
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference("Users")

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        user = User()

        setOnClickListener()
    }

    private fun setOnClickListener(){
        binding.tvLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateEmailAddress(this@LoginActivity, binding.etEmail) && validatePassword(this@LoginActivity, binding.etPassword)){
                loginUser(email, password)
            }
        }

        binding.tvRegister.setOnClickListener {
            val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
            startActivity(intent)
        }

        binding.imgGoogle.setOnClickListener {
            signInGoogle()
        }
    }

    private fun loginUser(email: String, password: String) {
        database.orderByChild("email").equalTo(email).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    for (userSnapshot in snapshot.children) {
                        val userId = userSnapshot.key.toString()
                        val storedPassword = userSnapshot.child("password").getValue(String::class.java)

                        if (storedPassword == password) {
                            Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                            SharedPreferenceManager.saveUserId(this@LoginActivity, userId)
                            SharedPreferenceManager.setLoggedIn(this@LoginActivity, true)
                            val intent = Intent(this@LoginActivity, NameActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            Toast.makeText(this@LoginActivity, "Incorrect Password", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this@LoginActivity, "User does not exist", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@LoginActivity, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun signInGoogle() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQ_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_CODE) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleGoogleSignInResult(task)
        }
    }

    private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
        try {
            val account: GoogleSignInAccount? = task.getResult(ApiException::class.java)
            if (account != null) {
                authenticateWithFirebase(account)
            }
        } catch (e: ApiException) {
            Toast.makeText(this, "Google Sign-In failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun authenticateWithFirebase(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                val user = firebaseAuth.currentUser
                user?.let {
                    val userId = it.uid
                    val userEmail = it.email ?: ""
                    val userName = it.displayName ?: ""
//                    val profileImageUrl = it.photoUrl?.toString() ?: ""

                    // Save user to Realtime Database
                    val newUser = User(userId, userName, "", userEmail, "", "")
                    database.child(userId).setValue(newUser).addOnSuccessListener {
                        Toast.makeText(this, "Welcome $userName!", Toast.LENGTH_SHORT).show()
                        SharedPreferenceManager.setLoggedIn(this@LoginActivity, true)
                        SharedPreferenceManager.saveUserId(this@LoginActivity, userId)
                        navigateToHome()
                    }.addOnFailureListener { e ->
                        Toast.makeText(this, "Failed to save user: ${e.message}", Toast.LENGTH_SHORT).show()
                    }

                    SharedPreferenceManager.setEmail(this, userEmail)
                    SharedPreferenceManager.setUsername(this, userName)
                }
            } else {
                Toast.makeText(this, "Firebase Authentication failed.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun navigateToHome() {
        val intent = Intent(this, NameActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onStart() {
        super.onStart()
        if (GoogleSignIn.getLastSignedInAccount(this) != null || firebaseAuth.currentUser != null) {
            navigateToHome()
        }
    }

}