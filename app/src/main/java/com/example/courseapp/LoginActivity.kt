package com.example.courseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courseapp.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)

        binding.btnLogin.setOnClickListener {
            attemptLogin()
        }

        binding.tvGoRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun attemptLogin() {
        val loginInput = binding.etLogin.text.toString().trim()
        val password = binding.etPassword.text.toString().trim()

        if (loginInput.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Барлық өрістерді толтырыңыз!", Toast.LENGTH_SHORT).show()
            return
        }

        // ProgressBar жоқ болғандықтан, тек батырманы өшіреміз
        binding.btnLogin.isEnabled = false
        binding.btnLogin.text = "Жүктелуде..."

        Thread {
            try {
                // Алдымен қолданушы статусын тексеру
                val userStatus = db.getUserStatusByIdentifier(loginInput)

                if (userStatus == "blocked") {
                    runOnUiThread {
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = "Кіру"
                        Toast.makeText(this, "Есептік жазбаңыз бұғатталған! Админге хабарласыңыз.", Toast.LENGTH_LONG).show()
                    }
                    return@Thread
                }

                // Админ логині
                if (loginInput == "admin" && password == "12345admin") {
                    runOnUiThread {
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = "Кіру"
                        Toast.makeText(this, "Админ ретінде кірдіңіз!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this, AdminActivity::class.java)
                        intent.putExtra("user_id", 0)
                        intent.putExtra("user_role", "admin")
                        startActivity(intent)
                        finish()
                    }
                    return@Thread
                }

                // Қалыпты қолданушы тексеру
                if (db.isUserValid(loginInput, password)) {
                    val userRole = db.getUserRoleByIdentifier(loginInput)
                    val userEmail = db.getUserEmail(loginInput) ?: loginInput
                    val userId = getUserIdByIdentifier(loginInput)

                    runOnUiThread {
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = "Кіру"
                        Toast.makeText(this, "$userRole ретінде кірдіңіз!", Toast.LENGTH_SHORT).show()

                        val intent = if (userRole == "admin") {
                            Intent(this, AdminActivity::class.java)
                        } else {
                            Intent(this, MainActivity::class.java)
                        }

                        intent.putExtra("user_email", userEmail)
                        intent.putExtra("user_role", userRole)
                        intent.putExtra("user_id", userId)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    runOnUiThread {
                        binding.btnLogin.isEnabled = true
                        binding.btnLogin.text = "Кіру"
                        Toast.makeText(this, "Қате логин немесе пароль!", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.btnLogin.isEnabled = true
                    binding.btnLogin.text = "Кіру"
                    Toast.makeText(this, "Жүйелік қате", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    // Қолданушы ID алу функциясы
    private fun getUserIdByIdentifier(identifier: String): Int {
        return try {
            val users = db.getAllUsers()
            val user = users.find { it.username == identifier || it.email == identifier }
            user?.id ?: 1
        } catch (e: Exception) {
            e.printStackTrace()
            1
        }
    }
}