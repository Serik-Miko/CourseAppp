package com.example.courseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courseapp.databinding.ActivityOrderSuccessBinding

class OrderSuccessActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderSuccessBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderSuccessBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnViewOrders.setOnClickListener {
            val intent = Intent(this, OrderHistoryActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnGoHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        binding.btnShare.setOnClickListener {
            shareOrder()
        }
    }

    private fun shareOrder() {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, "Мен CourseApp-тен курс сатып алдым! 🎓")
        }
        startActivity(Intent.createChooser(shareIntent, "Бөлісу"))
        Toast.makeText(this, "Бөлісу...", Toast.LENGTH_SHORT).show()
    }
}