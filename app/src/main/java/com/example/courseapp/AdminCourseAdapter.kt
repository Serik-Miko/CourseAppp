package com.example.courseapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide  // ✅ GLIDE ИМПОРТЫ
import com.example.courseapp.databinding.ItemAdminCourseBinding

class AdminCourseAdapter(
    private val courses: List<Course>,
    private val onEditClick: (Course) -> Unit
) : RecyclerView.Adapter<AdminCourseAdapter.AdminCourseViewHolder>() {

    inner class AdminCourseViewHolder(private val binding: ItemAdminCourseBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(course: Course) {
            with(binding) {
                tvCourseTitle.text = course.title
                tvInstructor.text = course.instructor
                tvPrice.text = "${course.price} ₸"
                tvCategory.text = course.features.firstOrNull() ?: "Жалпы"

                // ✅ КУРС СУРЕТІН КӨРСЕТУ (ЖАҢА)
                if (!course.imageUrl.isNullOrEmpty()) {
                    Glide.with(itemView.context)
                        .load(course.imageUrl)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(binding.ivCourseImage)  // ✅ ImageView қосу керек
                } else {
                    binding.ivCourseImage.setImageResource(R.drawable.ic_launcher_foreground)
                }

                btnEdit.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(itemView.context, R.anim.button_click)
                    it.startAnimation(animation)
                    onEditClick(course)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AdminCourseViewHolder {
        val binding = ItemAdminCourseBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AdminCourseViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AdminCourseViewHolder, position: Int) {
        holder.bind(courses[position])
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_animation)
        holder.itemView.startAnimation(animation)
    }

    override fun getItemCount(): Int = courses.size
}