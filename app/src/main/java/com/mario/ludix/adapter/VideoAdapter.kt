package com.mario.ludix.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
        // Usamos try-catch o verificamos nulos para evitar el cierre
        val playerView: PlayerView? = view.findViewById(R.id.playerView)
        val ivLike: ImageView? = view.findViewById(R.id.ivLike)
        val ivShare: ImageView? = view.findViewById(R.id.ivShare)
        val ivComments: ImageView? = view.findViewById(R.id.ivComments)
        val tvAutor: TextView? = view.findViewById(R.id.tvAutor)
        val tvTitulo: TextView? = view.findViewById(R.id.tvTitulo)

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

        // --- 1. SETEAR TEXTOS CON PROTECCIÓN ---
        holder.tvAutor?.text = clip.autor ?: "@usuario"
        holder.tvTitulo?.text = clip.titulo ?: "Sin descripción"

        // --- 2. CONFIGURAR REPRODUCTOR ---
        val player = ExoPlayer.Builder(context).build()
        holder.playerView?.player = player
        holder.player = player

        player.addListener(object : Player.Listener {
            override fun onPlayerError(error: PlaybackException) {
                Log.e("VideoError", "Error en el vídeo: ${error.message}")
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

        // --- 3. LÓGICA DEL BOTÓN LIKE ---
        holder.ivLike?.setOnClickListener {
            if (!holder.isLiked) {
                holder.ivLike.setImageResource(R.drawable.ic_heart_filled)
                holder.ivLike.setColorFilter(android.graphics.Color.RED)
                holder.isLiked = true
            } else {
                holder.ivLike.setImageResource(R.drawable.ic_heart_outline)
                holder.ivLike.setColorFilter(android.graphics.Color.WHITE)
                holder.isLiked = false
            }
        }
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.player?.release()
        holder.player = null
    }

    override fun getItemCount(): Int = listaVideos.size
}