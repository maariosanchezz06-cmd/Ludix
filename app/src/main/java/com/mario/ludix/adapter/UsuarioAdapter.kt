package com.mario.ludix.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.mario.ludix.R
import com.mario.ludix.domain.Usuario

class UsuarioAdapter(
    private val lista: List<Usuario>,
    private val onUserClick: (Usuario) -> Unit
) : RecyclerView.Adapter<UsuarioAdapter.UsuarioViewHolder>() {

    class UsuarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ShapeableImageView = view.findViewById(R.id.ivAvatarBusqueda)
        val tvNombre: TextView = view.findViewById(R.id.tvNombreUsuarioBusqueda)
        val tvBio: TextView = view.findViewById(R.id.tvBioBusqueda)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UsuarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_usuario, parent, false)
        return UsuarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: UsuarioViewHolder, position: Int) {
        val usuario = lista[position]
        holder.tvNombre.text = "@${usuario.nombre_usuario}"
        holder.tvBio.text = if (usuario.bio.isNotEmpty()) usuario.bio else "Sin biografía"

        if (usuario.imagen_perfil.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(usuario.imagen_perfil)
                .circleCrop()
                .placeholder(R.drawable.logo_ludix)
                .into(holder.ivAvatar)
        } else {
            holder.ivAvatar.setImageResource(R.drawable.logo_ludix)
        }

        holder.itemView.setOnClickListener { onUserClick(usuario) }
    }

    override fun getItemCount() = lista.size
}