package com.tunetide.music.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tunetide.music.databinding.AlbumListItemBinding
import com.tunetide.music.model.Music

class AlbumAdapter :
    ListAdapter<Music, AlbumAdapter.ViewHolder>(
        DiffCallback
    ) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding =
            AlbumListItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val currentItem = getItem(position)
        val isEvenPosition = position % 2 == 0

        holder.binding.albumIconImgStart.visibility =
            if (isEvenPosition) View.VISIBLE else View.GONE
        holder.binding.albumIconImgEnd.visibility = if (isEvenPosition) View.GONE else View.VISIBLE
        holder.binding.spacer8.visibility = if (isEvenPosition) View.GONE else View.VISIBLE

        holder.bind(currentItem)
    }

    class ViewHolder(val binding: AlbumListItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Music) {
            binding.albumTitle.text = item.title
//            binding.albumIconImgStart.setImageResource(item.img)
//            binding.albumIconImgEnd.setImageResource(item.img)
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<Music>() {
        override fun areItemsTheSame(oldItem: Music, newItem: Music): Boolean {
            return false
            //todo:check this
            //return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Music, newItem: Music): Boolean {
            return oldItem.title == newItem.title
        }
    }
}
