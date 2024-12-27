package com.ext.ext_firebase

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.ext.ext_firebase.databinding.ActivityLoginBinding
import com.ext.ext_firebase.databinding.ActivityNameBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class NameActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNameBinding
    private lateinit var dbReference: DatabaseReference
    private lateinit var userList: ArrayList<User>
    private lateinit var userAdapter: UserAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        dbReference = FirebaseDatabase.getInstance().getReference("Users")

        memoryAllocation()
        fetchUserData()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.tvEditProfile.setOnClickListener {
            val intent = Intent(this@NameActivity, EditActivity::class.java)
            startActivity(intent)
        }
    }

    private fun memoryAllocation(){
        auth = FirebaseAuth.getInstance()

        binding.rvUsers.layoutManager = LinearLayoutManager(this)
        userList = ArrayList()
        userAdapter = UserAdapter(userList)
        binding.rvUsers.adapter = userAdapter

    }

    private fun fetchUserData() {

        val currentUserId = auth.currentUser?.uid

        dbReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                userList.clear()
                for (userSnapshot in snapshot.children) {
                    val user = userSnapshot.getValue(User::class.java)
                    if (user != null && user.userId != currentUserId) {
                        userList.add(user)
                    }
                }
                userAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@NameActivity, "Failed to load user data", Toast.LENGTH_SHORT)
                    .show()
                Log.e("UserListActivity", "Database error: ${error.message}")
            }
        })
    }
}