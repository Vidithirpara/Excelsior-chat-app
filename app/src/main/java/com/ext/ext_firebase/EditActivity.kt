package com.ext.ext_firebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ext.ext_firebase.databinding.ActivityEditBinding
import com.ext.ext_firebase.utils.validateEmailAddress
import com.ext.ext_firebase.utils.validateName
import com.ext.ext_firebase.utils.validatePassword
import com.ext.ext_firebase.utils.validatePhoneNumber
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class EditActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEditBinding
    private lateinit var dbReference: DatabaseReference
    lateinit var mGoogleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        dbReference = FirebaseDatabase.getInstance().getReference("Users")

        user = User()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        fetchUserData()
        setOnClickListener()
        memoryAllocation()

    }

    private fun setOnClickListener() {
        binding.imgLogOut.setOnClickListener {
            logout()
        }

        binding.tvEditProfile.setOnClickListener {
            binding.etName.isEnabled = true
            binding.etPhone.isEnabled = true
            binding.etPassword.isEnabled = true
            binding.tvSubmitNow.visibility = View.VISIBLE
        }

        binding.tvSubmitNow.setOnClickListener {
            updateData()
        }
    }

    private fun memoryAllocation() {
        binding.etName.isEnabled = false
        binding.etEmail.isEnabled = false
        binding.etPhone.isEnabled = false
        binding.etPassword.isEnabled = false
    }

    private fun fetchUserData() {
        val userId1 = SharedPreferenceManager.getUserId(this@EditActivity)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId2 = firebaseUser?.uid.toString()

        if (userId1 != null) {
            //  J USER MANUALLY LOGIN THYO HSE ENA DATA FETCH KRVA
            dbReference.child(userId1).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User::class.java)
                    if (user != null) {
                        // User manually login krse ena data fetch krva mate If loop execute thase.
                        binding.etName.setText(user.name)
                        binding.etEmail.setText(user.email)
                        binding.etPhone.setText(user.phone)
                        binding.etPassword.setText(user.password)
                    }

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            }
            )
        }
        else{
            //  J USER GOOGLE THI LOGIN THYO HSE ENA DATA FETCH KRVA
            dbReference.child(userId2).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (firebaseUser != null) {
                        binding.etName.setText(firebaseUser.displayName)
                        binding.etEmail.setText(firebaseUser.email)
                        binding.etPhone.setText("")
                        binding.etEmail.isEnabled = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@EditActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
                }
            }
            )
        }
    }

    private fun updateData(){
        val userId1 = SharedPreferenceManager.getUserId(this@EditActivity)
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val userId2 = firebaseUser?.uid

        binding.tvSubmitNow.setOnClickListener {
            if (validateName(this@EditActivity, binding.etName) &&
                validatePhoneNumber(this@EditActivity, binding.etPhone) &&
                validateEmailAddress(this@EditActivity, binding.etEmail) &&
                validatePassword(this@EditActivity, binding.etPassword)
            ) {
                val name = binding.etName.text.toString().trim()
                val phone = binding.etPhone.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()

                if (userId1 != null) {
                    //  J USER MANUALLY LOGIN THYO HSE ENA DATA UPDATE KRVA
                    val user = User(userId1, name, phone, email, password)

                    dbReference.child(userId1).setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile updated Successfully!", Toast.LENGTH_SHORT).show()
                            binding.etName.isEnabled = false
                            binding.etEmail.isEnabled = false
                            binding.etPhone.isEnabled = false
                            binding.tvSubmitNow.visibility = View.GONE
                            fetchUserData()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to Update data", Toast.LENGTH_SHORT).show()
                        }
                }
                else{
                    //  J USER GOOGLE THI LOGIN THYO HSE ENA DATA UPDATE KRVA
                    val user = User(userId2.toString(), name, phone, email, password)

                    dbReference.child(userId2.toString()).setValue(user)
                        .addOnSuccessListener {
                            Toast.makeText(this, "Profile updated Successfully!", Toast.LENGTH_SHORT).show()
                            binding.etName.isEnabled = false
                            binding.etEmail.isEnabled = false
                            binding.etPhone.isEnabled = false
                            binding.tvSubmitNow.visibility = View.GONE
                            fetchUserData()
                        }
                        .addOnFailureListener {
                            Toast.makeText(this, "Failed to Update data", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }

    private fun logout() {
        // Sign out from Firebase
        auth.signOut()

        // Sign out from Google
        mGoogleSignInClient.signOut().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // Clear user data from shared preferences
                SharedPreferenceManager.setLoggedIn(this, false)
                SharedPreferenceManager.clearSession(this@EditActivity)

                // Redirect to LoginActivity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                Toast.makeText(this, "Signed out successfully!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Sign out failed. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

}