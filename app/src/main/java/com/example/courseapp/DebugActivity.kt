package com.example.courseapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class DebugActivity : AppCompatActivity() {
    private lateinit var db: DatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        db = DatabaseHelper(this)
        checkDatabase()
    }

    private fun checkDatabase() {
        Thread {
            try {
                // Курс алу
                val courses = db.getAllCourses()
                val courseIds = db.getAllCourseIds()

                runOnUiThread {
                    // Logcat-та көру
                    println("=== DEBUG DATABASE INFO ===")
                    println("DEBUG: Барлық курс саны: ${courses.size}")
                    println("DEBUG: Барлық курс ID: $courseIds")

                    courses.forEach { course ->
                        println("DEBUG: Курс - ID: ${course.id}, Атауы: ${course.title}, Бағасы: ${course.price} ₸")
                    }

                    // Пайдаланушыларды тексеру
                    val users = db.getAllUsers()
                    println("DEBUG: Барлық қолданушы саны: ${users.size}")
                    users.forEach { user ->
                        println("DEBUG: Қолданушы - ID: ${user.id}, Аты: ${user.username}, Рөлі: ${user.role}")
                    }

                    // Toast көрсету
                    Toast.makeText(this@DebugActivity,
                        "Курс саны: ${courses.size}\nҚолданушы саны: ${users.size}",
                        Toast.LENGTH_LONG
                    ).show()

                    // Активти аяқтау
                    finish()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    Toast.makeText(this@DebugActivity,
                        "Қате: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }.start()
    }
}