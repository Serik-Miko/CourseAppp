package com.example.courseapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.courseapp.databinding.ItemCourseBinding
import com.bumptech.glide.Glide
import android.util.Log

class CourseAdapter(
    private val courses: List<Course>,
    private val onItemClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    inner class CourseViewHolder(private val binding: ItemCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
            with(binding) {
                // Курс ақпаратын орнату
                courseTitle.text = course.title
                courseInstructor.text = course.instructor
                courseRating.text = "⭐ ${course.rating}"
                courseReviews.text = "(${course.reviews})"
                courseDuration.text = course.duration
                coursePrice.text = "${course.price} ₸"

                // ✅ КУРС СУРЕТІН КӨРСЕТУ (ТЕКСЕРІЛГЕН)
                Log.d("CourseAdapter", "🖼️ Курс: ${course.title} - Сурет URL: ${course.imageUrl}")

                if (!course.imageUrl.isNullOrEmpty()) {
                    Log.d("CourseAdapter", "🖼️ Glide арқылы сурет жүктелуде: ${course.imageUrl}")
                    Glide.with(root.context)
                        .load(course.imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(courseImage)
                } else {
                    Log.d("CourseAdapter", "🖼️ Сурет жоқ, дефолт сурет көрсетілуде")
                    courseImage.setImageResource(course.imageRes)
                }

                // Карточкаға клик іс-әрекеті
                root.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(root.context, R.anim.card_click)
                    root.startAnimation(animation)
                    onItemClick(course)
                }

                // Сатып алу батырмасы
                btnBuy.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(root.context, R.anim.button_click)
                    it.startAnimation(animation)
                    onItemClick(course)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val binding = ItemCourseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position])

        // Кірісу анимациясы
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_animation)
        holder.itemView.startAnimation(animation)
    }

    override fun getItemCount(): Int = courses.size
}