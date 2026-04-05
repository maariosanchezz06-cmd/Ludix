package com.mario.ludix.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mario.ludix.R
import com.mario.ludix.adapter.JuegoAdapter
import com.mario.ludix.databinding.FragmentDashboardBinding
import com.mario.ludix.domain.Videojuego

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private var listaJuegosOriginal = mutableListOf<Videojuego>()

    // Para almacenar el conteo de votos por juego
    private val votosCountMap = mutableMapOf<String, Int>()
    private val mediaVotosMap = mutableMapOf<String, Double>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Aplicar degradado al título
        aplicarGradienteTitulo()

        binding.rvJuegosCatalogo.layoutManager = LinearLayoutManager(context)

        // Configurar listeners de los chips
        binding.chipRecientes.setOnClickListener { ordenarPorRecientes() }
        binding.chipMejorValorados.setOnClickListener { ordenarPorMejorValorados() }
        binding.chipMasVotados.setOnClickListener { ordenarPorMasVotados() }
        binding.chipAZ.setOnClickListener { ordenarAlfabeticamente() }

        // Cargar datos iniciales
        cargarCatalogoConPuntuaciones()
    }

    private fun aplicarGradienteTitulo() {
        val paint = binding.tvTituloExplorar.paint
        val width = paint.measureText(binding.tvTituloExplorar.text.toString())
        binding.tvTituloExplorar.paint.shader = android.graphics.LinearGradient(
            0f, 0f, width, binding.tvTituloExplorar.textSize,
            intArrayOf(
                android.graphics.Color.parseColor("#00E5FF"),
                android.graphics.Color.parseColor("#9D50BB")
            ),
            null, android.graphics.Shader.TileMode.CLAMP
        )
    }

    private fun cargarCatalogoConPuntuaciones() {
        binding.progressBar.visibility = View.VISIBLE

        // Primero cargar todas las puntuaciones
        db.collection("puntuaciones").get()
            .addOnSuccessListener { puntuacionesResult ->
                // Calcular votos y media por juego
                val puntuacionesPorJuego = mutableMapOf<String, MutableList<Int>>()

                for (doc in puntuacionesResult) {
                    val idJuego = doc.getString("id_videojuego") ?: continue
                    val valor = doc.getLong("valor")?.toInt() ?: continue

                    if (!puntuacionesPorJuego.containsKey(idJuego)) {
                        puntuacionesPorJuego[idJuego] = mutableListOf()
                    }
                    puntuacionesPorJuego[idJuego]?.add(valor)
                }

                // Calcular estadísticas
                puntuacionesPorJuego.forEach { (idJuego, votos) ->
                    votosCountMap[idJuego] = votos.size
                    mediaVotosMap[idJuego] = if (votos.isNotEmpty()) votos.average() else 0.0
                }

                // Ahora cargar los juegos
                cargarJuegos()
            }
            .addOnFailureListener {
                cargarJuegos() // Cargar juegos aunque fallen las puntuaciones
            }
    }

    private fun cargarJuegos() {
        db.collection("juegos").get()
            .addOnSuccessListener { result ->
                binding.progressBar.visibility = View.GONE

                listaJuegosOriginal = result.map { doc ->
                    val juego = doc.toObject(Videojuego::class.java)
                    juego.id = doc.id

                    // Actualizar puntuación con la media real calculada
                    juego.puntuacion = mediaVotosMap[doc.id] ?: 0.0

                    juego
                }.toMutableList()

                // Mostrar ordenados por recientes (por defecto)
                ordenarPorRecientes()
            }
            .addOnFailureListener {
                binding.progressBar.visibility = View.GONE
                Toast.makeText(context, "Error al cargar catálogo", Toast.LENGTH_SHORT).show()
            }
    }

    private fun ordenarPorRecientes() {
        // En Firebase no tenemos fecha de creación del juego, así que usamos el ID
        // (los IDs autogenerados de Firebase son cronológicos)
        val listaOrdenada = listaJuegosOriginal.sortedByDescending { it.id }
        mostrarLista(listaOrdenada)
    }

    private fun ordenarPorMejorValorados() {
        val listaOrdenada = listaJuegosOriginal.sortedByDescending { 
            mediaVotosMap[it.id] ?: 0.0 
        }
        mostrarLista(listaOrdenada)
    }

    private fun ordenarPorMasVotados() {
        val listaOrdenada = listaJuegosOriginal.sortedByDescending { 
            votosCountMap[it.id] ?: 0 
        }
        mostrarLista(listaOrdenada)
    }

    private fun ordenarAlfabeticamente() {
        val listaOrdenada = listaJuegosOriginal.sortedBy { it.titulo.lowercase() }
        mostrarLista(listaOrdenada)
    }

    private fun mostrarLista(lista: List<Videojuego>) {
        binding.rvJuegosCatalogo.adapter = JuegoAdapter(lista) { juego ->
            val bundle = Bundle().apply {
                putString("juegoId", juego.id)
                putString("juegoTitulo", juego.titulo)
                putString("juegoGenero", juego.genero)
                putString("juegoImageUrl", juego.imageUrl)
            }
            findNavController().navigate(R.id.gameDetailFragment, bundle)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}