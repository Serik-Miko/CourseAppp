package com.example.courseapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courseapp.databinding.ActivityOrderHistoryBinding

class OrderHistoryActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOrderHistoryBinding
    private lateinit var db: DatabaseHelper
    private lateinit var orderAdapter: OrderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderHistoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DatabaseHelper(this)
        setupRecyclerView()

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        val orders = getOrdersFromDatabase()
        orderAdapter = OrderAdapter(orders) { order ->
            showOrderDetails(order)
        }

        binding.ordersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@OrderHistoryActivity)
            adapter = orderAdapter
        }

        if (orders.isEmpty()) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.ordersRecyclerView.visibility = View.GONE
        } else {
            binding.tvEmpty.visibility = View.GONE
            binding.ordersRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun getOrdersFromDatabase(): List<Order> {
        return listOf(
            Order(
                id = 1,
                userId = 1,
                orderDate = "2024-10-29 15:30",
                totalAmount = 25000,
                status = "completed",
                items = listOf(
                    CartItem(1, 1, Course(
                        id = 1, title = "«ДЕМЕУ» Мобилография", instructor = "Саят Жанабай",
                        rating = 4.9f, reviews = 148, duration = "2-3 апта", price = 25000,
                        imageRes = R.drawable.ic_launcher_foreground, description = "Курс", features = listOf()
                    ), 1)
                )
            ),
            Order(
                id = 2,
                userId = 1,
                orderDate = "2024-10-28 10:15",
                totalAmount = 35000,
                status = "pending",
                items = listOf(
                    CartItem(2, 1, Course(
                        id = 2, title = "Android Әзірлеу", instructor = "IT маман",
                        rating = 4.7f, reviews = 89, duration = "36 сабақ", price = 35000,
                        imageRes = R.drawable.ic_launcher_foreground, description = "Курс", features = listOf()
                    ), 1)
                )
            )
        )
    }

    private fun showOrderDetails(order: Order) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Тапсырыс №${order.id}")
            .setMessage(
                "Күні: ${order.orderDate}\n" +
                        "Жалпы сома: ${order.totalAmount} ₸\n" +
                        "Статусы: ${order.status}\n" +
                        "Курс саны: ${order.items.size}"
            )
            .setPositiveButton("OK", null)
            .show()
    }
}