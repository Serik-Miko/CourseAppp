package com.example.courseapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.courseapp.databinding.FragmentCoursesBinding
import android.util.Log

class CoursesFragment : Fragment() {
    private lateinit var binding: FragmentCoursesBinding
    private lateinit var courseAdapter: CourseAdapter
    private lateinit var db: DatabaseHelper
    private var currentUserId: Int = 1

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentCoursesBinding.inflate(inflater, container, false)

        // ✅ userId алу
        currentUserId = arguments?.getInt("user_id", 1) ?: 1

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        db = DatabaseHelper(requireContext())
        loadCoursesFromDatabase()
    }

    private fun loadCoursesFromDatabase() {
        Thread {
            try {
                val courses = db.getAllCourses()

                // ✅ ДЕБАГ: Курстарды тексеру
                courses.forEach { course ->
                    Log.d("CoursesFragment", "📚 Курс: ${course.title} - Сурет: ${course.imageUrl}")
                }

                // ✅ Әр курс үшін сатып алынған ба жоқты тексереміз
                val coursesWithPurchaseStatus = courses.map { course ->
                    val isPurchased = db.isCoursePurchased(currentUserId, course.id)
                    course.copy(isPurchased = isPurchased)
                }

                activity?.runOnUiThread {
                    setupRecyclerView(coursesWithPurchaseStatus)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun showProgress(show: Boolean) {
        try {
            binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        } catch (e: Exception) {
            // progressBar жоқ болса, ештеңе жасамау
        }
    }

    private fun getSampleCourses(): List<Course> {
        return listOf(
            Course(
                id = 1,
                title = "«ДЕМЕУ» Мобилография",
                instructor = "Саят Жанабай",
                rating = 4.9f,
                reviews = 148,
                duration = "2-3 апта + 1 ай менторлық",
                price = 25000,
                imageRes = R.drawable.ic_launcher_foreground,
                description = "Мобилография, контент жасау және SMM бойынша толық курс.",
                features = listOf("80% практика", "Сертификат", "Мәңгілік доступ", "Бітіру кеші")
            ),
            Course(
                id = 2,
                title = "Android Әзірлеу",
                instructor = "IT маман",
                rating = 4.7f,
                reviews = 89,
                duration = "36 сабақ",
                price = 35000,
                imageRes = R.drawable.ic_launcher_foreground,
                description = "Android қосымшаларын нөлден бастап әзірлеу.",
                features = listOf("Kotlin", "Firebase", "10 проект")
            )
        )
    }

    private fun setupRecyclerView(courses: List<Course>) {
        // ✅ ДЕБАГ: Курстарды тексеру
        courses.forEach { course ->
            Log.d("CoursesFragment", "📚 RecyclerView курс: ${course.title} - Сурет: ${course.imageUrl}")
        }

        courseAdapter = CourseAdapter(courses) { course ->
            // ✅ Курс деталь диалогқа userId жіберу
            val dialog = CourseDetailDialog.newInstance(course, currentUserId)
            dialog.show(parentFragmentManager, "course_detail")
        }

        binding.coursesRecyclerView.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = courseAdapter
        }

        Log.d("CoursesFragment", "📚 RecyclerView орнатылды, курс саны: ${courses.size}")
    }

    override fun onResume() {
        super.onResume()
        loadCoursesFromDatabase()
    }
}