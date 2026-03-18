package com.mario.ludix.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mario.ludix.R
import com.mario.ludix.domain.Videojuego

class JuegoAdapter(
    private val listaJuegos: List<Videojuego>,
    private val onJuegoClick: (Videojuego) -> Unit // NUEVO: Permite hacer clic en el juego
) : RecyclerView.Adapter<JuegoAdapter.JuegoViewHolder>() {

    class JuegoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
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

        // NUEVO: Al tocar la tarjeta del juego, avisamos a la pantalla principal
        holder.itemView.setOnClickListener {
            onJuegoClick(juego)
        }
    }

    override fun getItemCount(): Int = listaJuegos.size
}