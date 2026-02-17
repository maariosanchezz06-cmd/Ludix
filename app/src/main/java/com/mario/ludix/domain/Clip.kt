package com.mario.ludix.domain // OJO: Aquí ponemos domain, no model

data class Clip(
    val url: String = "",       // Enlace del vídeo
    val titulo: String = "",
    val autor: String = ""
)