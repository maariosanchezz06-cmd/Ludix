package com.mario.ludix.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mario.ludix.R
import com.mario.ludix.domain.Clip

class ClipMiniAdapter(
    private val clips: List<Clip>,
    private val onClipClick: (Clip) -> Unit
) : RecyclerView.Adapter<ClipMiniAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivThumbnail: ImageView = view.findViewById(R.id.ivThumbnailClip)
        val tvAutor: TextView = view.findViewById(R.id.tvAutorClipMini)
        val tvLikes: TextView = view.findViewById(R.id.tvLikesClipMini)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_clip_mini, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val clip = clips[position]

        holder.tvAutor.text = "@${clip.autor}"
        holder.tvLikes.text = "❤️ ${clip.likes}"

        // Cargar thumbnail del video con Glide
        if (clip.url.isNotEmpty()) {
            Glide.with(holder.itemView.context)
                .load(clip.url)
                .centerCrop()
                .placeholder(R.drawable.logo_ludix)
                .into(holder.ivThumbnail)
        }

        holder.itemView.setOnClickListener {
            onClipClick(clip)
        }
    }

    override fun getItemCount() = clips.size
}