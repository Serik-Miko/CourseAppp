package com.example.courseapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courseapp.databinding.ActivityEditProfileBinding

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private lateinit var db: DatabaseHelper
    private var currentUser: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        currentUser = intent.getStringExtra("user")

        if (currentUser.isNullOrEmpty()) {
            Toast.makeText(this, "Қолданушы табылмады!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // ✅ Ағымдағы деректерді жүктеу
        loadUserInfo()

        binding.btnSave.setOnClickListener {
            val newUsername = binding.etUsername.text.toString().trim()
            val newEmail = binding.etEmail.text.toString().trim().lowercase()
            val oldPass = binding.etOldPassword.text.toString().trim()
            val newPass = binding.etNewPassword.text.toString().trim()

            if (oldPass.isEmpty()) {
                binding.etOldPassword.error = "Ескі парольді енгізіңіз!"
                return@setOnClickListener
            }

            Thread {
                val userValid = db.isUserValid(currentUser!!, oldPass)
                if (!userValid) {
                    runOnUiThread {
                        Toast.makeText(this, "Ескі пароль қате!", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                // 🔹 Жаңа мәліметтерді жаңарту
                val updated = db.updateUser(
                    oldIdentifier = currentUser!!,
                    newUsername = if (newUsername.isNotEmpty()) newUsername else null,
                    newEmail = if (newEmail.isNotEmpty()) newEmail else null,
                    newPassword = if (newPass.isNotEmpty()) newPass else null
                )

                runOnUiThread {
                    if (updated) {
                        Toast.makeText(this, "Профиль сәтті жаңарды!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this, "Жаңарту қатесі!", Toast.LENGTH_SHORT).show()
                    }
                }
            }.start()
        }
    }

    private fun loadUserInfo() {
        Thread {
            val dbReadable = db.readableDatabase
            val cursor = dbReadable.rawQuery(
                "SELECT username, email FROM users WHERE username=? OR email=?",
                arrayOf(currentUser, currentUser)
            )
            if (cursor.moveToFirst()) {
                val username = cursor.getString(0)
                val email = cursor.getString(1)
                runOnUiThread {
                    binding.etUsername.setText(username)
                    binding.etEmail.setText(email)
                }
            }
            cursor.close()
            dbReadable.close()
        }.start()
    }
}
