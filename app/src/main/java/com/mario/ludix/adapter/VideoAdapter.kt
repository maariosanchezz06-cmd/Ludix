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

        // 1. Configurar Reproductor
        val player = ExoPlayer.Builder(context).build()
        holder.playerView.player = player
        holder.player = player

        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("VideoError", "Error: ${error.message}")
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
            Log.e("VideoAdapter", "Error URL: ${e.message}")
        }

        // --- 2. LÓGICA DEL BOTÓN LIKE (ME GUSTA) ---
        holder.ivLike.setOnClickListener {
            if (!holder.isLiked) {
                // Si NO tiene like -> Poner relleno rojo
                holder.ivLike.setImageResource(R.drawable.ic_heart_filled)
                holder.ivLike.setColorFilter(null) // Quitamos cualquier tinte para que se vea el rojo del XML
                holder.isLiked = true
                // Opcional: Toast.makeText(context, "¡Te gusta!", Toast.LENGTH_SHORT).show()
            } else {
                // Si YA tiene like -> Volver al borde blanco
                holder.ivLike.setImageResource(R.drawable.ic_heart_outline)
                // Forzamos el color blanco por si acaso
                holder.ivLike.setColorFilter(android.graphics.Color.WHITE)
                holder.isLiked = false
            }
        }

        // 3. Otros botones (Solo para avisar que funcionan)
        holder.ivShare.setOnClickListener {
            Toast.makeText(context, "Compartiendo vídeo...", Toast.LENGTH_SHORT).show()
        }

        holder.ivComments.setOnClickListener {
            Toast.makeText(context, "Abriendo comentarios...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.player?.release()
        holder.player = null
    }

    override fun getItemCount(): Int = listaVideos.size
}