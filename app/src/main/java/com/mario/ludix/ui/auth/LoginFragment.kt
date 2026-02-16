package com.mario.ludix.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.mario.ludix.R
import com.mario.ludix.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    // 1. Cambio: Implementamos View Binding como en el HomeFragment
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. Cambio: Accedemos a las vistas de forma segura sin findViewById

        // Botón entrar: Lógica de Firebase
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmailLogin.text.toString().trim()
            val pass = binding.etPassLogin.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(email, pass)
                    .addOnSuccessListener {
                        Toast.makeText(context, "¡Bienvenido de nuevo!", Toast.LENGTH_SHORT).show()
                        // Navegamos a la Home usando la acción del nav_graph
                        findNavController().navigate(R.id.action_loginFragment_to_navigation_home)
                    }
                    .addOnFailureListener {
                        Toast.makeText(context, "Error: Email o contraseña incorrectos", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(context, "Por favor, rellena todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Botón volver al registro
        binding.tvGoToRegister.setOnClickListener {
            // Usamos popBackStack para no cargar la memoria, simplemente cerramos esta vista
            findNavController().popBackStack()
        }

        // ==============================
        // RECUPERAR CONTRASEÑA (AÑADIDO)
        // ==============================

        // 1. Vinculamos el nuevo TextView
        val tvForgotPassword = binding.tvForgotPassword

        // 2. Lógica para recuperar contraseña
        tvForgotPassword.setOnClickListener {
            val email = binding.etEmailLogin.text.toString().trim()

            if (email.isNotEmpty()) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(
                                context,
                                "Se ha enviado un correo para restablecer tu contraseña a: $email",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Error: ${task.exception?.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            } else {
                Toast.makeText(
                    context,
                    "Por favor, introduce tu email en el campo de arriba para poder recuperarla",
                    Toast.LENGTH_SHORT
                ).show()
                binding.etEmailLogin.error = "Introduce tu email aquí"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
