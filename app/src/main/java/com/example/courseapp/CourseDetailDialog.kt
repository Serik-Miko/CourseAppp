package com.example.courseapp

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide  // ✅ GLIDE ИМПОРТЫ
import com.example.courseapp.databinding.DialogCourseDetailBinding

class CourseDetailDialog : DialogFragment() {
    private lateinit var binding: DialogCourseDetailBinding
    private var course: Course? = null
    private var userId: Int = 1

    companion object {
        private const val ARG_COURSE = "course"
        private const val ARG_USER_ID = "user_id"

        fun newInstance(course: Course, userId: Int): CourseDetailDialog {
            val args = Bundle().apply {
                putParcelable(ARG_COURSE, course)
                putInt(ARG_USER_ID, userId)
            }
            return CourseDetailDialog().apply {
                arguments = args
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        course = arguments?.getParcelable(ARG_COURSE)
        userId = arguments?.getInt(ARG_USER_ID, 1) ?: 1
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DialogCourseDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        course?.let {
            setupDialog(it)
        }

        binding.btnClose.setOnClickListener {
            dismiss()
        }
    }

    private fun setupDialog(course: Course) {
        with(binding) {
            // ✅ КУРС СУРЕТІН КӨРСЕТУ (ЖАҢА)
            if (!course.imageUrl.isNullOrEmpty()) {
                Glide.with(requireContext())
                    .load(course.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(courseImage)
            } else {
                courseImage.setImageResource(R.drawable.ic_launcher_foreground)
            }

            courseTitle.text = course.title
            courseInstructor.text = course.instructor
            courseRating.text = "⭐ ${course.rating} (${course.reviews} пікір)"
            courseDuration.text = course.duration
            coursePrice.text = "${course.price} ₸"
            courseDescription.text = course.description

            // ✅ Сатып алынған ба тексеру
            val db = DatabaseHelper(requireContext())
            val isPurchased = db.isCoursePurchased(userId, course.id)

            if (isPurchased) {
                btnBuy.text = "Сатып алынған"
                btnBuy.isEnabled = false
                btnBuy.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
            } else {
                btnBuy.text = "Сатып алу - ${course.price} ₸"
                btnBuy.isEnabled = true
            }

            btnBuy.setOnClickListener {
                buyCourse(course)
            }

            btnShare.setOnClickListener {
                shareCourse(course)
            }

            // Мүмкіндіктер тізімін толтыру
            featuresList.removeAllViews()
            course.features.forEach { feature ->
                val featureView = LayoutInflater.from(requireContext())
                    .inflate(R.layout.item_feature, featuresList, false) as android.widget.TextView
                featureView.text = "✅ $feature"
                featuresList.addView(featureView)
            }
        }
    }

    private fun buyCourse(course: Course) {
        val db = DatabaseHelper(requireContext())

        if (db.isCoursePurchased(userId, course.id)) {
            Toast.makeText(requireContext(),
                "Сіз бұл курсты сатып алғансыз!\n'${course.title}'",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        // ✅ Алдымен курс себетте бар ма тексеру
        val cartItems = db.getCartItems(userId)
        val alreadyInCart = cartItems.any { cartItem -> cartItem.course.id == course.id }

        if (alreadyInCart) {
            Toast.makeText(requireContext(),
                "Бұл курс себетте бар!\n'${course.title}'",
                Toast.LENGTH_LONG
            ).show()

            // ✅ Себетке өту (курсты қайта жібермей)
            val intent = Intent(requireContext(), CartActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
            dismiss()
            return
        }

        val success = db.addToCart(userId, course.id)
        if (success) {
            Toast.makeText(requireContext(),
                "${course.title} себетке қосылды",
                Toast.LENGTH_SHORT
            ).show()

            // ✅ Себетке өту (курсты қайта жібермей)
            val intent = Intent(requireContext(), CartActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
            dismiss()
        } else {
            Toast.makeText(requireContext(),
                "Курсты себетке қосу қатесі",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun shareCourse(course: Course) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, "Қараңыз: ${course.title} - ${course.price} ₸")
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "Курсты бөлісу"))
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
    }
}