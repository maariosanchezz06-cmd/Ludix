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
import com.google.android.material.snackbar.Snackbar
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
            Snackbar.make(binding.root, "No seleccionaste ningún vídeo", Snackbar.LENGTH_SHORT).show()
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
            Snackbar.make(binding.root, "Atención: No has iniciado sesión", Snackbar.LENGTH_LONG).show()
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
                R.id.navigation_search -> { 
                    navController.navigate(R.id.navigation_search)
                    true 
                }
                R.id.navigation_profile -> { navController.navigate(R.id.navigation_profile); true }
                else -> false
            }
        }
    }

    private fun mostrarDialogoTitulo(videoUri: Uri) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_upload_video, null)
        
        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        dialog.window?.setBackgroundDrawable(android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))

        val etInput = dialogView.findViewById<android.widget.EditText>(R.id.etDialogInput)
        val btnSubmit = dialogView.findViewById<android.widget.Button>(R.id.btnDialogSubmit)

        btnSubmit.setOnClickListener {
            val titulo = etInput.text.toString().trim()
            if (titulo.isNotEmpty()) {
                subirVideoAFirebase(videoUri, titulo)
                dialog.dismiss()
            } else {
                Snackbar.make(
                    binding.root, 
                    "El título no puede estar vacío", 
                    Snackbar.LENGTH_SHORT
                ).show()
            }
        }
        dialog.show()
    }

    private fun subirVideoAFirebase(videoUri: Uri, tituloVideo: String) {
        // MENSAJE LIMPIO: Fondo oscuro, letras blancas
        val snackbar = Snackbar.make(binding.root, "Iniciando subida... 🚀", Snackbar.LENGTH_LONG)
        snackbar.view.background = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_bottom_nav)
        snackbar.setTextColor(android.graphics.Color.WHITE) // Letras blancas
        snackbar.show()

        val storageRef = Firebase.storage.getReferenceFromUrl("gs://ludix-56c7f.firebasestorage.app")
        val nombreArchivo = "videos/clip_${System.currentTimeMillis()}.mp4"
        val videoRef = storageRef.child(nombreArchivo)

        videoRef.putFile(videoUri)
            .addOnSuccessListener {
                videoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    guardarEnFirestore(downloadUri.toString(), tituloVideo)
                }
            }
            .addOnFailureListener { e -> 
                Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show() 
            }
    }

    private fun guardarEnFirestore(urlVideo: String, tituloVideo: String) {
        val db = Firebase.firestore
        val uidAutor = Firebase.auth.currentUser?.uid ?: "sin_id"

        db.collection("usuarios").document(uidAutor).get()
            .addOnSuccessListener { document ->
                val nombreReal = document.getString("nombre_usuario") ?: "Gamer"

                val nuevoVideo = hashMapOf(
                    "autor" to "@$nombreReal",
                    "autorId" to uidAutor,
                    "url" to urlVideo,
                    "titulo" to tituloVideo,
                    "likes" to 0,
                    "timestamp" to System.currentTimeMillis()
                )

                db.collection("clips").add(nuevoVideo)
                    .addOnSuccessListener {
                        // NUEVO MENSAJE LIMPIO DE VÍDEO PUBLICADO
                        val snackbar = Snackbar.make(binding.root, "¡Vídeo publicado por $nombreReal! 🎉", Snackbar.LENGTH_LONG)
                        snackbar.view.background = androidx.core.content.ContextCompat.getDrawable(this, R.drawable.bg_bottom_nav)
                        snackbar.setTextColor(android.graphics.Color.WHITE)
                        snackbar.show()
                    }
            }
    }
}