package com.mario.ludix.domain // OJO: Aquí ponemos domain, no model

data class Clip(
    var id: String = "",          // ID único del documento en Firebase
    val url: String = "",         // Enlace del vídeo
    val titulo: String = "",      // Título/Descripción
    val autor: String = "",       // Nombre del creador
    val autorId: String = "",     // ID del creador (para el perfil)
    var likes: Int = 0,           // Contador de likes
    val timestamp: Long = 0       // Para saber cuándo se subió
)