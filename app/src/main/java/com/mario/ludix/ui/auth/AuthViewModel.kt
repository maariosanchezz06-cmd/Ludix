package com.mario.ludix.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mario.ludix.data.AuthRepository
import kotlinx.coroutines.launch

class AuthViewModel : ViewModel() {

    // Instancia del repositorio (donde llamas a Firebase)
    private val repository = AuthRepository()

    // LIVEDATA: La "antena" que el Fragment escuchará
    // Usamos un sealed class para manejar estados: Reposo, Cargando, Éxito, Error
    private val _estado = MutableLiveData<AuthState>(AuthState.Idle)
    val estado: LiveData<AuthState> = _estado

    /**
     * Función principal que valida los datos antes de molestar a Firebase.
     * Cumple Objetivo 2: Validaciones explícitas.
     */
    fun validarYRegistrar(nombre: String, email: String, pass: String, confirmPass: String) {
        // 0. Reseteamos estado a Cargando
        _estado.value = AuthState.Loading

        // 1. Validar campos vacíos
        if (nombre.isBlank() || email.isBlank() || pass.isBlank() || confirmPass.isBlank()) {
            _estado.value = AuthState.Error("Por favor, rellena todos los campos", TipoError.GENERICO)
            return
        }

        // 2. Validar coincidencia de contraseñas (Objetivo TFG)
        if (pass != confirmPass) {
            _estado.value = AuthState.Error("Las contraseñas no coinciden", TipoError.PASSWORD_MISMATCH)
            return
        }

        // 3. Validar longitud de seguridad
        if (pass.length < 6) {
            _estado.value = AuthState.Error("La contraseña es muy corta (mínimo 6)", TipoError.PASSWORD_WEAK)
            return
        }

        // 4. Si pasa los filtros -> Llamamos a Firebase
        registrarEnFirebase(email, pass, nombre)
    }

    private fun registrarEnFirebase(email: String, pass: String, nombre: String) {
        viewModelScope.launch {
            // Asumimos que tu repositorio devuelve true si se crea, false si falla
            val exito = repository.registrarUsuario(email, pass, nombre)
            if (exito) {
                _estado.value = AuthState.Success
            } else {
                _estado.value = AuthState.Error("Error al registrar. ¿El email ya existe?", TipoError.FIREBASE)
            }
        }
    }
}

// ESTADOS Y ERRORES (Copia esto también al final del archivo o en uno nuevo)
sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val mensaje: String, val tipo: TipoError) : AuthState()
}

enum class TipoError {
    GENERICO,          // Campos vacíos
    PASSWORD_MISMATCH, // No coinciden
    PASSWORD_WEAK,     // Muy corta
    FIREBASE           // Error de servidor
}