package com.mario.ludix.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mario.ludix.R
import com.mario.ludix.domain.Videojuego // ¡CORREGIDO! Apunta a tu carpeta real

class JuegoAdapter(private val listaJuegos: List<Videojuego>) :
    RecyclerView.Adapter<JuegoAdapter.JuegoViewHolder>() {

    class JuegoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        // Asegúrate de que estos IDs existen en tu item_juego.xml
        // Si usaste otros nombres, avísame.
        val titulo: TextView = view.findViewById(R.id.tvTituloJuego)
        val genero: TextView = view.findViewById(R.id.tvGenero)
        val puntuacion: TextView = view.findViewById(R.id.tvPuntuacion)
        val portada: ImageView = view.findViewById(R.id.ivPortada)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): JuegoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_juego, parent, false)
        return JuegoViewHolder(view)
    }

    override fun onBindViewHolder(holder: JuegoViewHolder, position: Int) {
        val juego = listaJuegos[position]

        holder.titulo.text = juego.titulo
        holder.genero.text = juego.genero
        holder.puntuacion.text = "⭐ ${juego.puntuacion}"

        if (juego.imageUrl.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(juego.imageUrl)
                .centerCrop()
                .placeholder(R.drawable.logo_ludix)
                .into(holder.portada)
        }
    }

    override fun getItemCount(): Int = listaJuegos.size
}