package com.ext.ext_firebase

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.ext.ext_firebase.databinding.ActivityRegisterBinding
import com.ext.ext_firebase.utils.validateEmailAddress
import com.ext.ext_firebase.utils.validateName
import com.ext.ext_firebase.utils.validatePassword
import com.ext.ext_firebase.utils.validatePhoneNumber
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.util.UUID

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var database: DatabaseReference
    private lateinit var storageReference: StorageReference
    private lateinit var firestore: FirebaseFirestore
    private val PICK_IMAGE_REQUEST = 22
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance().getReference("Users")
        storageReference = FirebaseStorage.getInstance().getReference("profile_images")

        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.tvLogin.setOnClickListener {
            val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
            startActivity(intent)
        }

        binding.imgUser.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Choose Image to Upload"), PICK_IMAGE_REQUEST)
        }

        binding.tvRegister.setOnClickListener {
            if (validateName(this@RegisterActivity, binding.etName) &&
                validatePhoneNumber(this@RegisterActivity, binding.etPhone) &&
                validateEmailAddress(this@RegisterActivity, binding.etEmail) &&
                validatePassword(this@RegisterActivity, binding.etPassword)
            ) {
                val name = binding.etName.text.toString().trim()
                val phone = binding.etPhone.text.toString().trim()
                val email = binding.etEmail.text.toString().trim()
                val password = binding.etPassword.text.toString().trim()

                val userId = database.push().key!!

                SharedPreferenceManager.saveUserId(this, userId)
                SharedPreferenceManager.setLoggedIn(this@RegisterActivity, true)

                saveUserToDatabase(userId, name, phone, email, password, "")

//                if (imageUri != null) {
//                    uploadImage(userId, name, phone, email, password)
//                } else {
//                    saveUserToDatabase(userId, name, phone, email, password, "")
//                }
            }
        }
    }

//    private fun uploadImage(userId: String, name: String, phone: String, email: String, password: String) {
//        val ref = storageReference.child("${userId}/profile_image_${UUID.randomUUID()}")
//        ref.putFile(imageUri!!)
//            .addOnSuccessListener {
//                ref.downloadUrl.addOnSuccessListener { uri ->
//                    saveUserToDatabase(userId, name, phone, email, password, uri.toString())
//                }
//            }
//            .addOnFailureListener { e ->
//                Toast.makeText(this, "Image Upload Failed: ${e.message}", Toast.LENGTH_SHORT).show()
//            }
//
//    }

    private fun saveUserToDatabase(userId: String, name: String, phone: String, email: String, password: String, profileImageUrl: String) {
        val user = User(userId, name, phone, email, password, profileImageUrl)

        database.child(userId).setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "User Registered Successfully!", Toast.LENGTH_SHORT).show()
                binding.etName.text?.clear()
                binding.etPhone.text?.clear()
                binding.etEmail.text?.clear()
                binding.etPassword.text?.clear()
                binding.imgUser.setImageResource(R.drawable.user_big)
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to Register User", Toast.LENGTH_SHORT).show()
            }
    }

//    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
//            imageUri = data.data
//            binding.imgUser.setImageURI(imageUri) // J image select krisu e imageView ma batavase.
//        }
//    }

    override fun onBackPressed() {
        super.onBackPressed()
    }
}