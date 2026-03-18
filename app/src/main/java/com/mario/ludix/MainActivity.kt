package com.mario.ludix

import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mario.ludix.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            mostrarDialogoTitulo(uri)
        } else {
            Toast.makeText(this, "No seleccionaste ningún vídeo", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        navView.setupWithNavController(navController)

        val currentUser = Firebase.auth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "Atención: No has iniciado sesión", Toast.LENGTH_LONG).show()
            binding.root.post {
                try { navController.navigate(R.id.loginFragment) } 
                catch (e: Exception) {
                    try { navController.navigate(R.id.authFragment) } 
                    catch (e2: Exception) {}
                }
            }
        }

        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_add -> {
                    pickVideoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    false 
                }
                R.id.navigation_home -> { navController.navigate(R.id.navigation_home); true }
                R.id.navigation_search -> { true }
                R.id.navigation_profile -> { navController.navigate(R.id.navigation_profile); true }
                else -> false
            }
        }
    }

    private fun mostrarDialogoTitulo(videoUri: Uri) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Nuevo Clip")
        builder.setMessage("Escribe un título o descripción para tu vídeo:")
        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Subir") { dialog, _ ->
            val titulo = input.text.toString().trim()
            if (titulo.isNotEmpty()) {
                subirVideoAFirebase(videoUri, titulo)
            } else {
                Toast.makeText(this, "El título no puede estar vacío", Toast.LENGTH_SHORT).show()
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ -> dialog.cancel() }
        builder.show()
    }

    private fun subirVideoAFirebase(videoUri: Uri, tituloVideo: String) {
        Toast.makeText(this, "Iniciando subida... ten paciencia 🚀", Toast.LENGTH_SHORT).show()
        val storageRef = Firebase.storage.getReferenceFromUrl("gs://ludix-56c7f.firebasestorage.app")
        val nombreArchivo = "videos/clip_${System.currentTimeMillis()}.mp4"
        val videoRef = storageRef.child(nombreArchivo)

        videoRef.putFile(videoUri)
            .addOnSuccessListener {
                videoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    guardarEnFirestore(downloadUri.toString(), tituloVideo)
                }
            }
            .addOnFailureListener { e -> Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show() }
    }

    // LÓGICA NUEVA: Buscar el nombre real antes de guardar
    private fun guardarEnFirestore(urlVideo: String, tituloVideo: String) {
        val db = Firebase.firestore
        val uidAutor = Firebase.auth.currentUser?.uid ?: "sin_id"

        // Buscamos el nombre del usuario en la base de datos
        db.collection("usuarios").document(uidAutor).get()
            .addOnSuccessListener { document ->
                // Si lo encuentra, usa ese nombre; si no, pone "Gamer"
                val nombreReal = document.getString("nombre_usuario") ?: "Gamer"

                val nuevoVideo = hashMapOf(
                    "autor" to "@$nombreReal", // Usamos el nombre real arreglado
                    "autorId" to uidAutor,
                    "url" to urlVideo,
                    "titulo" to tituloVideo,
                    "likes" to 0,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("clips").add(nuevoVideo)
                    .addOnSuccessListener {
                        Toast.makeText(this, "¡Vídeo publicado por $nombreReal! 🎉", Toast.LENGTH_LONG).show()
                    }
            }
    }
}