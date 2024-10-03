package com.tunetide.music.ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tunetide.music.R
import com.tunetide.music.databinding.ArtistListItemBinding
import com.tunetide.music.model.Music

class ArtistAdapter(
    val context: Context,
    val onMusicItemClicked: (Music) -> Unit,
    val currentPosition: (Int) -> Unit
) :
    ListAdapter<Music, ArtistAdapter.ViewHolder>(
        DiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            ArtistListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        val isEvenPosition = position % 2 == 0
        holder.binding.title.setOnClickListener {
            onMusicItemClicked(currentItem)
            currentPosition(position)
//            holder.binding.constraintLay.setBackgroundResource(R.drawable.song_list_focus_shape)
        }

        holder.binding.iconImgStart.visibility =
            if (isEvenPosition) View.VISIBLE else View.GONE
        holder.binding.iconImgEnd.visibility = if (isEvenPosition) View.GONE else View.VISIBLE
        holder.binding.spacer8.visibility = if (isEvenPosition) View.GONE else View.VISIBLE

        val requestBuilder = Glide.with(context)
            .load(currentItem.thumbnailURI)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .placeholder(R.drawable.no_music_icon)

        requestBuilder.into(holder.binding.iconImgEnd)
        requestBuilder.into(holder.binding.iconImgEnd)

        holder.bind(currentItem)
    }

    class ViewHolder(val binding: ArtistListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Music) {
            binding.title.text = item.artist //TODO: need to change this later
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Music>() {
        override fun areItemsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem.title == newItem.title
        }
    }
}
