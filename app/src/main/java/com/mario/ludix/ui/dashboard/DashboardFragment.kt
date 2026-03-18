package com.mario.ludix.ui.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mario.ludix.adapter.JuegoAdapter
import com.mario.ludix.databinding.FragmentDashboardBinding
import com.mario.ludix.domain.Videojuego

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
        db.collection("juegos").get().addOnSuccessListener { result ->
            val lista = result.map { doc ->
                val juego = doc.toObject(Videojuego::class.java)
                juego.id = doc.id // Guardamos su ID de Firebase
                juego
            }
            
            // NUEVO: Le pasamos la lógica de qué hacer al tocar un juego
            binding.rvJuegosCatalogo.adapter = JuegoAdapter(lista) { juegoSeleccionado ->
                mostrarDialogoPuntuacion(juegoSeleccionado)
            }
        }.addOnFailureListener {
            Toast.makeText(context, "Error al cargar catálogo", Toast.LENGTH_SHORT).show()
        }
    }

    // NUEVA FUNCIÓN: Cuadro de diálogo para elegir la nota
    private fun mostrarDialogoPuntuacion(juego: Videojuego) {
        val opciones = arrayOf("1 Estrella ⭐", "2 Estrellas ⭐⭐", "3 Estrellas ⭐⭐⭐", "4 Estrellas ⭐⭐⭐⭐", "5 Estrellas ⭐⭐⭐⭐⭐")
        
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Puntuar: ${juego.titulo}")
        builder.setItems(opciones) { _, indexSeleccionado ->
            val nota = indexSeleccionado + 1 // El índice empieza en 0, le sumamos 1
            guardarPuntuacion(juego.id, nota)
        }
        builder.show()
    }

    // NUEVA FUNCIÓN: Guarda la nota en la base de datos
    private fun guardarPuntuacion(idJuego: String, nota: Int) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        
        val nuevaPuntuacion = hashMapOf(
            "id_usuario" to uid,
            "id_videojuego" to idJuego,
            "valor" to nota,
            "fecha" to System.currentTimeMillis()
        )
        
        db.collection("puntuaciones").add(nuevaPuntuacion).addOnSuccessListener {
            Toast.makeText(context, "¡Has valorado el juego con $nota estrellas!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}