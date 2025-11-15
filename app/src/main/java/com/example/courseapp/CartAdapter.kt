package com.example.courseapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.courseapp.databinding.ItemCartBinding

class CartAdapter(
    private val cartItems: List<CartItem>,
    private val onItemClick: (CartItem) -> Unit
) : RecyclerView.Adapter<CartAdapter.CartViewHolder>() {

    inner class CartViewHolder(private val binding: ItemCartBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(cartItem: CartItem) {
            with(binding) {
                tvCourseTitle.text = cartItem.course.title
                tvInstructor.text = cartItem.course.instructor
                tvPrice.text = "${cartItem.course.price} ₸"

                btnRemove.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(itemView.context, R.anim.button_click)
                    it.startAnimation(animation)
                    onItemClick(cartItem)
                }

                root.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(itemView.context, R.anim.card_click)
                    root.startAnimation(animation)
                    onItemClick(cartItem)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding = ItemCartBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return CartViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        holder.bind(cartItems[position])
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_animation)
        holder.itemView.startAnimation(animation)
    }

    override fun getItemCount(): Int = cartItems.size
}