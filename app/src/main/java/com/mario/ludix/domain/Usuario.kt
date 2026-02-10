package com.mario.ludix.domain

/**
 * Representa a un gamer en la plataforma Ludix.
 * Coincide con la entidad "Usuario" definida en el modelo E-R de la memoria.
 */
data class Usuario(
    val id_usuario: String = "",       // PK: El UID único que nos da Firebase Authentication
    val nombre_usuario: String = "",   // El "nick" del jugador
    val email: String = "",            // Correo de registro
    val imagen_perfil: String = "",    // URL de la foto en Firebase Storage
    val fecha_registro: Long = System.currentTimeMillis() // Fecha automática
)