package com.mario.ludix.domain

/**
 * Representa la valoración de un usuario a un juego.
 * Relaciona las entidades Usuario y Videojuego (Relación N:M desglosada).
 */
data class Puntuacion(
    val id_puntuacion: String = "",    // PK: Auto-generado por Firebase
    val valor: Int = 0,                // Nota del 1 al 5 (o al 10)
    val fecha: Long = System.currentTimeMillis(),
    val id_usuario: String = "",       // FK: Quién votó
    val id_videojuego: String = ""     // FK: Qué juego votó
)