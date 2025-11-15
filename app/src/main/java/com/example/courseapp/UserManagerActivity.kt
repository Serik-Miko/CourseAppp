package com.example.courseapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courseapp.databinding.ActivityUserManagerBinding
import android.util.Log

class UserManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityUserManagerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var userAdapter: UserAdapter
    private var allUsers: List<User> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        setupEmptyView()
        setupRecyclerView()
        setupSearchFunctionality() // Іздеу функциясын қосу
        loadUserStats()

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupSearchFunctionality() {
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Мәтін өзгерген сайын іздеуді орындау
                searchUsers(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun searchUsers(query: String) {
        Thread {
            try {
                val filteredUsers = if (query.isBlank()) {
                    allUsers // Егер сұраныс бос болса, барлық қолданушыларды көрсету
                } else {
                    db.searchUsers(query) // Дерекқордан іздеу
                }

                runOnUiThread {
                    updateUsersList(filteredUsers)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this, "Іздеу қатесі", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun setupRecyclerView() {
        Thread {
            try {
                val allUsers = db.getAllUsers() // Барлық қолданушыларды сақтау

                // ✅ ДЕБАГ: Қолданушылардың суреттерін тексеру
                allUsers.forEach { user ->
                    Log.d("UserManager", "👤 ${user.username} - Сурет: ${user.profileImage}")
                }

                runOnUiThread {
                    updateUsersList(allUsers)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.tvEmptyUsers.visibility = View.VISIBLE
                    binding.tvEmptyUsers.text = "Деректерді жүктеу қатесі"
                }
            }
        }.start()
    }

    private fun updateUsersList(users: List<User>) {
        if (users.isNotEmpty()) {
            userAdapter = UserAdapter(users) { user ->
                showUserActions(user)
            }
            binding.usersRecyclerView.apply {
                layoutManager = LinearLayoutManager(this@UserManagerActivity)
                adapter = userAdapter
            }
            binding.tvEmptyUsers.visibility = View.GONE
        } else {
            binding.tvEmptyUsers.visibility = View.VISIBLE
            binding.tvEmptyUsers.text = "Қолданушылар табылмады"
        }
    }

    private fun setupEmptyView() {
        try {
            binding.tvEmptyUsers.visibility = View.GONE
        } catch (e: Exception) {
            // Егер tvEmptyUsers жоқ болса, ештеңе жасамау
        }
    }

    private fun loadUserStats() {
        Thread {
            try {
                val users = db.getAllUsers()
                runOnUiThread {
                    val totalUsers = users.size
                    val activeUsers = users.count { it.status == "active" }
                    val blockedUsers = users.count { it.status == "blocked" }
                    val adminUsers = users.count { it.role == "admin" }

                    binding.tvTotalUsers.text = "Барлығы: $totalUsers"
                    binding.tvActiveUsers.text = "Белсенді: $activeUsers"
                    binding.tvNewUsers.text = "Бұғатталған: $blockedUsers"
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.tvTotalUsers.text = "Барлығы: 0"
                    binding.tvActiveUsers.text = "Белсенді: 0"
                    binding.tvNewUsers.text = "Бұғатталған: 0"
                }
            }
        }.start()
    }

    private fun showUserActions(user: User) {
        val actions = if (user.status == "active") {
            arrayOf("Рөлін өзгерту", "Бұғаттау", "Мәліметтерін қарау", "Бас тарту")
        } else {
            arrayOf("Рөлін өзгерту", "Бұғатты ашу", "Мәліметтерін қарау", "Бас тарту")
        }

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("${user.username} әрекеттері")
            .setItems(actions) { dialog, which ->
                when (which) {
                    0 -> changeUserRole(user)
                    1 -> toggleUserStatus(user)
                    2 -> showUserDetails(user)
                    // 3 - Бас тарту
                }
            }
            .setNegativeButton("Бас тарту", null)
            .show()
    }

    private fun changeUserRole(user: User) {
        val newRole = if (user.role == "admin") "user" else "admin"

        Thread {
            val success = db.updateUserRole(user.id, newRole)
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "${user.username} рөлі ${newRole} болып өзгертілді", Toast.LENGTH_SHORT).show()
                    // Тізімді жаңарту
                    setupRecyclerView()
                    loadUserStats()
                } else {
                    Toast.makeText(this, "Рөлді өзгерту қатесі", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun toggleUserStatus(user: User) {
        val newStatus = if (user.status == "active") "blocked" else "active"
        val statusText = if (newStatus == "blocked") "бұғатталды" else "белсенді"

        Thread {
            val success = db.updateUserStatus(user.id, newStatus)
            runOnUiThread {
                if (success) {
                    Toast.makeText(this, "${user.username} ${statusText} болды", Toast.LENGTH_SHORT).show()
                    // Тізімді жаңарту
                    setupRecyclerView()
                    loadUserStats()
                } else {
                    Toast.makeText(this, "Статусты өзгерту қатесі", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showUserDetails(user: User) {
        val purchasedCourses = db.getUserPurchasedCourses(user.id)
        val totalSpent = purchasedCourses.sumOf { it.price }
        val statusText = if (user.status == "active") "✅ Белсенді" else "🚫 Бұғатталған"

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Қолданушы мәліметтері")
            .setMessage(
                "Аты: ${user.username}\n" +
                        "Email: ${user.email}\n" +
                        "Рөлі: ${user.role}\n" +
                        "Статусы: $statusText\n" +
                        "Тіркелген: ${user.registrationDate}\n" +
                        "Сатып алған курс саны: ${user.purchasedCourses}\n" +
                        "Жалпы жұмсаған сомасы: $totalSpent ₸\n" +
                        "Сатып алған курс:\n" + purchasedCourses.joinToString("\n") { "• ${it.title} - ${it.price} ₸" }
            )
            .setPositiveButton("OK", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
        loadUserStats()
    }
}