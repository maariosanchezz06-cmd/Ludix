package com.mario.ludix.domain

data class Clip(
    var id: String = "",          // ID único del documento en Firebase
    val url: String = "",         // Enlace del vídeo
    val titulo: String = "",      // Título/Descripción
    val autor: String = "",       // Nombre del creador
    val autorId: String = "",     // ID del creador (para el perfil)
    val id_juego: String = "",    // ID técnico del juego
    val juego: String = "",       // Nombre del juego para mostrar
    var likes: Int = 0,           // Contador de likes
    val timestamp: Long = 0L      // Para saber cuándo se subió
)