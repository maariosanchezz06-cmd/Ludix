package com.mario.ludix.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mario.ludix.domain.Usuario
import kotlinx.coroutines.tasks.await

class AuthRepository {

    private val auth: FirebaseAuth = Firebase.auth
    private val db: FirebaseFirestore = Firebase.firestore

    /**
     * Registra un usuario nuevo con estado ACTIVO por defecto
     */
    suspend fun registrarUsuario(email: String, pass: String, nombre: String): Boolean {
        return try {
            val authResult = auth.createUserWithEmailAndPassword(email, pass).await()
            val uid = authResult.user?.uid ?: return false

            val nuevoUsuario = Usuario(
                id_usuario = uid,
                nombre_usuario = nombre,
                email = email,
                estado = "ACTIVO"  // Estado inicial
            )

            db.collection("usuarios").document(uid).set(nuevoUsuario).await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Desactiva una cuenta (borrado lógico) - Objetivo 5.5
     */
    suspend fun desactivarCuenta(userId: String): Boolean {
        return try {
            db.collection("usuarios")
                .document(userId)
                .update("estado", "INACTIVO")
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Reactiva una cuenta
     */
    suspend fun reactivarCuenta(userId: String): Boolean {
        return try {
            db.collection("usuarios")
                .document(userId)
                .update("estado", "ACTIVO")
                .await()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Obtiene los datos del usuario actual
     */
    suspend fun obtenerUsuarioActual(): Usuario? {
        return try {
            val uid = auth.currentUser?.uid ?: return null
            val document = db.collection("usuarios").document(uid).get().await()
            document.toObject(Usuario::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Verifica si el usuario está activo antes de permitir login
     */
    suspend fun verificarEstadoUsuario(userId: String): String {
        return try {
            val document = db.collection("usuarios").document(userId).get().await()
            document.getString("estado") ?: "ACTIVO"
        } catch (e: Exception) {
            "ACTIVO"
        }
    }
}