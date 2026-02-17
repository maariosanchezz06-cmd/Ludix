package com.mario.ludix.domain

import com.google.firebase.firestore.PropertyName

data class Videojuego(
    var id: String = "",
    var titulo: String = "",
    var genero: String = "",
    var puntuacion: Double = 0.0,  // OJO: Si en Firebase pusiste texto, pon String aquí
    var imageUrl: String = ""      // La foto
) {
    // Constructor vacío necesario para Firebase
    constructor() : this("", "", "", 0.0, "")
}