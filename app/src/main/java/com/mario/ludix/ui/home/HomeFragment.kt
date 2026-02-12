package com.mario.ludix.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.mario.ludix.R

class HomeFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // --- LÍNEA TEMPORAL PARA TESTEO ---
        // Descomenta la siguiente línea para cerrar sesión y volver a ver el Login/Registro
        // FirebaseAuth.getInstance().signOut()
        // ----------------------------------

        // Inflamos el layout fragment_home que diseñamos antes
        return inflater.inflate(R.layout.fragment_home, container, false)
    }
}