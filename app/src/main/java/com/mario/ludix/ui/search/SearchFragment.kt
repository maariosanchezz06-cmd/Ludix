package com.mario.ludix.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.mario.ludix.R
import com.mario.ludix.adapter.JuegoAdapter
import com.mario.ludix.domain.Videojuego

class SearchFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var rvResultados: RecyclerView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val tvTitle = view.findViewById<TextView>(R.id.tvTitleSearch)
        // MAGIA: Pincel de degradado para el título
        val paint = tvTitle.paint
        val width = paint.measureText(tvTitle.text.toString())
        tvTitle.paint.shader = android.graphics.LinearGradient(
            0f, 0f, width, tvTitle.textSize,
            intArrayOf(android.graphics.Color.parseColor("#00E5FF"), android.graphics.Color.parseColor("#9D50BB")),
            null, android.graphics.Shader.TileMode.CLAMP
        )

        val etBuscador = view.findViewById<EditText>(R.id.etBuscador)
        rvResultados = view.findViewById(R.id.rvResultadosBusqueda)
        rvResultados.layoutManager = LinearLayoutManager(context)

        // Escuchamos cada vez que el usuario teclea algo
        etBuscador.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val texto = s.toString()
                if (texto.isNotEmpty()) {
                    buscarJuegos(texto)
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    private fun buscarJuegos(query: String) {
        // Buscamos en Firebase los juegos que coincidan con el texto
        db.collection("juegos")
            .orderBy("titulo")
            .startAt(query)
            .endAt(query + "\uf8ff")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.map { it.toObject(Videojuego::class.java) }
                rvResultados.adapter = JuegoAdapter(lista) { juego ->
                    Toast.makeText(context, "Clic en: ${juego.titulo}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}