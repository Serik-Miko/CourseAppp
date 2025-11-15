package com.example.courseapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.courseapp.databinding.ItemUserBinding
import com.bumptech.glide.Glide
import android.util.Log

class UserAdapter(
    private val users: List<User>,
    private val onUserClick: (User) -> Unit
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    inner class UserViewHolder(private val binding: ItemUserBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(user: User) {
            with(binding) {
                // Қолданушы ақпаратын орнату
                tvUsername.text = user.username
                tvEmail.text = user.email
                tvRegistrationDate.text = "Тіркелген: ${user.registrationDate}"
                tvPurchasedCourses.text = "${user.purchasedCourses} курс"

                // ✅ ПРОФИЛЬ СУРЕТІН КӨРСЕТУ (ТЕКСЕРІЛГЕН)
                if (!user.profileImage.isNullOrEmpty()) {
                    Log.d("UserAdapter", "📸 Сурет көрсетілуде: ${user.username} - ${user.profileImage}")
                    Glide.with(root.context)
                        .load(user.profileImage)
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .circleCrop()
                        .into(ivProfileImage)
                } else {
                    Log.d("UserAdapter", "📸 Сурет жоқ: ${user.username}")
                    ivProfileImage.setImageResource(R.drawable.ic_launcher_foreground)
                }

                // Рөл бойынша түс
                when (user.role) {
                    "admin" -> {
                        tvRole.text = "🏷️ Админ"
                        tvRole.setTextColor(binding.root.context.getColor(android.R.color.holo_red_light))
                    }
                    else -> {
                        tvRole.text = "🏷️ Қолданушы"
                        tvRole.setTextColor(binding.root.context.getColor(android.R.color.holo_green_light))
                    }
                }

                // Статус бойынша түс
                when (user.status) {
                    "active" -> {
                        tvStatus.text = "✅ Белсенді"
                        tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_green_light))
                    }
                    "blocked" -> {
                        tvStatus.text = "🚫 Бұғатталған"
                        tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_red_light))
                    }
                    else -> {
                        tvStatus.text = "✅ Белсенді"
                        tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_green_light))
                    }
                }

                // Бүкіл карточкаға клик
                root.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(root.context, R.anim.card_click)
                    root.startAnimation(animation)
                    onUserClick(user)
                }

                // Әрекеттер батырмасы
                btnActions.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(root.context, R.anim.button_click)
                    it.startAnimation(animation)
                    onUserClick(user)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(users[position])

        // Кірісу анимациясы
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_animation)
        holder.itemView.startAnimation(animation)
    }

    override fun getItemCount(): Int = users.size
}