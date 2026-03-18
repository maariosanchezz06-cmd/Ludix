package com.mario.ludix.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mario.ludix.R

// Modelo de datos rápido
data class Comentario(
    val texto: String = "",
    val autorId: String = ""
)

class ComentarioAdapter(private val lista: List<Comentario>) : RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder>() {

    class ComentarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTexto: TextView = view.findViewById(R.id.tvTextoComentario)
        val tvAutor: TextView = view.findViewById(R.id.tvAutorComentario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_comentario, parent, false)
        return ComentarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        val comentario = lista[position]
        holder.tvTexto.text = comentario.texto
        holder.tvAutor.text = "Usuario" // Más adelante lo conectaremos con su nombre real
    }

    override fun getItemCount() = lista.size
}