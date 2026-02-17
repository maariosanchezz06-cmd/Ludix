package com.mario.ludix.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.mario.ludix.adapter.JuegoAdapter
import com.mario.ludix.databinding.FragmentDashboardBinding
import com.mario.ludix.domain.Videojuego // ¡CORREGIDO! Importamos tu clase real

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.rvJuegosCatalogo.layoutManager = LinearLayoutManager(context)
        cargarCatalogo()
    }

    private fun cargarCatalogo() {
        db.collection("juegos")
            .get()
            .addOnSuccessListener { result ->
                // Aquí convertimos los documentos a objetos Videojuego
                val lista = result.map { doc ->
                    doc.toObject(Videojuego::class.java)
                }
                binding.rvJuegosCatalogo.adapter = JuegoAdapter(lista)
            }
            .addOnFailureListener {
                Toast.makeText(context, "Error al cargar catálogo", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}