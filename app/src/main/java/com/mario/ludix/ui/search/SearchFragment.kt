package com.mario.ludix.ui.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.firestore.FirebaseFirestore
import com.mario.ludix.R
import com.mario.ludix.adapter.JuegoAdapter
import com.mario.ludix.adapter.UsuarioAdapter
import com.mario.ludix.domain.Usuario
import com.mario.ludix.domain.Videojuego

class SearchFragment : Fragment() {

    private val db = FirebaseFirestore.getInstance()
    private lateinit var rvResultados: RecyclerView
    private lateinit var etBuscador: EditText
    private lateinit var cgSearchType: ChipGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val tvTitle = view.findViewById<TextView>(R.id.tvTitleSearch)
        aplicarGradiente(tvTitle)

        etBuscador = view.findViewById(R.id.etBuscador)
        cgSearchType = view.findViewById(R.id.cgSearchType)
        rvResultados = view.findViewById(R.id.rvResultadosBusqueda)
        rvResultados.layoutManager = LinearLayoutManager(context)

        // 1. Búsqueda automática mientras escribes
        etBuscador.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                realizarBusqueda(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 2. Búsqueda al pulsar ENTER en el teclado
        etBuscador.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                realizarBusqueda(etBuscador.text.toString())
                true
            } else {
                false
            }
        }

        // 3. Cambiar búsqueda al cambiar el Chip (Juegos/Usuarios)
        cgSearchType.setOnCheckedStateChangeListener { _, _ ->
            realizarBusqueda(etBuscador.text.toString())
        }
    }

    private fun realizarBusqueda(query: String) {
        if (query.isEmpty()) {
            rvResultados.adapter = null
            return
        }

        val esBusquedaJuegos = view?.findViewById<Chip>(R.id.chipSearchJuegos)?.isChecked == true
        
        if (esBusquedaJuegos) {
            buscarJuegos(query)
        } else {
            buscarUsuarios(query)
        }
    }

    private fun buscarJuegos(query: String) {
        val queryLower = query.lowercase()
        
        db.collection("juegos")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.mapNotNull { doc ->
                    val juego = doc.toObject(Videojuego::class.java)
                    juego.id = doc.id
                    if (juego.titulo.lowercase().contains(queryLower)) juego else null
                }
                rvResultados.adapter = JuegoAdapter(lista) { juego ->
                    val bundle = Bundle().apply {
                        putString("juegoId", juego.id)
                        putString("juegoTitulo", juego.titulo)
                        putString("juegoGenero", juego.genero)
                        putString("juegoImageUrl", juego.imageUrl)
                    }
                    findNavController().navigate(R.id.gameDetailFragment, bundle)
                }
            }
    }

    private fun buscarUsuarios(query: String) {
        val queryLower = query.lowercase()
        
        db.collection("usuarios")
            .get()
            .addOnSuccessListener { result ->
                val lista = result.mapNotNull { doc ->
                    val user = doc.toObject(Usuario::class.java)
                    if (user.nombre_usuario.lowercase().contains(queryLower)) user else null
                }
                rvResultados.adapter = UsuarioAdapter(lista) { usuario ->
                    Toast.makeText(context, "Perfil de @${usuario.nombre_usuario}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun aplicarGradiente(tv: TextView) {
        val paint = tv.paint
        val width = paint.measureText(tv.text.toString())
        tv.paint.shader = android.graphics.LinearGradient(
            0f, 0f, width, tv.textSize,
            intArrayOf(android.graphics.Color.parseColor("#00E5FF"), android.graphics.Color.parseColor("#9D50BB")),
            null, android.graphics.Shader.TileMode.CLAMP
        )
    }
}