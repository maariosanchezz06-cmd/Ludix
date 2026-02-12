package com.mario.ludix.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView // Importante para el enlace de abajo
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController // Importante para navegar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.mario.ludix.R

class AuthFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()

    override fun onStart() {
        super.onStart()
        // Comprobamos si el usuario ya está logueado en Firebase
        val usuarioActual = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        if (usuarioActual != null) {
            // Si existe, saltamos directamente a la Home sin pasar por el Registro
            findNavController().navigate(R.id.action_authFragment_to_navigation_home)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. VINCULACIÓN DE VISTAS
        val tilNombre = view.findViewById<TextInputLayout>(R.id.tilNombre)
        val tilEmail = view.findViewById<TextInputLayout>(R.id.tilEmail)
        val tilPass = view.findViewById<TextInputLayout>(R.id.tilPassword)
        val tilConfirmPass = view.findViewById<TextInputLayout>(R.id.tilConfirmPassword)

        val etNombre = view.findViewById<TextInputEditText>(R.id.etNombre)
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmail)
        val etPass = view.findViewById<TextInputEditText>(R.id.etPassword)
        val etConfirmPass = view.findViewById<TextInputEditText>(R.id.etConfirmPassword)

        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvLogin = view.findViewById<TextView>(R.id.tvLogin) // El enlace "¿Ya tienes cuenta?"

        // 2. OBSERVADOR DEL ESTADO
        viewModel.estado.observe(viewLifecycleOwner) { estado ->
            tilNombre.error = null
            tilEmail.error = null
            tilPass.error = null
            tilConfirmPass.error = null
            btnRegister.isEnabled = true
            btnRegister.text = "REGISTRARSE"

            when (estado) {
                is AuthState.Loading -> {
                    btnRegister.isEnabled = false
                    btnRegister.text = "Cargando..."
                }
                is AuthState.Success -> {
                    Toast.makeText(context, "✅ ¡Bienvenido a Ludix!", Toast.LENGTH_LONG).show()
                    // Navegamos a la Home y borramos el historial de atrás para que no pueda volver al registro
                    findNavController().navigate(R.id.action_authFragment_to_navigation_home)
                }
                is AuthState.Error -> {
                    btnRegister.text = "REGISTRARSE"
                    when (estado.tipo) {
                        TipoError.PASSWORD_MISMATCH -> {
                            tilConfirmPass.error = estado.mensaje
                            tilPass.error = " "
                        }
                        TipoError.PASSWORD_WEAK -> tilPass.error = estado.mensaje
                        TipoError.FIREBASE -> tilEmail.error = estado.mensaje
                        TipoError.GENERICO -> Toast.makeText(context, estado.mensaje, Toast.LENGTH_SHORT).show()
                    }
                }
                is AuthState.Idle -> { /* Sin acción */ }
            }
        }

        // 3. EVENTO DEL BOTÓN REGISTRAR
        btnRegister.setOnClickListener {
            val nombre = etNombre.text.toString()
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()
            val confirmPass = etConfirmPass.text.toString()

            viewModel.validarYRegistrar(nombre, email, pass, confirmPass)
        }

        // 4. ENLACE AL LOGIN (Navegación)
        tvLogin.setOnClickListener {
            // Esto usa la flecha que creamos en mobile_navigation.xml
            findNavController().navigate(R.id.action_authFragment_to_loginFragment)
        }
    }
}