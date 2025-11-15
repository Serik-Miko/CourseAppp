package com.example.courseapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.recyclerview.widget.RecyclerView
import com.example.courseapp.databinding.ItemOrderBinding

class OrderAdapter(
    private val orders: List<Order>,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(order: Order) {
            with(binding) {
                tvOrderId.text = "Тапсырыс №${order.id}"
                tvOrderDate.text = order.orderDate
                tvTotalAmount.text = "${order.totalAmount} ₸"

                when (order.status) {
                    "completed" -> {
                        tvStatus.text = "Аяқталды"
                        tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_green_light))
                    }
                    "pending" -> {
                        tvStatus.text = "Өңделуде"
                        tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_orange_light))
                    }
                    "cancelled" -> {
                        tvStatus.text = "Бас тартылды"
                        tvStatus.setTextColor(binding.root.context.getColor(android.R.color.holo_red_light))
                    }
                }

                root.setOnClickListener {
                    val animation = AnimationUtils.loadAnimation(root.context, R.anim.card_click)
                    root.startAnimation(animation)
                    onOrderClick(order)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
        val animation = AnimationUtils.loadAnimation(holder.itemView.context, R.anim.item_animation)
        holder.itemView.startAnimation(animation)
    }

    override fun getItemCount(): Int = orders.size
}