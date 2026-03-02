package com.mario.ludix.ui.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.mario.ludix.adapter.VideoAdapter
import com.mario.ludix.databinding.FragmentHomeBinding
import com.mario.ludix.domain.Clip

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 1. Conexión a Firestore
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 2. Cargamos los vídeos al entrar
        cargarVideosDesdeFirebase()
    }

    private fun cargarVideosDesdeFirebase() {
        // Pedimos los clips ordenados por fecha (los más nuevos arriba)
        db.collection("clips")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val listaVideos = mutableListOf<Clip>()

                for (document in result) {
                    // Convertimos el documento al objeto Clip automáticamente
                    val clip = document.toObject(Clip::class.java)

                    // --- PASO CLAVE ---
                    // Guardamos el ID del documento de Firebase en el objeto Clip
                    // Sin esto, el botón de Like no sabría a qué vídeo apuntar
                    clip.id = document.id

                    // Solo lo añadimos si tiene una URL válida
                    if (clip.url.isNotEmpty()) {
                        listaVideos.add(clip)
                    }
                }

                // 3. Enviamos la lista al Adapter
                if (listaVideos.isNotEmpty()) {
                    val adapter = VideoAdapter(requireContext(), listaVideos)
                    binding.viewPagerVideos.adapter = adapter
                } else {
                    Toast.makeText(context, "Aún no hay vídeos en Ludix", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e("HomeFragment", "Error al cargar vídeos", exception)
                Toast.makeText(context, "Error al conectar con la base de datos", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}