package com.mario.ludix.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mario.ludix.R

class AuthFragment : Fragment() {

    // 1. Conectamos con el ViewModel (el cerebro de la pantalla)
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Carga el diseño XML que acabamos de pegar
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. Buscamos los botones y cajas de texto por su ID
        val etNombre = view.findViewById<EditText>(R.id.etNombre)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPass = view.findViewById<EditText>(R.id.etPassword)
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)

        // 3. Qué pasa cuando pulsas el botón
        btnRegister.setOnClickListener {
            val nombre = etNombre.text.toString()
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()

            if (nombre.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty()) {
                // ¡AQUÍ OCURRE LA MAGIA! Llamamos a Firebase
                viewModel.registrarUsuario(email, pass, nombre)
                Toast.makeText(context, "Registrando...", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Por favor, rellena todo", Toast.LENGTH_SHORT).show()
            }
        }
    }
}