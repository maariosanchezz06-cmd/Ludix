package com.mario.ludix.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mario.ludix.data.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {
    private val repository = AuthRepository()

    fun registrarUsuario(email: String, pass: String, nombre: String) {
        viewModelScope.launch {
            val exito = repository.registrarUsuario(email, pass, nombre)
            if (exito) {
                println("✅ REGISTRO OK")
            } else {
                println("❌ FALLO EN REGISTRO")
            }
        }
    }
}