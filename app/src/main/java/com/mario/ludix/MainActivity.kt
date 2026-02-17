package com.mario.ludix

import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.mario.ludix.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    // 1. LANZADOR DE GALERÍA: Se activa al elegir el vídeo
    private val pickVideoLauncher = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            // Si el usuario elige un vídeo, llamamos a la función de subida
            subirVideoAFirebase(uri)
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

        // Configuración básica de navegación
        navView.setupWithNavController(navController)

        // 2. CONTROL DEL BOTÓN "+" Y NAVEGACIÓN
        navView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_add -> {
                    // Abrir galería solo para vídeos
                    pickVideoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly))
                    false // 'false' para que el icono "+" no se quede marcado como seleccionado
                }
                R.id.navigation_home -> {
                    navController.navigate(R.id.navigation_home)
                    true
                }
                R.id.navigation_search -> {
                    // navController.navigate(R.id.navigation_search) // Descomenta si tienes esta pantalla
                    true
                }
                R.id.navigation_profile -> {
                    navController.navigate(R.id.navigation_profile)
                    true
                }
                else -> false
            }
        }
    }

    // 3. FUNCIÓN PARA SUBIR AL STORAGE (NUBE)
    private fun subirVideoAFirebase(videoUri: Uri) {
        Toast.makeText(this, "Iniciando subida... ten paciencia 🚀", Toast.LENGTH_SHORT).show()

        // IMPORTANTE: Usamos la URL exacta de tu bucket de Firebase
        val storageRef = Firebase.storage.getReferenceFromUrl("gs://ludix-56c7f.firebasestorage.app")

        // Creamos un nombre único basado en el tiempo actual para evitar duplicados
        val nombreArchivo = "videos/clip_${System.currentTimeMillis()}.mp4"
        val videoRef = storageRef.child(nombreArchivo)

        videoRef.putFile(videoUri)
            .addOnSuccessListener {
                // Si sube bien, pedimos el enlace (URL) público
                videoRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    guardarEnFirestore(downloadUri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al subir a la nube: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }

    // 4. FUNCIÓN PARA GUARDAR EL ENLACE EN LA BASE DE DATOS (FIRESTORE)
    private fun guardarEnFirestore(urlVideo: String) {
        val db = Firebase.firestore

        // Datos que guardaremos de cada vídeo
        val nuevoVideo = hashMapOf(
            "autor" to "Mario User",
            "url" to urlVideo,
            "likes" to 0
        )

        db.collection("clips")
            .add(nuevoVideo)
            .addOnSuccessListener {
                Toast.makeText(this, "¡Vídeo publicado con éxito! 🎉", Toast.LENGTH_LONG).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error al guardar en la base de datos: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}