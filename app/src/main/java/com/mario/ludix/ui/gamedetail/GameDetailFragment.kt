package com.mario.ludix.ui.gamedetail

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mario.ludix.R
import com.mario.ludix.adapter.ClipMiniAdapter
import com.mario.ludix.databinding.FragmentGameDetailBinding
import com.mario.ludix.domain.Clip

class GameDetailFragment : Fragment() {

    private var _binding: FragmentGameDetailBinding? = null
    private val binding get() = _binding!!

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var juegoId: String = ""
    private var juegoTitulo: String = ""
    private var juegoGenero: String = ""
    private var juegoImageUrl: String = ""
    
    private var miPuntuacionActual: Int = 0
    private var miPuntuacionDocId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGameDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Recuperar argumentos
        juegoId = arguments?.getString("juegoId") ?: ""
        juegoTitulo = arguments?.getString("juegoTitulo") ?: "Juego"
        juegoGenero = arguments?.getString("juegoGenero") ?: ""
        juegoImageUrl = arguments?.getString("juegoImageUrl") ?: ""

        if (juegoId.isEmpty()) {
            Toast.makeText(context, "Error: Juego no encontrado", Toast.LENGTH_SHORT).show()
            findNavController().popBackStack()
            return
        }

        // Configurar UI inicial
        configurarUI()
        
        // Cargar datos
        cargarPuntuacionMedia()
        cargarMiPuntuacion()
        cargarClipsDelJuego()

        // Listeners
        binding.toolbarDetalle.setNavigationOnClickListener {
            findNavController().popBackStack()
        }

        binding.btnPuntuarJuego.setOnClickListener {
            mostrarDialogoPuntuar()
        }
    }

    private fun configurarUI() {
        binding.collapsingToolbar.title = juegoTitulo
        binding.tvTituloDetalle.text = juegoTitulo
        binding.tvGeneroDetalle.text = juegoGenero

        if (juegoImageUrl.isNotEmpty()) {
            Glide.with(this)
                .load(juegoImageUrl)
                .centerCrop()
                .placeholder(R.drawable.logo_ludix)
                .into(binding.ivPortadaDetalle)
        }

        // Configurar RecyclerView horizontal
        binding.rvClipsJuego.layoutManager = LinearLayoutManager(
            context, LinearLayoutManager.HORIZONTAL, false
        )
    }

    private fun cargarPuntuacionMedia() {
        db.collection("puntuaciones")
            .whereEqualTo("id_videojuego", juegoId)
            .get()
            .addOnSuccessListener { result ->
                if (result.isEmpty) {
                    binding.tvPuntuacionMedia.text = "-"
                    binding.tvNumVotos.text = "Sin votos aún"
                    binding.tvEstrellas.text = "☆☆☆☆☆"
                    return@addOnSuccessListener
                }

                val puntuaciones = result.mapNotNull { it.getLong("valor")?.toInt() }
                val media = puntuaciones.average()
                val numVotos = puntuaciones.size

                binding.tvPuntuacionMedia.text = String.format("%.1f", media)
                binding.tvNumVotos.text = "$numVotos voto${if (numVotos != 1) "s" else ""}"
                
                // Mostrar estrellas según la media
                val estrellasLlenas = media.toInt()
                val estrellaMedia = if (media - estrellasLlenas >= 0.5) "⭐" else ""
                binding.tvEstrellas.text = "⭐".repeat(estrellasLlenas) + estrellaMedia + 
                    "☆".repeat(5 - estrellasLlenas - (if (estrellaMedia.isNotEmpty()) 1 else 0))
            }
    }

    private fun cargarMiPuntuacion() {
        val userId = auth.currentUser?.uid ?: return

        db.collection("puntuaciones")
            .whereEqualTo("id_videojuego", juegoId)
            .whereEqualTo("id_usuario", userId)
            .get()
            .addOnSuccessListener { result ->
                if (!result.isEmpty) {
                    val doc = result.documents[0]
                    miPuntuacionActual = doc.getLong("valor")?.toInt() ?: 0
                    miPuntuacionDocId = doc.id
                    
                    binding.tvYaVotaste.visibility = View.VISIBLE
                    binding.tvYaVotaste.text = "Tu voto: ${"⭐".repeat(miPuntuacionActual)}. Pulsa para cambiar."
                    binding.btnPuntuarJuego.text = "✏️ CAMBIAR MI VOTO"
                }
            }
    }

    private fun cargarClipsDelJuego() {
        db.collection("clips")
            .whereEqualTo("id_juego", juegoId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->
                val clips = result.map { doc ->
                    val clip = doc.toObject(Clip::class.java)
                    clip.id = doc.id
                    clip
                }

                if (clips.isEmpty()) {
                    binding.tvSinClipsJuego.visibility = View.VISIBLE
                    binding.rvClipsJuego.visibility = View.GONE
                } else {
                    binding.tvSinClipsJuego.visibility = View.GONE
                    binding.rvClipsJuego.visibility = View.VISIBLE
                    binding.rvClipsJuego.adapter = ClipMiniAdapter(clips) { clip ->
                        Toast.makeText(context, "Clip de ${clip.autor}", Toast.LENGTH_SHORT).show()
                        // Aquí podrías navegar al feed en ese clip específico
                    }
                }
            }
            .addOnFailureListener {
                binding.tvSinClipsJuego.visibility = View.VISIBLE
                binding.tvSinClipsJuego.text = "Error al cargar clips"
            }
    }

    private fun mostrarDialogoPuntuar() {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(context, "Debes iniciar sesión para votar", Toast.LENGTH_SHORT).show()
            return
        }

        val dialogView = layoutInflater.inflate(R.layout.dialog_puntuar_juego, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        val tvNombreJuego = dialogView.findViewById<TextView>(R.id.tvNombreJuegoPuntuar)
        val tvPuntuacionSel = dialogView.findViewById<TextView>(R.id.tvPuntuacionSeleccionada)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarPuntuar)
        val btnConfirmar = dialogView.findViewById<Button>(R.id.btnConfirmarPuntuar)

        val stars = listOf(
            dialogView.findViewById<ImageView>(R.id.star1),
            dialogView.findViewById<ImageView>(R.id.star2),
            dialogView.findViewById<ImageView>(R.id.star3),
            dialogView.findViewById<ImageView>(R.id.star4),
            dialogView.findViewById<ImageView>(R.id.star5)
        )

        tvNombreJuego.text = juegoTitulo
        var puntuacionSeleccionada = miPuntuacionActual

        // Función para actualizar visualización de estrellas
        fun actualizarEstrellas(valor: Int) {
            puntuacionSeleccionada = valor
            stars.forEachIndexed { index, star ->
                if (index < valor) {
                    star.setImageResource(R.drawable.ic_star_filled)
                } else {
                    star.setImageResource(R.drawable.ic_star_outline)
                }
            }
            tvPuntuacionSel.text = when (valor) {
                1 -> "😕 Malo"
                2 -> "😐 Regular"
                3 -> "🙂 Bueno"
                4 -> "😊 Muy bueno"
                5 -> "🤩 ¡Excelente!"
                else -> "Selecciona una puntuación"
            }
        }

        // Si ya votó, mostrar su voto actual
        if (miPuntuacionActual > 0) {
            actualizarEstrellas(miPuntuacionActual)
        }

        // Click en cada estrella
        stars.forEachIndexed { index, star ->
            star.setOnClickListener {
                actualizarEstrellas(index + 1)
            }
        }

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnConfirmar.setOnClickListener {
            if (puntuacionSeleccionada == 0) {
                Toast.makeText(context, "Selecciona una puntuación", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            guardarPuntuacion(userId, puntuacionSeleccionada)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun guardarPuntuacion(userId: String, valor: Int) {
        val puntuacionData = hashMapOf(
            "id_usuario" to userId,
            "id_videojuego" to juegoId,
            "valor" to valor,
            "fecha" to System.currentTimeMillis()
        )

        if (miPuntuacionDocId != null) {
            // Actualizar puntuación existente
            db.collection("puntuaciones")
                .document(miPuntuacionDocId!!)
                .update(puntuacionData as Map<String, Any>)
                .addOnSuccessListener {
                    Toast.makeText(context, "✅ Voto actualizado: ${"⭐".repeat(valor)}", Toast.LENGTH_SHORT).show()
                    miPuntuacionActual = valor
                    cargarPuntuacionMedia()
                    binding.tvYaVotaste.text = "Tu voto: ${"⭐".repeat(valor)}. Pulsa para cambiar."
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al actualizar", Toast.LENGTH_SHORT).show()
                }
        } else {
            // Crear nueva puntuación
            db.collection("puntuaciones")
                .add(puntuacionData)
                .addOnSuccessListener { docRef ->
                    Toast.makeText(context, "✅ ¡Gracias por votar! ${"⭐".repeat(valor)}", Toast.LENGTH_SHORT).show()
                    miPuntuacionActual = valor
                    miPuntuacionDocId = docRef.id
                    cargarPuntuacionMedia()
                    binding.tvYaVotaste.visibility = View.VISIBLE
                    binding.tvYaVotaste.text = "Tu voto: ${"⭐".repeat(valor)}. Pulsa para cambiar."
                    binding.btnPuntuarJuego.text = "✏️ CAMBIAR MI VOTO"
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}