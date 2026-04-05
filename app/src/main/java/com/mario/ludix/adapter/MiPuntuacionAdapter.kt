package com.mario.ludix.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mario.ludix.R

data class PuntuacionConJuego(
    val nombreJuego: String,
    val valor: Int
)

class MiPuntuacionAdapter(
    private val lista: List<PuntuacionConJuego>
) : RecyclerView.Adapter<MiPuntuacionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombre: TextView = view.findViewById(R.id.tvNombreJuegoPunt)
        val tvValor: TextView = view.findViewById(R.id.tvValorPuntuacion)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mi_puntuacion, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        holder.tvNombre.text = item.nombreJuego
        holder.tvValor.text = "⭐".repeat(item.valor)
    }

    override fun getItemCount() = lista.size
}