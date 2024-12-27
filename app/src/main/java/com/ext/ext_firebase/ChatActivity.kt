package com.ext.ext_firebase

import AdapterChat
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.ext.ext_firebase.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatBinding
    private lateinit var adapterChat: AdapterChat
    private val messageList = ArrayList<Chats>()
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    lateinit var user : User
    lateinit var chats: Chats

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userName = intent.getStringExtra("userName")
        binding.userName.text = userName

        user = User()
        chats = Chats()

        database = FirebaseDatabase.getInstance().getReference("Chats")

        memoryAllocation()
        fetchMessages()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        binding.imgBack.setOnClickListener {
            onBackPressed()
        }

        binding.imgSend.setOnClickListener {
            sendMessage()
            adapterChat.notifyDataSetChanged()
        }
    }

    private fun memoryAllocation(){
        auth = FirebaseAuth.getInstance()
        val senderId1 = auth.currentUser?.uid
        val senderId2 = SharedPreferenceManager.getUserId(this)
        val final = if (!senderId1.isNullOrEmpty()) {
            senderId1
        } else {
            senderId2
        }

        adapterChat = AdapterChat(messageList, final.toString())
        binding.rvChats.layoutManager = LinearLayoutManager(this)
        binding.rvChats.adapter = adapterChat
    }

    private fun fetchMessages() {
        val manualLoginSender = SharedPreferenceManager.getUserId(this@ChatActivity)
        val receiverId = intent.getStringExtra("receiverId").toString()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val googleLoginSender = firebaseUser?.uid

        val senderId = if (!manualLoginSender.isNullOrEmpty()) {
            manualLoginSender
        } else if (!googleLoginSender.isNullOrEmpty()) {
            googleLoginSender
        } else {
            null
        }

        if (senderId.isNullOrEmpty() || receiverId.isNullOrEmpty()) {
            Toast.makeText(this, "Sender or Receiver ID is missing!", Toast.LENGTH_SHORT).show()
            return
        }

        val finalId1 = "${manualLoginSender}_${receiverId}"
        val finalId2 = "${receiverId}_${manualLoginSender}"
        val finalId3 = "${googleLoginSender}_${receiverId}"
        val finalId4 = "${receiverId}_${googleLoginSender}"

        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()

                // Check if any of the conversation IDs exist
                val conversationSnapshot = when {
                    snapshot.hasChild(finalId1) -> snapshot.child(finalId1)
                    snapshot.hasChild(finalId2) -> snapshot.child(finalId2)
                    snapshot.hasChild(finalId3) -> snapshot.child(finalId3)
                    snapshot.hasChild(finalId4) -> snapshot.child(finalId4)
                    else -> null
                }

                if (conversationSnapshot != null) {
                    // Fetch messages if a conversation exists
                    for (messageSnapshot in conversationSnapshot.children) {
                        val chat = messageSnapshot.getValue(Chats::class.java)
                        if (chat != null) {
                            messageList.add(chat)
                        }
                    }
                    adapterChat.notifyDataSetChanged()
                } else {
                    Toast.makeText(this@ChatActivity, "No messages found.", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ChatActivity, "Failed to fetch messages.", Toast.LENGTH_SHORT).show()
            }
        })
    }



//    private fun sendMessage() {
//        val messageText = binding.etMessage.text.toString().trim()
//
//        val manualLoginSender = SharedPreferenceManager.getUserId(this@ChatActivity)
//        val receiverId = intent.getStringExtra("receiverId").toString()
////
//        val firebaseUser = FirebaseAuth.getInstance().currentUser
//        val googleLoginSender = firebaseUser?.uid
//
//        val finalId1 = "${manualLoginSender}_${receiverId}"
//        val finalId_2 = "${receiverId}_${manualLoginSender}"
//        val finalId3 = "${googleLoginSender}_${receiverId}"
//        val finalId4 = "${receiverId}_${googleLoginSender}"
//
//        if (messageText.isNotEmpty()) {
//            // finalId ane finalId2 database ma exist kre 6 k nai e check krse.
//            database.get().addOnCompleteListener { task ->
//                if (task.isSuccessful) {
//                    val snapshot = task.result
//                    if (snapshot != null && (snapshot.child(finalId).exists() || snapshot.child(finalId2).exists())) {
//                        // Aiya e nakki thase k kai ID exist kre 6 ane j exist kre 6 e use krvani. TERNARY LOOP : if finalId exist krti hse to finalId ma push thase nakr finalId2 ma push thase.
//                        val existingNode = if (snapshot.child(finalId).exists()) finalId else finalId2
//                        val chat = Chats(senderId.toString(), receiverId, messageText)
//                        database.child(existingNode).push().setValue(chat)
//                            .addOnSuccessListener {
//                                Toast.makeText(this, "Message saved successfully!", Toast.LENGTH_SHORT).show()
//                                binding.etMessage.text?.clear()
//                            }
//                            .addOnFailureListener { error ->
//                                Toast.makeText(this, "Failed to save message.", Toast.LENGTH_SHORT).show()
//                            }
//                    } else {
//                        // Neither finalId nor finalId2 exists, create a new node using finalId
//                        val chat = Chats(senderId.toString(), receiverId, messageText)
//                        database.child(finalId).push().setValue(chat)
//                            .addOnSuccessListener {
//                                Toast.makeText(this, "Message saved successfully!", Toast.LENGTH_SHORT).show()
//                                binding.etMessage.text?.clear()
//                            }
//                            .addOnFailureListener { error ->
//                                Toast.makeText(this, "Failed to save message.", Toast.LENGTH_SHORT).show()
//                            }
//                    }
//                } else {
//                    Toast.makeText(this, "Failed to send message.", Toast.LENGTH_SHORT).show()
//                }
//            }
//        } else {
//            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun sendMessage() {
        // Get the message text from the input field
        val messageText = binding.etMessage.text.toString().trim()

        // Get the sender and receiver IDs
        val manualLoginSender = SharedPreferenceManager.getUserId(this@ChatActivity)
        val receiverId = intent.getStringExtra("receiverId").toString()
        val firebaseUser = FirebaseAuth.getInstance().currentUser
        val googleLoginSender = firebaseUser?.uid

        // Determine which sender ID to use
        val senderId = if (!manualLoginSender.isNullOrEmpty()) {
            manualLoginSender // Use manual login sender ID if available
        } else if (!googleLoginSender.isNullOrEmpty()) {
            googleLoginSender // Use Google login sender ID if available
        } else {
            null // No valid sender ID found
        }

        // Check if sender and receiver IDs are valid
        if (senderId.isNullOrEmpty() || receiverId.isNullOrEmpty()) {
            Toast.makeText(this, "Sender or Receiver ID is missing!", Toast.LENGTH_SHORT).show()
            return
        }

        // Check if the message is not empty
        if (messageText.isEmpty()) {
            Toast.makeText(this, "Please enter a message", Toast.LENGTH_SHORT).show()
            return
        }

        // Define the possible node IDs
        val finalId1 = "${manualLoginSender}_${receiverId}"
        val finalId2 = "${receiverId}_${manualLoginSender}"
        val finalId3 = "${googleLoginSender}_${receiverId}"
        val finalId4 = "${receiverId}_${googleLoginSender}"

        // Access the database
        database.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val snapshot = task.result

                if (snapshot != null) {
                    // Check if manual login IDs exist
                    val manualNodeExists = snapshot.child(finalId1).exists() || snapshot.child(finalId2).exists()

                    // Check if Google login IDs exist
                    val googleNodeExists = snapshot.child(finalId3).exists() || snapshot.child(finalId4).exists()

                    val targetNode = when {
                        manualNodeExists -> if (snapshot.child(finalId1).exists()) finalId1 else finalId2
                        googleNodeExists -> if (snapshot.child(finalId3).exists()) finalId3 else finalId4
                        else -> null
                    }

                    if (targetNode != null) {
                        // Use the existing node to push the message
                        val chat = Chats(senderId, receiverId, messageText)
                        database.child(targetNode).push().setValue(chat)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Message saved successfully!", Toast.LENGTH_SHORT).show()
                                binding.etMessage.text?.clear()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to save message.", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // No node exists, create a new one (prefer manual login node)
                        val newNode = if (manualLoginSender!!.isNotEmpty()) finalId1 else finalId3
                        val chat = Chats(senderId, receiverId, messageText)
                        database.child(newNode).push().setValue(chat)
                            .addOnSuccessListener {
                                Toast.makeText(this, "New node created and message saved!", Toast.LENGTH_SHORT).show()
                                binding.etMessage.text?.clear()
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to create node and save message.", Toast.LENGTH_SHORT).show()
                            }
                    }
                } else {
                    Toast.makeText(this, "Failed to retrieve data.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Error accessing database.", Toast.LENGTH_SHORT).show()
            }
        }
    }





    override fun onBackPressed() {
        super.onBackPressed()
    }

}
