package com.mario.ludix

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.mario.ludix.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)

        // 1. ELIMINADA la línea de setupActionBarWithNavController (evita el crash)
        // 2. Vinculamos el menú de abajo con la navegación
        navView.setupWithNavController(navController)

        // 3. LOGICA PRO: Esconder la barra de abajo en Login y Registro
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.authFragment, R.id.loginFragment -> {
                    navView.visibility = View.GONE // Se esconde
                }
                else -> {
                    navView.visibility = View.VISIBLE // Se muestra en Home, etc.
                }
            }
        }
    }
}