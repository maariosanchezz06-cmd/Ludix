package com.mario.ludix.adapter

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mario.ludix.R
import com.mario.ludix.domain.Clip

class VideoAdapter(
    private val context: Context,
    private val listaVideos: List<Clip>
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val playersMap = mutableMapOf<Int, ExoPlayer>()

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playerView: PlayerView? = view.findViewById(R.id.playerView)
        val ivLike: ImageView? = view.findViewById(R.id.ivLike)
        val tvLikesCount: TextView? = view.findViewById(R.id.tvLikesCount)
        val ivShare: ImageView? = view.findViewById(R.id.ivShare)
        val ivComments: ImageView? = view.findViewById(R.id.ivComments)
        val tvCommentsCount: TextView? = view.findViewById(R.id.tvCommentsCount)
        val tvAutorClip: TextView = view.findViewById(R.id.tvAutorClip)
        val tvJuegoClip: TextView = view.findViewById(R.id.tvJuegoClip)
        val tvTituloClip: TextView = view.findViewById(R.id.tvTituloClip)
        val ivPerfilMini: ImageView = view.findViewById(R.id.ivPerfilMini)

        var player: ExoPlayer? = null
        var isLiked: Boolean = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_reel, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val clip = listaVideos[position]

        holder.tvAutorClip.text = "@${clip.autor}"
        holder.tvTituloClip.text = clip.titulo
        holder.tvLikesCount?.text = clip.likes.toString()

        // --- OPTIMIZACIÓN: Carga de imagen diferida ---
        if (clip.autorId.isNotEmpty()) {
            db.collection("usuarios").document(clip.autorId).get()
                .addOnSuccessListener { doc ->
                    if (doc.exists()) {
                        val avatar = doc.getString("imagen_perfil") ?: ""
                        if (avatar.isNotEmpty()) {
                            Glide.with(context).load(avatar).circleCrop().into(holder.ivPerfilMini)
                        }
                    }
                }
        }

        // --- LÓGICA DEL JUEGO ---
        val nombreJuego = clip.juego
        if (nombreJuego.isNotEmpty() && nombreJuego != "General" && nombreJuego != "sin_juego") {
            holder.tvJuegoClip.text = "🎮 $nombreJuego"
            holder.tvJuegoClip.visibility = View.VISIBLE
            holder.tvJuegoClip.setOnClickListener {
                if (clip.id_juego.isNotEmpty()) {
                    db.collection("juegos").document(clip.id_juego).get()
                        .addOnSuccessListener { doc ->
                            if (doc.exists()) {
                                val bundle = Bundle().apply {
                                    putString("juegoId", doc.id)
                                    putString("juegoTitulo", doc.getString("titulo"))
                                    putString("juegoGenero", doc.getString("genero"))
                                    putString("juegoImageUrl", doc.getString("imageUrl"))
                                }
                                holder.itemView.findNavController().navigate(R.id.gameDetailFragment, bundle)
                            }
                        }
                }
            }
        } else {
            holder.tvJuegoClip.visibility = View.GONE
        }

        // --- OPTIMIZACIÓN REPRODUCTOR: Crear solo si es necesario ---
        val player = ExoPlayer.Builder(context).build()
        holder.playerView?.player = player
        holder.player = player
        playersMap[position] = player

        if (clip.url.isNotEmpty()) {
            val mediaItem = MediaItem.fromUri(clip.url)
            player.setMediaItem(mediaItem)
            player.prepare()
            // No reproducir automáticamente todos, solo el primero o según el scroll
            player.playWhenReady = false 
            player.repeatMode = Player.REPEAT_MODE_ONE
        }

        // Contador comentarios
        db.collection("clips").document(clip.id).collection("comentarios")
            .addSnapshotListener { snapshots, e ->
                if (e != null || snapshots == null) return@addSnapshotListener
                holder.tvCommentsCount?.text = snapshots.size().toString()
            }

        // Like
        holder.ivLike?.setOnClickListener {
            val clipRef = db.collection("clips").document(clip.id)
            if (!holder.isLiked) {
                holder.ivLike.setImageResource(R.drawable.ic_heart_filled)
                holder.ivLike.setColorFilter(android.graphics.Color.RED)
                holder.isLiked = true
                clip.likes += 1
                holder.tvLikesCount?.text = clip.likes.toString()
                clipRef.update("likes", FieldValue.increment(1))
            } else {
                holder.ivLike.setImageResource(R.drawable.ic_heart_outline)
                holder.ivLike.setColorFilter(android.graphics.Color.WHITE)
                holder.isLiked = false
                clip.likes -= 1
                holder.tvLikesCount?.text = clip.likes.toString()
                clipRef.update("likes", FieldValue.increment(-1))
            }
        }

        holder.ivShare?.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, "🎮 ¡Mira este clip en Ludix! \nEnlace: ${clip.url}")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir clip vía..."))
        }

        holder.ivComments?.setOnClickListener {
            mostrarBottomSheetComentarios(clip)
        }
    }

    private fun mostrarBottomSheetComentarios(clip: Clip) {
        val bottomSheetDialog = BottomSheetDialog(context)
        val sheetView = LayoutInflater.from(context).inflate(R.layout.bottom_sheet_comments, null)
        bottomSheetDialog.setContentView(sheetView)
        val etComentario = sheetView.findViewById<android.widget.EditText>(R.id.etComentario)
        val btnEnviar = sheetView.findViewById<ImageView>(R.id.btnEnviarComentario)
        val rvComentarios = sheetView.findViewById<RecyclerView>(R.id.rvListaComentarios)
        rvComentarios.layoutManager = LinearLayoutManager(context)

        db.collection("clips").document(clip.id).collection("comentarios")
            .orderBy("timestamp")
            .get() // Usar get en lugar de addSnapshotListener para el BottomSheet inicial reduce carga
            .addOnSuccessListener { snapshots ->
                val listaComentarios = snapshots.map { doc -> 
                    Comentario(
                        id = doc.id,
                        texto = doc.getString("texto") ?: "",
                        autorId = doc.getString("autorId") ?: "",
                        timestamp = doc.getLong("timestamp") ?: 0
                    )
                }.toMutableList()
                rvComentarios.adapter = ComentarioAdapter(listaComentarios, clip.id)
            }

        btnEnviar.setOnClickListener {
            val texto = etComentario.text.toString().trim()
            if (texto.isNotEmpty()) {
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonimo"
                val data = hashMapOf<String, Any>(
                    "texto" to texto,
                    "autorId" to currentUserId,
                    "timestamp" to System.currentTimeMillis()
                )
                db.collection("clips").document(clip.id).collection("comentarios").add(data).addOnSuccessListener { etComentario.text.clear() }
            }
        }
        bottomSheetDialog.show()
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        val position = holder.absoluteAdapterPosition
        playersMap[position]?.release()
        playersMap.remove(position)
        holder.player = null
    }

    override fun getItemCount(): Int = listaVideos.size
}