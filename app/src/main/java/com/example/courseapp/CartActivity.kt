package com.example.courseapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.courseapp.databinding.ActivityCartBinding

class CartActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCartBinding
    private lateinit var db: DatabaseHelper
    private lateinit var cartAdapter: CartAdapter
    private var cartItems = mutableListOf<CartItem>()
    private var currentUserId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCartBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentUserId = intent.getIntExtra("user_id", 1)
        db = DatabaseHelper(this)
        setupRecyclerView()
/*
        val course = intent.getParcelableExtra<Course>("course")
        if (course != null) {
            addCourseToCart(course)
        } else {
            loadCartItems()
        }
*/
        binding.btnCheckout.setOnClickListener {
            checkout()
        }

        binding.btnContinueShopping.setOnClickListener {
            continueShopping()
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun continueShopping() {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("user_id", currentUserId)
        startActivity(intent)
    }

    private fun addCourseToCart(course: Course) {
        Thread {
            try {
                if (db.isCoursePurchased(currentUserId, course.id)) {
                    runOnUiThread {
                        Toast.makeText(this, "Сіз бұл курсты сатып алғансыз!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    return@Thread
                }

                val success = db.addToCart(currentUserId, course.id)
                runOnUiThread {
                    if (success) {
                        Toast.makeText(this, "${course.title} себетке қосылды", Toast.LENGTH_SHORT).show()
                        loadCartItems()
                    } else {
                        Toast.makeText(this, "Курс себетте бар", Toast.LENGTH_SHORT).show()
                        loadCartItems()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun loadCartItems() {
        Thread {
            try {
                val items = db.getCartItems(currentUserId)
                cartItems.clear()
                cartItems.addAll(items)

                runOnUiThread {
                    cartAdapter.notifyDataSetChanged()
                    updateTotalPrice()

                    if (items.isEmpty()) {
                        binding.tvCartEmpty.visibility = android.view.View.VISIBLE
                        binding.cartRecyclerView.visibility = android.view.View.GONE
                        binding.btnCheckout.isEnabled = false
                        binding.tvTotalPrice.visibility = android.view.View.GONE
                    } else {
                        binding.tvCartEmpty.visibility = android.view.View.GONE
                        binding.cartRecyclerView.visibility = android.view.View.VISIBLE
                        binding.btnCheckout.isEnabled = true
                        binding.tvTotalPrice.visibility = android.view.View.VISIBLE
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(cartItems) { cartItem ->
            showCartItemActions(cartItem)
        }

        binding.cartRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@CartActivity)
            adapter = cartAdapter
        }
    }

    private fun updateTotalPrice() {
        val total = cartItems.sumOf { it.course.price * it.quantity }
        binding.tvTotalPrice.text = "Жалпы баға: $total ₸"
    }

    private fun showCartItemActions(cartItem: CartItem) {
        AlertDialog.Builder(this)
            .setTitle("${cartItem.course.title} курсы")
            .setItems(arrayOf("Өшіру", "Бас тарту")) { dialog, which ->
                if (which == 0) removeFromCart(cartItem)
            }
            .setNegativeButton("Бас тарту", null)
            .show()
    }

    private fun removeFromCart(cartItem: CartItem) {
        Thread {
            try {
                val success = db.removeFromCart(cartItem.id)
                runOnUiThread {
                    if (success) {
                        loadCartItems()
                        Toast.makeText(this, "${cartItem.course.title} себеттен өшірілді", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Өшіру қатесі", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun checkout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Себет бос!", Toast.LENGTH_SHORT).show()
            return
        }

        // ✅ CHECKOUTACTIVITY-ГЕ ӨТУ
        val intent = Intent(this, CheckoutActivity::class.java)
        intent.putExtra("user_id", currentUserId)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        loadCartItems()
    }
}