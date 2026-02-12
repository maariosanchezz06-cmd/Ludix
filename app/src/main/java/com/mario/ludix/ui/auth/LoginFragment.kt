package com.mario.ludix.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.mario.ludix.R

class LoginFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias
        val etEmail = view.findViewById<TextInputEditText>(R.id.etEmailLogin)
        val etPass = view.findViewById<TextInputEditText>(R.id.etPassLogin)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvGoToRegister = view.findViewById<TextView>(R.id.tvGoToRegister)

        // Botón entrar
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString()
            val pass = etPass.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        Toast.makeText(context, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()
                        // Navegamos a la Home
                        findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error: Datos incorrectos", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Rellena los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón volver al registro
        tvGoToRegister.setOnClickListener {
            findNavController().popBackStack() // Simplemente vuelve atrás
        }
    }
}