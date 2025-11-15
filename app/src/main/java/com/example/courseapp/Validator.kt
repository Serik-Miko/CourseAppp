package com.example.courseapp

object Validator {

    // Логинге рұқсат етілген символдар (a-z, A-Z, 0-9, -, _)
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9](?:[A-Za-z0-9-_]{1,28}[A-Za-z0-9])?$")

    // Парольге тек 5+ таңба, арнайы символдар болмайды
    private val PASSWORD_INVALID_CHARS = Regex("[\\s@#\$()]+")

    // Курс атауына рұқсат етілген символдар
    private val COURSE_TITLE_REGEX = Regex("^[А-ЯӘҒҚҢӨҰҮҺа-яәғқңөұүһA-Za-z0-9\\s\\-\\._]+$")

    // Оқытушы атына рұқсат етілген символдар
    private val INSTRUCTOR_NAME_REGEX = Regex("^[А-ЯӘҒҚҢӨҰҮҺа-яәғқңөұүһA-Za-z\\s\\-]+$")

    // Ұзақтыққа рұқсат етілген символдар
    private val DURATION_REGEX = Regex("^[А-ЯӘҒҚҢӨҰҮҺа-яәғқңөұүһA-Za-z0-9\\s\\+]+$")

    // Мүмкіндіктерге рұқсат етілген символдар
    private val FEATURES_REGEX = Regex("^[А-ЯӘҒҚҢӨҰҮҺа-яәғқңөұүһA-Za-z0-9\\s\\-\\,\\.\\;]+$")

    // === БҰРЫНҒЫ ФУНКЦИЯЛАР (өзгеріссіз) ===

    fun isValidUsername(u: String): Boolean {
        val s = u.trim()

        // Ұзындық тексеру
        if (s.length < 3 || s.length > 30) return false

        // Бірнеше -- немесе __ болмауы керек
        if (s.contains("--") || s.contains("__") || s.contains("  ")) {
            return false
        }

        // Басында/соңында - _ болмауы
        if (s.startsWith("-") || s.endsWith("-") || s.startsWith("_") || s.endsWith("_")) {
            return false
        }

        // Тек рұқсат етілген символдар
        val USERNAME_REGEX = Regex("^[A-Za-z0-9](?:[A-Za-z0-9-_]{1,28}[A-Za-z0-9])?$")
        return USERNAME_REGEX.matches(s)
    }

    fun isValidPassword(p: String): Boolean {
        if (p.length < 5) return false
        if (PASSWORD_INVALID_CHARS.containsMatchIn(p)) return false
        return true
    }

    fun isValidEmail(e: String): Boolean {
        val s = e.trim().lowercase()

        // Бос болмайды
        if (s.isEmpty()) return false

        // Бірнеше -- немесе __ болмауы керек
        if (s.contains("--") || s.contains("__") || s.contains("..")) {
            return false
        }

        // Басында/соңында . немесе - болмайды
        if (s.startsWith(".") || s.endsWith(".") || s.startsWith("-") || s.endsWith("-")) return false

        // Пробел, # $ ( ) болмайды
        if (s.contains(' ') || s.contains('#') || s.contains('$') || s.contains('(') || s.contains(')')) return false

        // @ болуы керек, тек бір рет
        val parts = s.split("@")
        if (parts.size != 2) return false

        val localPart = parts[0] // @ алдындағы бөлік
        val domainPart = parts[1] // @ кейінгі бөлік

        // @ алдындағы бөлікте бірнеше -- __ болмауы
        if (localPart.contains("--") || localPart.contains("__") || localPart.contains("..")) {
            return false
        }

        // @ алдындағы бөлікте басында/соңында - болмайды
        if (localPart.startsWith("-") || localPart.endsWith("-")) return false

        // @ кейінгі бөлікте басында/соңында - болмайды
        val domainSegments = domainPart.split(".")
        if (domainSegments.any { it.startsWith("-") || it.endsWith("-") }) return false

        // Жалпы email форматы
        val EMAIL_REGEX = Regex("^[A-Za-z0-9._-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
        return EMAIL_REGEX.matches(s)
    }

    // === ЖАҢА ФУНКЦИЯЛАР (Курс қосу үшін) ===

    // Курс атауын тексеру
    fun validateCourseTitle(title: String): ValidationResult {
        val s = title.trim()

        if (s.isBlank()) {
            return ValidationResult(false, "Курс атауы бос болмауы керек")
        }

        if (s.length < 3 || s.length > 60) {
            return ValidationResult(false, "Курс атауы 3-60 таңба аралығында болуы керек")
        }

        // Арнайы белгілермен басталуын тексеру
        if (s.startsWith("-") || s.startsWith(".") || s.startsWith("_") ||
            s[0].isDigit()) {
            return ValidationResult(false, "Курс атауы арнайы белгілермен немесе санмен басталмауы керек")
        }

        // Қатар келген белгілерді тексеру
        if (s.contains("--") || s.contains("__") || s.contains("..")) {
            return ValidationResult(false, "Қатар келген арнайы белгілерге рұқсат етілмейді")
        }

        // Тек сандардан тұруын тексеру
        if (s.matches(Regex("^[0-9]+$"))) {
            return ValidationResult(false, "Курс атауы тек сандардан тұра алмайды")
        }

        // Рұқсат етілген символдар
        if (!COURSE_TITLE_REGEX.matches(s)) {
            return ValidationResult(false, "Курс атауында рұқсат етілмеген символдар бар")
        }

        // Бірінші әріптің бас әріп екенін тексеру
        if (!s[0].isUpperCase()) {
            return ValidationResult(false, "Курс атауы бас әріптен басталуы керек")
        }

        return ValidationResult(true, "Жарамды")
    }

    // Оқытушы атын тексеру
    fun validateInstructorName(instructor: String): ValidationResult {
        val s = instructor.trim()

        if (s.isBlank()) {
            return ValidationResult(false, "Оқытушы аты бос болмауы керек")
        }

        // Аты мен тегінің болуын тексеру (кемінде 2 сөз)
        val words = s.split("\\s+".toRegex())
        if (words.size < 2) {
            return ValidationResult(false, "Оқытушының аты мен тегі екеуі де көрсетілуі керек")
        }

        // Әр сөздің бірінші әрпі бас әріп екенін тексеру
        for (word in words) {
            if (word.isNotEmpty() && !word[0].isUpperCase()) {
                return ValidationResult(false, "Әр сөздің бірінші әрпі бас әріппен жазылуы керек")
            }
        }

        // Рұқсат етілген символдар
        if (!INSTRUCTOR_NAME_REGEX.matches(s)) {
            return ValidationResult(false, "Оқытушы атында рұқсат етілмеген символдар бар")
        }

        // Артық бос орындарды тексеру
        if (s.contains("  ")) {
            return ValidationResult(false, "Артық бос орындар бар")
        }

        // Ұзындық тексеруі
        if (s.length < 5 || s.length > 50) {
            return ValidationResult(false, "Оқытушы аты 5-50 таңба аралығында болуы керек")
        }

        return ValidationResult(true, "Жарамды")
    }

    // Бағаны тексеру
    fun validatePrice(price: String): ValidationResult {
        val s = price.trim()

        if (s.isBlank()) {
            return ValidationResult(false, "Баға бос болмауы керек")
        }

        val priceValue = s.toIntOrNull()
        if (priceValue == null) {
            return ValidationResult(false, "Баға сан түрінде болуы керек")
        }

        if (priceValue <= 0) {
            return ValidationResult(false, "Баға оң сан болуы керек")
        }

        if (priceValue < 5000) {
            return ValidationResult(false, "Баға 5,000 тг-ден төмен болмауы керек")
        }

        if (priceValue > 500000) {
            return ValidationResult(false, "Баға 500,000 тг-ден аспауы керек")
        }

        return ValidationResult(true, "Жарамды")
    }

    // Ұзақтықты тексеру
    fun validateDuration(duration: String): ValidationResult {
        val s = duration.trim()

        if (s.isBlank()) {
            return ValidationResult(false, "Ұзақтық бос болмауы керек")
        }

        // Тек сандардан тұруын тексеру
        if (s.matches(Regex("^[0-9]+$"))) {
            return ValidationResult(false, "Ұзақтық тек сандардан тұра алмайды")
        }

        // Рұқсат етілген символдар
        if (!DURATION_REGEX.matches(s)) {
            return ValidationResult(false, "Ұзақтықта рұқсат етілмеген символдар бар")
        }

        // Мағынасыз енгізулерді тексеру
        if (s.contains("++") || s.contains("!!")) {
            return ValidationResult(false, "Ұзақтық мағынасыз символдарды қамтымауы керек")
        }

        // Ұзындық тексеруі
        if (s.length < 5 || s.length > 100) {
            return ValidationResult(false, "Ұзақтық 5-100 таңба аралығында болуы керек")
        }

        return ValidationResult(true, "Жарамды")
    }

    // Сипаттаманы тексеру
    fun validateDescription(description: String): ValidationResult {
        val s = description.trim()

        if (s.isBlank()) {
            return ValidationResult(false, "Сипаттама бос болмауы керек")
        }

        if (s.length < 10) {
            return ValidationResult(false, "Сипаттама өте қысқа (кемінде 10 таңба)")
        }

        if (s.length > 1000) {
            return ValidationResult(false, "Сипаттама өте ұзын (1000 таңбадан аспауы керек)")
        }

        // Эмодзи және HTML тегтерін тексеру
        if (s.contains(Regex("[\\uD83C-\\uDBFF\\uDC00-\\uDFFF]")) ||
            s.contains("<") || s.contains(">")) {
            return ValidationResult(false, "Сипаттамада эмодзи немесе HTML тегтеріне рұқсат етілмейді")
        }

        // Артық бос орындарды тексеру
        if (s.contains("  ")) {
            return ValidationResult(false, "Сипаттамада артық бос орындар бар")
        }

        return ValidationResult(true, "Жарамды")
    }

    // Мүмкіндіктерді тексеру
    fun validateFeatures(features: String): ValidationResult {
        val s = features.trim()

        if (s.isBlank()) {
            return ValidationResult(false, "Мүмкіндіктер бос болмауы керек")
        }

        val featureList = s.split("\n").filter { it.isNotBlank() }

        if (featureList.size < 2) {
            return ValidationResult(false, "Кемінде 2 мүмкіндік көрсетілуі керек")
        }

        // Бірдей мүмкіндіктерді тексеру
        if (featureList.distinct().size != featureList.size) {
            return ValidationResult(false, "Бірдей мүмкіндіктерді қайталауға болмайды")
        }

        for (feature in featureList) {
            val trimmedFeature = feature.trim()
            if (trimmedFeature.isBlank()) {
                return ValidationResult(false, "Бос жолдар болмауы керек")
            }

            // Рұқсат етілген символдар
            if (!FEATURES_REGEX.matches(trimmedFeature)) {
                return ValidationResult(false, "'$trimmedFeature' мүмкіндігінде рұқсат етілмеген символдар бар")
            }
        }

        return ValidationResult(true, "Жарамды")
    }
}

// Нәтиже класы
data class ValidationResult(val isValid: Boolean, val message: String)