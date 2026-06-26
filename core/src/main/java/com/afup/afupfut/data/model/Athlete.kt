package com.afup.afupfut.data.model

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

data class Athlete(
    val id: String = "",
    val photoUrl: String = "",
    val name: String = "",
    val nickname: String = "",
    val height: Double = 0.0,
    val weight: Double = 0.0,
    val dominantFoot: String = "", // "Direito" ou "Esquerdo"
    val positions: List<String> = emptyList(), // Ex: ["Goleiro", "Zagueiro", "Meia", "Atacante"]
    val birthDate: String = "", // Formato "dd/MM/yyyy"
    val rating: Int? = null, // Avaliação de 1 a 10 estrelas (nula até que o admin classifique)
    val isAdmin: Boolean = false,
    val athleteType: String = "Associado",
    val isManager: Boolean = false,
    val registrationDate: Long = System.currentTimeMillis()
) {
    // Calcula a idade automaticamente com base na data de nascimento
    fun getAge(): Int {
        return try {
            val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dob = LocalDate.parse(birthDate, formatter)
            val now = LocalDate.now()
            ChronoUnit.YEARS.between(dob, now).toInt()
        } catch (e: Exception) {
            0
        }
    }
}
