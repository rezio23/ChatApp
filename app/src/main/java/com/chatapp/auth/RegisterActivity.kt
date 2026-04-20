package com.chatapp.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.chatapp.MainActivity
import com.chatapp.databinding.ActivityRegisterBinding
import com.chatapp.model.User
import com.chatapp.utils.FirebaseUtils

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener { registerUser() }

        binding.tvLogin.setOnClickListener {
            finish()
        }
    }

    private fun registerUser() {
        val name = binding.etName.text.toString().trim()
        val email = binding.etEmail.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()
        val confirmPassword = binding.etConfirmPassword.text.toString().trim()

        if (name.isEmpty()) {
            binding.etName.error = "Name is required"
            binding.etName.requestFocus()
            return
        }

        if (email.isEmpty()) {
            binding.etEmail.error = "Email is required"
            binding.etEmail.requestFocus()
            return
        }

        if (password.isEmpty()) {
            binding.etPassword.error = "Password is required"
            binding.etPassword.requestFocus()
            return
        }

        if (password.length < 6) {
            binding.etPassword.error = "Minimum 6 characters required"
            binding.etPassword.requestFocus()
            return
        }

        if (password != confirmPassword) {
            binding.etConfirmPassword.error = "Passwords do not match"
            binding.etConfirmPassword.requestFocus()
            return
        }

        setLoading(true)

        FirebaseUtils.auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = FirebaseUtils.auth.currentUser!!.uid
                    val user = User(
                        uid = uid,
                        name = name,
                        email = email,
                        status = "Hey there! I am using ChatApp.",
                        lastSeen = System.currentTimeMillis()
                    )
                    FirebaseUtils.usersCollection().document(uid).set(user)
                        .addOnCompleteListener { dbTask ->
                            setLoading(false)
                            if (dbTask.isSuccessful) {
                                startActivity(Intent(this, MainActivity::class.java))
                                finishAffinity()
                            } else {
                                Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                            }
                        }
                } else {
                    setLoading(false)
                    Toast.makeText(this, task.exception?.message ?: "Registration failed", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun setLoading(loading: Boolean) {
        binding.progressBar.visibility = if (loading) View.VISIBLE else View.GONE
        binding.btnRegister.isEnabled = !loading
        binding.tvLogin.isEnabled = !loading
    }
}
