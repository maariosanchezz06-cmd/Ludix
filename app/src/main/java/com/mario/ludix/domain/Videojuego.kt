package com.mario.ludix.domain

/**
 * Representa un juego en la base de datos.
 * Atributos basados en la pág. 3 de la Memoria del Proyecto.
 */
data class Videojuego(
    val id_videojuego: String = "",    // PK: Identificador único del juego
    val nombre: String = "",
    val descripcion: String = "",      // Sin acento en código para evitar problemas
    val genero: String = "",           // Ej: "RPG", "Shooter"
    val plataforma: String = "",       // Ej: "PC", "PS5", "Switch"
    val fecha_lanzamiento: String = "",
    val imagen_portada: String = ""    // URL de la imagen
)