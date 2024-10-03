package com.tunetide.music.ui

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.tunetide.music.R
import com.tunetide.music.databinding.SongListItemBinding
import com.tunetide.music.model.Music
import com.tunetide.music.util.toFilenameWithoutExtension


var selectedMusicItemPosition: Int? = null

class MusicAdapter(
    val context: Context,
    val onMusicItemClicked: (Music, Int) -> Unit
) :
    PagingDataAdapter<Music, MusicAdapter.ViewHolder>(
        DiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            SongListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        if (position == selectedMusicItemPosition) {
            holder.binding.constraintLay.requestFocus()
        }

        holder.binding.root.setOnClickListener {
            selectedMusicItemPosition = position
            currentItem?.let { onMusicItemClicked(it, position) }
        }

        currentItem?.let {
            Glide.with(context)
                .load(currentItem.thumbnailURI)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .placeholder(R.drawable.no_music_icon)
                .into(holder.binding.musicIconImgV)
            holder.bind(currentItem)
        }
    }

    class ViewHolder(val binding: SongListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Music) {
            binding.musicTitle.text = item.displayName.toFilenameWithoutExtension()
            binding.musicSubtitle.text = "${item.artist} - ${item.album}"
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
