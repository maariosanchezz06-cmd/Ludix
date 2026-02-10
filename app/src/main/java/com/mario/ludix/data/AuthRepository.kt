package com.mario.ludix.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mario.ludix.domain.Usuario
import kotlinx.coroutines.tasks.await

class AuthRepository {

    // Instancias de Firebase (El "cerebro" y la "memoria")
    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    /**
     * Registra un usuario nuevo.
     * 1. Crea la cuenta en Firebase Authentication.
     * 2. Guarda los datos del perfil en Firestore Database.
     */
    suspend fun registrarUsuario(email: String, pass: String, nombre: String): Boolean {
        return try {
            // Paso 1: Crear usuario en Auth
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: return false

            // Paso 2: Crear el objeto Usuario con el ID que nos ha dado Firebase
            val nuevoUsuario = Usuario(
                id_usuario = uid,
                nombre_usuario = nombre,
                email = email
            )

            // Paso 3: Guardarlo en la colección "usuarios" de la base de datos
            db.collection("usuarios").document(uid).set(nuevoUsuario).await()

            true // Todo ha ido bien
        } catch (e: Exception) {
            e.printStackTrace()
            false // Algo falló
        }
    }
}