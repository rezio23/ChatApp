package com.chatapp.users

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.chatapp.chat.ChatActivity
import com.chatapp.databinding.ActivityUsersBinding
import com.chatapp.model.User
import com.chatapp.utils.FirebaseUtils

class UsersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUsersBinding
    private var allUsers = mutableListOf<User>()
    private var filteredUsers = mutableListOf<User>()
    private lateinit var adapter: UsersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Select User"

        adapter = UsersAdapter(filteredUsers) { user ->
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("receiverId", user.uid)
            intent.putExtra("receiverName", user.name)
            startActivity(intent)
            finish()
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterUsers(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        loadUsers()
    }

    private fun loadUsers() {
        binding.progressBar.visibility = View.VISIBLE
        val currentUid = FirebaseUtils.currentUserId

        Log.d("UsersActivity", "Fetching users from Firestore... Current User UID: $currentUid")

        FirebaseUtils.usersCollection().get()
            .addOnSuccessListener { snapshot ->
                binding.progressBar.visibility = View.GONE
                allUsers.clear()

                if (snapshot.isEmpty) {
                    Log.w("UsersActivity", "Firestore 'users' collection is EMPTY!")
                }

                for (doc in snapshot.documents) {
                    val user = doc.toObject(User::class.java)
                    if (user != null) {
                        // We hide the current user so you don't chat with yourself
                        if (user.uid != currentUid) {
                            allUsers.add(user)
                            Log.d("UsersActivity", "Added user: ${user.name} (${user.email})")
                        } else {
                            Log.d("UsersActivity", "Skipped current user: ${user.name}")
                        }
                    } else {
                        Log.e("UsersActivity", "Failed to parse user document: ${doc.id}")
                    }
                }

                Log.d("UsersActivity", "Total users available for search: ${allUsers.size}")
                filterUsers(binding.etSearch.text.toString())
            }
            .addOnFailureListener { e ->
                binding.progressBar.visibility = View.GONE
                Log.e("UsersActivity", "Firestore Error", e)
                Toast.makeText(this, "Permission or Network Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun filterUsers(query: String) {
        val lowerCaseQuery = query.lowercase().trim()

        val newList = if (lowerCaseQuery.isEmpty()) {
            allUsers
        } else {
            allUsers.filter { user ->
                user.name.lowercase().contains(lowerCaseQuery) ||
                user.email.lowercase().contains(lowerCaseQuery)
            }
        }

        filteredUsers.clear()
        filteredUsers.addAll(newList)
        adapter.notifyDataSetChanged()

        if (filteredUsers.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.tvEmpty.text = if (allUsers.isEmpty()) {
                "No other users registered in Firestore yet.\nTry registering another account from the app."
            } else {
                "No users match '$query'"
            }
        } else {
            binding.tvEmpty.visibility = View.GONE
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
