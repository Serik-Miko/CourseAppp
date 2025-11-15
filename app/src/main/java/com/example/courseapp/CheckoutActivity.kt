package com.example.courseapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.courseapp.databinding.ActivityCheckoutBinding

class CheckoutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCheckoutBinding
    private lateinit var db: DatabaseHelper
    private var currentUserId: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCheckoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ USER_ID АЛУ
        currentUserId = intent.getIntExtra("user_id", 1)

        db = DatabaseHelper(this)
        setupCardInputs() // ✅ Карта форматтауын қосу
        setupPaymentMethods()
        updateOrderSummary()

        binding.btnPayNow.setOnClickListener {
            processPayment()
        }

        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun setupCardInputs() {
        // Карта нөміріне форматтау
        binding.etCardNumber.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    var input = s.toString().replace(" ", "")

                    // Тек сандарға рұқсат ету
                    input = input.filter { char -> char.isDigit() }

                    // Максималды ұзындық 16 символ (пробелдерсіз)
                    if (input.length > 16) {
                        input = input.substring(0, 16)
                    }

                    // Форматтау: 0000 0000 0000 0000
                    val formatted = StringBuilder()
                    for (i in input.indices) {
                        if (i > 0 && i % 4 == 0) {
                            formatted.append(" ")
                        }
                        formatted.append(input[i])
                    }

                    current = formatted.toString()
                    binding.etCardNumber.removeTextChangedListener(this)
                    binding.etCardNumber.setText(current)
                    binding.etCardNumber.setSelection(current.length)
                    binding.etCardNumber.addTextChangedListener(this)
                }
            }
        })

        // Мерзімге форматтау
        binding.etExpiryDate.addTextChangedListener(object : TextWatcher {
            private var current = ""

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    var input = s.toString().replace("/", "")

                    // Тек сандарға рұқсат ету
                    input = input.filter { char -> char.isDigit() }

                    // Максималды ұзындық 4 символ (пробелдерсіз)
                    if (input.length > 4) {
                        input = input.substring(0, 4)
                    }

                    // Форматтау: АА/ЖЖ
                    if (input.length >= 2) {
                        input = input.substring(0, 2) + "/" + input.substring(2)
                    }

                    current = input
                    binding.etExpiryDate.removeTextChangedListener(this)
                    binding.etExpiryDate.setText(input)
                    binding.etExpiryDate.setSelection(input.length)
                    binding.etExpiryDate.addTextChangedListener(this)
                }
            }
        })

        // CVV үшін тек сандарға рұқсат ету
        binding.etCVV.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                val input = s.toString().filter { char -> char.isDigit() }

                // Максималды ұзындық 3 символ
                val limitedInput = if (input.length > 3) input.substring(0, 3) else input

                if (s.toString() != limitedInput) {
                    binding.etCVV.removeTextChangedListener(this)
                    binding.etCVV.setText(limitedInput)
                    binding.etCVV.setSelection(limitedInput.length)
                    binding.etCVV.addTextChangedListener(this)
                }
            }
        })
    }

    private fun setupPaymentMethods() {
        binding.radioPaymentMethod.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.radioCard -> binding.layoutCardInfo.visibility = View.VISIBLE
                else -> binding.layoutCardInfo.visibility = View.GONE
            }
        }

        // Әдепкі төлем әдісі
        binding.radioCard.isChecked = true
    }

    private fun updateOrderSummary() {
        Thread {
            try {
                val cartItems = db.getCartItems(currentUserId)
                val totalCourses = cartItems.size
                val totalAmount = cartItems.sumOf { cartItem -> cartItem.course.price * cartItem.quantity }

                runOnUiThread {
                    binding.tvOrderSummary.text = """
                        Курс саны: $totalCourses
                        Жалпы баға: $totalAmount ₸
                    """.trimIndent()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    binding.tvOrderSummary.text = "Деректерді жүктеу қатесі"
                }
            }
        }.start()
    }

    private fun processPayment() {
        val selectedMethod = when (binding.radioPaymentMethod.checkedRadioButtonId) {
            R.id.radioCard -> "Банк картасы"
            R.id.radioKaspi -> "Kaspi QR"
            else -> ""
        }

        if (selectedMethod.isEmpty()) {
            Toast.makeText(this, "Төлем әдісін таңдаңыз!", Toast.LENGTH_SHORT).show()
            return
        }

        if (selectedMethod == "Банк картасы") {
            val cardNumber = binding.etCardNumber.text.toString().trim()
            val expiryDate = binding.etExpiryDate.text.toString().trim()
            val cvv = binding.etCVV.text.toString().trim()

            val paymentManager = PaymentManager(this)
            val validationResult = paymentManager.validateCard(cardNumber, expiryDate, cvv)

            if (!validationResult.success) {
                Toast.makeText(this, validationResult.message, Toast.LENGTH_SHORT).show()
                return
            }
        }

        showProgress(true)

        Thread {
            try {
                val cartItems = db.getCartItems(currentUserId)

                // DEBUG: Себеттегі курс ақпаратын көрсету
                println("DEBUG: Себеттегі курс саны: ${cartItems.size}")
                cartItems.forEach { cartItem ->
                    println("DEBUG: Курс - ID: ${cartItem.course.id}, Атауы: ${cartItem.course.title}")
                }

                if (cartItems.isEmpty()) {
                    runOnUiThread {
                        showProgress(false)
                        Toast.makeText(this@CheckoutActivity, "Себет бос!", Toast.LENGTH_SHORT).show()
                    }
                    return@Thread
                }

                var successfulPurchases = 0
                var failedPurchases = 0
                val errorMessages = mutableListOf<String>()

                // Әр курс сатып алу
                for (cartItem in cartItems) {
                    try {
                        // DEBUG: Курс бар ма тексеру
                        val courseExists = db.checkCourseExists(cartItem.course.id)
                        println("DEBUG: Курс ${cartItem.course.id} бар ма? $courseExists")

                        // Алдымен курс сатып алынған ба тексеру
                        val alreadyPurchased = db.isCoursePurchased(currentUserId, cartItem.course.id)
                        println("DEBUG: Курс ${cartItem.course.id} сатып алынған ба? $alreadyPurchased")

                        if (alreadyPurchased) {
                            errorMessages.add("'${cartItem.course.title}' бұрыннан сатып алынған")
                            failedPurchases++
                            continue
                        }

                        if (!courseExists) {
                            errorMessages.add("'${cartItem.course.title}' курс жоқ")
                            failedPurchases++
                            continue
                        }

                        // Курсты сатып алу
                        val success = db.purchaseCourse(currentUserId, cartItem.course.id)
                        println("DEBUG: Курс ${cartItem.course.id} сатып алу нәтижесі: $success")

                        if (success) {
                            successfulPurchases++
                        } else {
                            errorMessages.add("'${cartItem.course.title}' сақтау қатесі")
                            failedPurchases++
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorMessages.add("'${cartItem.course.title}' жүйелік қате: ${e.message}")
                        failedPurchases++
                    }
                }

                runOnUiThread {
                    showProgress(false)

                    if (successfulPurchases > 0) {
                        val message = StringBuilder()
                        message.append("✅ $successfulPurchases курс сәтті сатып алынды!")

                        if (errorMessages.isNotEmpty()) {
                            message.append("\n\n⚠️ Проблемалар:\n")
                            message.append(errorMessages.joinToString("\n"))
                        }

                        Toast.makeText(this@CheckoutActivity, message.toString(), Toast.LENGTH_LONG).show()

                        // Себетті тазалау
                        clearCart()

                        // Басты бетке өту
                        val intent = Intent(this@CheckoutActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        val message = StringBuilder()
                        message.append("❌ Ешқандай курс сатып алынбады!")

                        if (errorMessages.isNotEmpty()) {
                            message.append("\n\nСебептер:\n")
                            message.append(errorMessages.joinToString("\n"))
                        }

                        Toast.makeText(this@CheckoutActivity, message.toString(), Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                runOnUiThread {
                    showProgress(false)
                    Toast.makeText(this@CheckoutActivity, "🔴 Жүйелік қате: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun getTotalAmount(): Int {
        val cartItems = db.getCartItems(currentUserId)
        return cartItems.sumOf { cartItem -> cartItem.course.price * cartItem.quantity }
    }

    private fun showProgress(show: Boolean) {
        try {
            if (show) {
                binding.progressBar.visibility = View.VISIBLE
                binding.btnPayNow.isEnabled = false
                binding.btnPayNow.text = "Төлем өңделуде..."
            } else {
                binding.progressBar.visibility = View.GONE
                binding.btnPayNow.isEnabled = true
                binding.btnPayNow.text = "Қазір төлеу"
            }
        } catch (e: Exception) {
            if (show) {
                binding.btnPayNow.isEnabled = false
                binding.btnPayNow.text = "Төлем өңделуде..."
            } else {
                binding.btnPayNow.isEnabled = true
                binding.btnPayNow.text = "Қазір төлеу"
            }
        }
    }

    private fun clearCart() {
        Thread {
            try {
                db.clearCart(currentUserId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }
}