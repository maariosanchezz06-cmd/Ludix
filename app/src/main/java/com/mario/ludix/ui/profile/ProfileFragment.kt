package com.mario.ludix.ui.profile

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.mario.ludix.R
import com.mario.ludix.adapter.ClipMiniAdapter
import com.mario.ludix.adapter.MiPuntuacionAdapter
import com.mario.ludix.adapter.PuntuacionConJuego
import com.mario.ludix.databinding.FragmentProfileBinding
import com.mario.ludix.domain.Clip

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var userId: String = ""
    private var nombreActual: String = ""
    private var bioActual: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        userId = auth.currentUser?.uid ?: ""
        
        if (userId.isEmpty()) {
            Toast.makeText(context, "Debes iniciar sesión", Toast.LENGTH_SHORT).show()
            findNavController().navigate(R.id.loginFragment)
            return
        }

        // Aplicar degradado al título
        aplicarGradienteTitulo()

        // Configurar RecyclerViews
        binding.rvMisClips.layoutManager = GridLayoutManager(context, 3)
        binding.rvMisPuntuaciones.layoutManager = LinearLayoutManager(context)

        // Cargar datos
        cargarDatosUsuario()
        cargarMisClips()
        cargarMisPuntuaciones()

        // Listeners
        binding.btnEditarPerfil.setOnClickListener { mostrarDialogoEditarPerfil() }
        binding.ivSettings.setOnClickListener { mostrarDialogoSettings() }
    }

    private fun aplicarGradienteTitulo() {
        val paint = binding.tvTitleProfile.paint
        val width = paint.measureText(binding.tvTitleProfile.text.toString())
        binding.tvTitleProfile.paint.shader = android.graphics.LinearGradient(
            0f, 0f, width, binding.tvTitleProfile.textSize,
            intArrayOf(
                android.graphics.Color.parseColor("#00E5FF"),
                android.graphics.Color.parseColor("#9D50BB")
            ),
            null, android.graphics.Shader.TileMode.CLAMP
        )
    }

    private fun cargarDatosUsuario() {
        db.collection("usuarios").document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    nombreActual = doc.getString("nombre_usuario") ?: "Gamer"
                    bioActual = doc.getString("bio") ?: ""
                    val email = doc.getString("email") ?: ""
                    val imagenPerfil = doc.getString("imagen_perfil") ?: ""

                    binding.tvNombreUsuario.text = "@$nombreActual"
                    binding.tvEmailUsuario.text = email
                    binding.tvBioUsuario.text = if (bioActual.isNotEmpty()) bioActual 
                        else "¡Bienvenido a Ludix! Edita tu biografía..."

                    if (imagenPerfil.isNotEmpty()) {
                        Glide.with(this)
                            .load(imagenPerfil)
                            .circleCrop()
                            .placeholder(R.drawable.logo_ludix)
                            .into(binding.ivFotoPerfil)
                    }
                }
            }
    }

    private fun cargarMisClips() {
        db.collection("clips")
            .whereEqualTo("autorId", userId)
            .get()
            .addOnSuccessListener { result ->
                val clips = result.map { doc ->
                    val clip = doc.toObject(Clip::class.java)
                    clip.id = doc.id
                    clip
                }.sortedByDescending { it.timestamp }

                val totalLikes = clips.sumOf { it.likes }
                binding.tvNumLikes.text = totalLikes.toString()
                binding.tvNumClips.text = clips.size.toString()

                if (clips.isEmpty()) {
                    binding.tvSinClips.visibility = View.VISIBLE
                    binding.rvMisClips.visibility = View.GONE
                } else {
                    binding.tvSinClips.visibility = View.GONE
                    binding.rvMisClips.visibility = View.VISIBLE
                    binding.rvMisClips.adapter = ClipMiniAdapter(clips) { clip ->
                        Toast.makeText(context, "Clip: ${clip.titulo}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .addOnFailureListener {
                binding.tvSinClips.visibility = View.VISIBLE
                binding.tvSinClips.text = "Error al cargar clips"
            }
    }

    private fun cargarMisPuntuaciones() {
        db.collection("puntuaciones")
            .whereEqualTo("id_usuario", userId)
            .get()
            .addOnSuccessListener { result ->
                binding.tvNumPuntuaciones.text = result.size().toString()

                if (result.isEmpty) {
                    binding.tvSinPuntuaciones.visibility = View.VISIBLE
                    binding.rvMisPuntuaciones.visibility = View.GONE
                    return@addOnSuccessListener
                }

                // Obtener nombres de juegos
                val puntuaciones = mutableListOf<PuntuacionConJuego>()
                var pendientes = result.size()

                for (doc in result) {
                    val idJuego = doc.getString("id_videojuego") ?: ""
                    val valor = doc.getLong("valor")?.toInt() ?: 0

                    db.collection("juegos").document(idJuego).get()
                        .addOnSuccessListener { juegoDoc ->
                            val nombreJuego = juegoDoc.getString("titulo") ?: "Juego desconocido"
                            puntuaciones.add(PuntuacionConJuego(nombreJuego, valor))
                            
                            pendientes--
                            if (pendientes == 0) {
                                binding.tvSinPuntuaciones.visibility = View.GONE
                                binding.rvMisPuntuaciones.visibility = View.VISIBLE
                                binding.rvMisPuntuaciones.adapter = MiPuntuacionAdapter(puntuaciones)
                            }
                        }
                        .addOnFailureListener {
                            pendientes--
                        }
                }
            }
    }

    private fun mostrarDialogoEditarPerfil() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_profile, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        val etNombre = dialogView.findViewById<EditText>(R.id.etEditNombre)
        val etBio = dialogView.findViewById<EditText>(R.id.etEditBio)
        val btnCancelar = dialogView.findViewById<Button>(R.id.btnCancelarEdit)
        val btnGuardar = dialogView.findViewById<Button>(R.id.btnGuardarEdit)

        etNombre.setText(nombreActual)
        etBio.setText(bioActual)

        btnCancelar.setOnClickListener { dialog.dismiss() }

        btnGuardar.setOnClickListener {
            val nuevoNombre = etNombre.text.toString().trim()
            val nuevaBio = etBio.text.toString().trim()

            if (nuevoNombre.isEmpty()) {
                Toast.makeText(context, "El nombre no puede estar vacío", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (nuevoNombre.length > 20) {
                Toast.makeText(context, "El nombre es muy largo (máx. 20)", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            db.collection("usuarios").document(userId)
                .update(
                    mapOf(
                        "nombre_usuario" to nuevoNombre,
                        "bio" to nuevaBio
                    )
                )
                .addOnSuccessListener {
                    Toast.makeText(context, "✅ Perfil actualizado", Toast.LENGTH_SHORT).show()
                    nombreActual = nuevoNombre
                    bioActual = nuevaBio
                    binding.tvNombreUsuario.text = "@$nuevoNombre"
                    binding.tvBioUsuario.text = if (nuevaBio.isNotEmpty()) nuevaBio 
                        else "¡Bienvenido a Ludix! Edita tu biografía..."
                    dialog.dismiss()
                }
                .addOnFailureListener {
                    Toast.makeText(context, "Error al guardar", Toast.LENGTH_SHORT).show()
                }
        }

        dialog.show()
    }

    private fun mostrarDialogoSettings() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_settings, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(
            android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
        )

        val optionCerrarSesion = dialogView.findViewById<LinearLayout>(R.id.optionCerrarSesion)
        val optionDesactivar = dialogView.findViewById<LinearLayout>(R.id.optionDesactivarCuenta)

        optionCerrarSesion.setOnClickListener {
            dialog.dismiss()
            cerrarSesion()
        }

        optionDesactivar.setOnClickListener {
            dialog.dismiss()
            confirmarDesactivarCuenta()
        }

        dialog.show()
    }

    private fun cerrarSesion() {
        AlertDialog.Builder(requireContext())
            .setTitle("Cerrar sesión")
            .setMessage("¿Seguro que quieres cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                auth.signOut()
                Toast.makeText(context, "Sesión cerrada", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.loginFragment)
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun confirmarDesactivarCuenta() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Desactivar cuenta")
            .setMessage("Tu cuenta se desactivará y no podrás acceder hasta contactar con soporte. ¿Continuar?")
            .setPositiveButton("Desactivar") { _, _ ->
                db.collection("usuarios").document(userId)
                    .update("estado", "INACTIVO")
                    .addOnSuccessListener {
                        auth.signOut()
                        Toast.makeText(context, "Cuenta desactivada", Toast.LENGTH_LONG).show()
                        findNavController().navigate(R.id.loginFragment)
                    }
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}