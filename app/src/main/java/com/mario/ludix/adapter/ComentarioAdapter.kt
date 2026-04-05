package com.mario.ludix.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mario.ludix.R

// Modelo de datos mejorado
data class Comentario(
    val id: String = "",           // ID del documento para poder borrarlo
    val texto: String = "",
    val autorId: String = "",
    val autorNombre: String = "",  // Nombre del autor (se carga después)
    val autorAvatar: String = "",  // Avatar del autor
    val timestamp: Long = 0
)

class ComentarioAdapter(
    private val lista: MutableList<Comentario>,
    private val clipId: String,
    private val onComentarioBorrado: () -> Unit = {}
) : RecyclerView.Adapter<ComentarioAdapter.ComentarioViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    class ComentarioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAvatar: ShapeableImageView = view.findViewById(R.id.ivAvatarComentario)
        val tvAutor: TextView = view.findViewById(R.id.tvAutorComentario)
        val tvTexto: TextView = view.findViewById(R.id.tvTextoComentario)
        val tvTiempo: TextView = view.findViewById(R.id.tvTiempoComentario)
        val ivBorrar: ImageView = view.findViewById(R.id.ivBorrarComentario)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComentarioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comentario, parent, false)
        return ComentarioViewHolder(view)
    }

    override fun onBindViewHolder(holder: ComentarioViewHolder, position: Int) {
        val comentario = lista[position]

        holder.tvTexto.text = comentario.texto
        holder.tvTiempo.text = calcularTiempoRelativo(comentario.timestamp)

        // Cargar datos del autor desde Firestore
        if (comentario.autorId.isNotEmpty()) {
            db.collection("usuarios").document(comentario.autorId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val nombre = doc.getString("nombre_usuario") ?: "Usuario"
                        val avatar = doc.getString("imagen_perfil") ?: ""

                        holder.tvAutor.text = "@$nombre"

                        if (avatar.isNotEmpty()) {
                            Glide.with(holder.itemView.context)
                                .load(avatar)
                                .circleCrop()
                                .placeholder(R.drawable.logo_ludix)
                                .into(holder.ivAvatar)
                        }
                    } else {
                        holder.tvAutor.text = "@Usuario"
                    }
                }
                .addOnFailureListener {
                    holder.tvAutor.text = "@Usuario"
                }
        } else {
            holder.tvAutor.text = "@Anónimo"
        }

        // Mostrar botón borrar solo si es mi comentario
        if (comentario.autorId == currentUserId) {
            holder.ivBorrar.visibility = View.VISIBLE
            holder.ivBorrar.setOnClickListener {
                borrarComentario(comentario.id, position)
            }
        } else {
            holder.ivBorrar.visibility = View.GONE
        }
    }

    private fun borrarComentario(comentarioId: String, position: Int) {
        if (comentarioId.isEmpty()) return

        db.collection("clips").document(clipId)
            .collection("comentarios").document(comentarioId)
            .delete()
            .addOnSuccessListener {
                // Eliminar de la lista local
                if (position < lista.size) {
                    lista.removeAt(position)
                    notifyItemRemoved(position)
                    notifyItemRangeChanged(position, lista.size)
                    onComentarioBorrado()
                }
            }
    }

    private fun calcularTiempoRelativo(timestamp: Long): String {
        val ahora = System.currentTimeMillis()
        val diferencia = ahora - timestamp

        val segundos = diferencia / 1000
        val minutos = segundos / 60
        val horas = minutos / 60
        val dias = horas / 24

        return when {
            segundos < 60 -> "Ahora"
            minutos < 60 -> "Hace ${minutos}min"
            horas < 24 -> "Hace ${horas}h"
            dias < 7 -> "Hace ${dias}d"
            else -> "Hace más de 1 semana"
        }
    }

    override fun getItemCount() = lista.size
}