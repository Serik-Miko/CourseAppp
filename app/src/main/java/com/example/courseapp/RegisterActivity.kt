package com.example.courseapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courseapp.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        db = DatabaseHelper(this)

        binding.btnRegister.setOnClickListener {
            val username = binding.etUsername.text.toString().trim()
            val email = binding.etEmail.text.toString().trim().lowercase()
            val password = binding.etPassword.text.toString()

            // Бос тексеру
            if (username.isEmpty()) {
                binding.etUsername.error = "Логинды енгізіңіз"
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.etEmail.error = "Email енгізіңіз"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.etPassword.error = "Пароль енгізіңіз"
                return@setOnClickListener
            }

            // Форматтық тексерістер
            if (!Validator.isValidUsername(username)) {
                binding.etUsername.error = "Логин жарамсыз (3-30, арнайы символдар жоқ)"
                return@setOnClickListener
            }
            if (!Validator.isValidUsername(username)) {
                binding.etUsername.error = "Логин жарамсыз! (3-30, бірнеше -- __ болмауы керек)"
                return@setOnClickListener
            }

            if (!Validator.isValidEmail(email)) {
                binding.etEmail.error = "Email формат дұрыс емес"
                return@setOnClickListener
            }
            if (!Validator.isValidPassword(password)) {
                binding.etPassword.error = "Пароль кемінде 5 таңба, арнайы символдар болмайды"
                return@setOnClickListener
            }

            // Бірегейлік
            if (db.isUsernameExists(username)) {
                binding.etUsername.error = "Бұл логин тіркелген"
                return@setOnClickListener
            }
            if (db.isEmailExists(email)) {
                binding.etEmail.error = "Бұл email тіркелген"
                return@setOnClickListener
            }
            if (!Validator.isValidEmail(email)) {
                binding.etEmail.error = "Email жарамсыз! (бірнеше -- __ .. болмауы керек)"
                return@setOnClickListener
            }

            val ok = db.registerUser(username, email, password)
            if (ok) {
                Toast.makeText(this, "Тіркелу сәтті өтті", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Тіркеу қатесі", Toast.LENGTH_SHORT).show()
            }
        }

        binding.tvGoLogin.setOnClickListener {
            finish()
        }
    }
}
