package com.mario.ludix.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.mario.ludix.R
import com.mario.ludix.domain.Clip

class VideoAdapter(
    private val context: Context,
    private val listaVideos: List<Clip>
) : RecyclerView.Adapter<VideoAdapter.VideoViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class VideoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val playerView: PlayerView? = view.findViewById(R.id.playerView)
        val ivLike: ImageView? = view.findViewById(R.id.ivLike)
        val tvLikesCount: TextView? = view.findViewById(R.id.tvLikesCount)
        val ivShare: ImageView? = view.findViewById(R.id.ivShare) // Botón compartir
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

        holder.tvAutor?.text = clip.autor
        holder.tvTitulo?.text = clip.titulo
        holder.tvLikesCount?.text = clip.likes.toString()

        // --- REPRODUCTOR ---
        val player = ExoPlayer.Builder(context).build()
        holder.playerView?.player = player
        holder.player = player

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

        // --- LÓGICA DE COMPARTIR (NUEVO) ---
        holder.ivShare?.setOnClickListener {
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "text/plain"
                // Mensaje que se enviará por WhatsApp/Telegram
                putExtra(Intent.EXTRA_TEXT, "🎮 ¡Mira este clip en Ludix! \nTítulo: ${clip.titulo}\nEnlace: ${clip.url}")
            }
            context.startActivity(Intent.createChooser(shareIntent, "Compartir clip vía..."))
        }

        // --- LÓGICA DEL LIKE ---
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
    }

    override fun onViewRecycled(holder: VideoViewHolder) {
        super.onViewRecycled(holder)
        holder.player?.release()
        holder.player = null
    }

    override fun getItemCount(): Int = listaVideos.size
}