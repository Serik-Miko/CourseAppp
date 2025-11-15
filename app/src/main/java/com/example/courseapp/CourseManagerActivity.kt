package com.example.courseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courseapp.databinding.ActivityCourseManagerBinding
import android.util.Log

class CourseManagerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCourseManagerBinding
    private lateinit var db: DatabaseHelper
    private lateinit var courseAdapter: AdminCourseAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCourseManagerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        setupRecyclerView()

        binding.btnAddCourse.setOnClickListener {
            // Жаңа курс қосу
            val intent = Intent(this, AddCourseActivity::class.java)
            startActivity(intent)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        Thread {
            val courses = db.getAllCourses()
            runOnUiThread {
                courseAdapter = AdminCourseAdapter(courses) { course ->
                    showCourseActions(course)
                }

                binding.coursesRecyclerView.apply {
                    layoutManager = LinearLayoutManager(this@CourseManagerActivity)
                    adapter = courseAdapter
                }

                if (courses.isEmpty()) {
                    Toast.makeText(this, "Курстар жоқ", Toast.LENGTH_SHORT).show()
                }
            }
        }.start()
    }

    private fun showCourseActions(course: Course) {
        val actions = arrayOf("Өзгерту", "Өшіру", "Бас тарту")

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(course.title)
            .setItems(actions) { dialog, which ->
                when (which) {
                    0 -> editCourse(course)
                    1 -> deleteCourse(course)
                }
            }
            .setNegativeButton("Бас тарту", null)
            .show()
    }

    private fun editCourse(course: Course) {
        val intent = Intent(this, AddCourseActivity::class.java)
        intent.putExtra("EDIT_MODE", true)
        intent.putExtra("COURSE_ID", course.id)
        intent.putExtra("COURSE_TITLE", course.title)
        intent.putExtra("COURSE_INSTRUCTOR", course.instructor)
        intent.putExtra("COURSE_PRICE", course.price)
        intent.putExtra("COURSE_DURATION", course.duration)
        intent.putExtra("COURSE_DESCRIPTION", course.description)
        intent.putExtra("COURSE_FEATURES", course.features.joinToString("\n"))
        intent.putExtra("COURSE_IMAGE_URL", course.imageUrl) // ✅ СУРЕТ URL ЖІБЕРУ
        Log.d("CourseManager", "📸 Курс өзгертуге жіберілуде: ${course.title} - ${course.imageUrl}")
        startActivity(intent)
    }

    private fun deleteCourse(course: Course) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Өшіру")
            .setMessage("${course.title} курсын өшіруге сенімдісіз бе?")
            .setPositiveButton("Ия") { dialog, which ->
                Thread {
                    val success = db.deleteCourse(course.id)
                    runOnUiThread {
                        if (success) {
                            Toast.makeText(this, "Курс өшірілді", Toast.LENGTH_SHORT).show()
                            setupRecyclerView() // Тізімді жаңарту
                        } else {
                            Toast.makeText(this, "Өшіру қатесі", Toast.LENGTH_SHORT).show()
                        }
                    }
                }.start()
            }
            .setNegativeButton("Жоқ", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        setupRecyclerView()
    }
}