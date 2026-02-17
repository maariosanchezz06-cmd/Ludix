package com.mario.ludix.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.mario.ludix.R
import com.mario.ludix.domain.Clip

class VideoAdapter(
    private val context: Context,
    private val listaVideos: List<Clip>
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playerView: PlayerView = view.findViewById(R.id.playerView)
        val ivLike: ImageView = view.findViewById(R.id.ivLike)
        val ivShare: ImageView = view.findViewById(R.id.ivShare)
        val ivComments: ImageView = view.findViewById(R.id.ivComments)
        val ivPerfil: ImageView = view.findViewById(R.id.ivPerfilMini) // Lo añadimos por si quieres cambiar la foto luego
        var player: ExoPlayer? = null

        // Estado local para el like
        var isLiked: Boolean = false
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_video_reel, parent, false)
        return VideoViewHolder(view)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        val clip = listaVideos[position]

        // 1. Configurar Reproductor (Uno nuevo para cada vídeo)
        val player = ExoPlayer.Builder(context).build()
        holder.playerView.player = player
        holder.player = player

        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("VideoError", "Error en el vídeo de ${clip.autor}: ${error.message}")
            }
        })

        try {
            if (clip.url.isNotEmpty()) {
                val mediaItem = MediaItem.fromUri(clip.url)
                player.setMediaItem(mediaItem)
                player.prepare()
                player.playWhenReady = true
                player.repeatMode = Player.REPEAT_MODE_ONE
            }
        } catch (e: Exception) {
            Log.e("VideoAdapter", "Error cargando URL: ${e.message}")
        }

        // --- 2. LÓGICA DEL BOTÓN LIKE ---
        // Reseteamos el estado visual por si el ViewHolder se recicla
        holder.isLiked = false
        holder.ivLike.setImageResource(R.drawable.ic_heart_outline)
        holder.ivLike.setColorFilter(android.graphics.Color.WHITE)

        holder.ivLike.setOnClickListener {
            if (!holder.isLiked) {
                // ACTIVAR LIKE: Corazón relleno y rojo
                holder.ivLike.setImageResource(R.drawable.ic_heart_filled)
                holder.ivLike.setColorFilter(android.graphics.Color.RED)
                holder.isLiked = true
            } else {
                // QUITAR LIKE: Corazón borde y blanco
                holder.ivLike.setImageResource(R.drawable.ic_heart_outline)
                holder.ivLike.setColorFilter(android.graphics.Color.WHITE)
                holder.isLiked = false
            }
        }

        // 3. Otros botones
        holder.ivShare.setOnClickListener {
            Toast.makeText(context, "Compartiendo el vídeo de ${clip.autor}", Toast.LENGTH_SHORT).show()
        }

        holder.ivComments.setOnClickListener {
            Toast.makeText(context, "Cargando comentarios...", Toast.LENGTH_SHORT).show()
        }

        holder.ivPerfil.setOnClickListener {
            Toast.makeText(context, "Perfil de ${clip.autor}", Toast.LENGTH_SHORT).show()
        }
    }

    // Limpieza de memoria fundamental para que no se pete la app
    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.player?.release()
        holder.player = null
    }

    override fun getItemCount(): Int = listaVideos.size
}