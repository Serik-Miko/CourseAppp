package com.example.courseapp

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.courseapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var currentUserId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ userId алу
        currentUserId = intent.getIntExtra("user_id", 1)
        val userRole = intent.getStringExtra("user_role") ?: "user"

        // ✅ Текстті дереу жасыру
        binding.tvContent.visibility = View.GONE

        // Алдымен шығу батырмасын жасырамыз
        binding.btnLogout.visibility = View.GONE

        // Әдепкі фрагмент
        replaceFragment(CoursesFragment())

        // Батырмалар
        binding.btnProfile.setOnClickListener {
            replaceFragment(ProfileFragment())
            binding.btnLogout.visibility = View.VISIBLE
        }


        binding.btnCourses.setOnClickListener {
            replaceFragment(CoursesFragment())
            binding.btnLogout.visibility = View.GONE
        }

        // Шығу батырмасы
        binding.btnLogout.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        // ✅ Фрагментке userId жіберу
        val bundle = Bundle()
        bundle.putInt("user_id", currentUserId)
        fragment.arguments = bundle

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}