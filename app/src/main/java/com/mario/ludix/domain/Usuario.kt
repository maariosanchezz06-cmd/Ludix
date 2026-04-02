package com.mario.ludix.domain

/**
 * Representa a un gamer en la plataforma Ludix.
 * Cumple con Objetivo 5.5: Control de cuenta (ACTIVO/INACTIVO)
 */
data class Usuario(
    val id_usuario: String = "",
    val nombre_usuario: String = "",
    val email: String = "",
    val imagen_perfil: String = "",
    val fecha_registro: Long = System.currentTimeMillis(),
    val estado: String = "ACTIVO",  // ACTIVO, INACTIVO, SUSPENDIDO
    val bio: String = "",           // Para el perfil
    val seguidores: Int = 0,        // Contador
    val siguiendo: Int = 0          // Contador
)