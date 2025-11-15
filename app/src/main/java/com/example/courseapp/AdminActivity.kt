package com.example.courseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courseapp.databinding.ActivityAdminBinding

class AdminActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Қолданушыларды басқару батырмасы
        binding.btnManageUsers.setOnClickListener {
            // ✅ ТҮЗЕТІЛДІ - CourseManagerActivity-ға өту
            val intent = Intent(this, UserManagerActivity::class.java)
            startActivity(intent)
        }

        // Курс қосу батырмасы
        binding.btnAddCourse.setOnClickListener {
            // ✅ ТҮЗЕТІЛДІ - AddCourseActivity-ға өту
            val intent = Intent(this, CourseManagerActivity::class.java)
            startActivity(intent)
        }

        // Шығу батырмасы
        binding.btnLogout.setOnClickListener {
            Toast.makeText(this, "Шығып кеттіңіз!", Toast.LENGTH_SHORT).show()

            // Барлық алдыңғы экрандарды өшіріп, логинге қайту
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}