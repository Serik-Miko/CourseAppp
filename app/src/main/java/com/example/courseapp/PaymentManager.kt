package com.example.courseapp

import android.content.Context
import java.text.SimpleDateFormat
import java.util.*

class PaymentManager(private val context: Context) {

    fun validateCard(cardNumber: String, expiryDate: String, cvv: String): PaymentResult {
        // Бос өрістерді тексеру
        if (cardNumber.isEmpty() || expiryDate.isEmpty() || cvv.isEmpty()) {
            return PaymentResult(false, "Барлық өрістерді толтырыңыз")
        }

        // Карта нөмірін тазарту (пробелдерді алып тастау)
        val cleanCardNumber = cardNumber.replace(" ", "")

        // Карта нөмірін тексеру
        if (cleanCardNumber.length != 16 || !cleanCardNumber.all { it.isDigit() }) {
            return PaymentResult(false, "Карта нөмірі 16 саннан тұруы керек")
        }

        // Логикаға келмейтін карта нөмірін тексеру
        if (isInvalidCardNumber(cleanCardNumber)) {
            return PaymentResult(false, "Карта нөмірі дұрыс емес")
        }

        // Мерзімді тексеру
        if (!isValidExpiryDate(expiryDate)) {
            return PaymentResult(false, "Мерзім дұрыс емес (АА/ЖЖ формат)")
        }

        // Логикаға келмейтін мерзімді тексеру
        if (isInvalidExpiryDate(expiryDate)) {
            return PaymentResult(false, "Мерзім дұрыс емес")
        }

        // CVV тексеру
        if (cvv.length != 3 || !cvv.all { it.isDigit() }) {
            return PaymentResult(false, "CVV 3 саннан тұруы керек")
        }

        // Логикаға келмейтін CVV тексеру
        if (isInvalidCVV(cvv)) {
            return PaymentResult(false, "CVV дұрыс емес")
        }

        // Карта мерзімі өткен ба тексеру
        if (isCardExpired(expiryDate)) {
            return PaymentResult(false, "Карта мерзімі өткен")
        }

        return PaymentResult(true, "Төлем сәтті")
    }

    private fun isInvalidCardNumber(cardNumber: String): Boolean {
        // Барлығы нөл немесе бірдей сандар
        return cardNumber.all { it == '0' } ||
                cardNumber.all { it == cardNumber[0] } ||
                cardNumber == "1111111111111111" ||
                cardNumber == "1234567812345678" ||
                cardNumber == "9999999999999999" ||
                cardNumber == "0000000000000001"
    }

    private fun isInvalidExpiryDate(expiryDate: String): Boolean {
        try {
            val parts = expiryDate.split("/")
            val month = parts[0].toInt()
            val year = parts[1].toInt()

            // Ай 1-12 аралығында болуы керек
            if (month < 1 || month > 12) {
                return true
            }

            // Жыл 00-99 аралығында болуы керек
            if (year < 0 || year > 99) {
                return true
            }

            // Логикаға келмейтін мерзімдер
            return expiryDate == "00/00" ||
                    expiryDate == "13/25" ||
                    expiryDate == "99/99" ||
                    month == 0 || year == 0
        } catch (e: Exception) {
            return true
        }
    }

    private fun isInvalidCVV(cvv: String): Boolean {
        // Барлығы нөл немесе бірдей сандар
        return cvv.all { it == '0' } ||
                cvv.all { it == cvv[0] } ||
                cvv == "000" ||
                cvv == "111" ||
                cvv == "999"
    }

    private fun isValidExpiryDate(expiryDate: String): Boolean {
        val pattern = Regex("""^(0[1-9]|1[0-2])/([0-9]{2})$""")
        return pattern.matches(expiryDate)
    }

    private fun isCardExpired(expiryDate: String): Boolean {
        try {
            val parts = expiryDate.split("/")
            val month = parts[0].toInt()
            val year = 2000 + parts[1].toInt()

            val currentDate = Calendar.getInstance()
            val currentYear = currentDate.get(Calendar.YEAR)
            val currentMonth = currentDate.get(Calendar.MONTH) + 1

            return year < currentYear || (year == currentYear && month < currentMonth)
        } catch (e: Exception) {
            return true
        }
    }

    fun processPayment(amount: Int, paymentMethod: String): Boolean {
        // Уақытша төлем әрдайым сәтті
        // Нақты төлем жүйесін интеграциялау керек
        return true
    }
}

data class PaymentResult(val success: Boolean, val message: String)