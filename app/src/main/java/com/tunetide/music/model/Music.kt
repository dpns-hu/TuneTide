package com.tunetide.music.model

import android.net.Uri
import com.tunetide.music.R

class Music(
    val id: Long,
    val img: Int = (R.drawable.no_music_icon),
    val title: String = "",
    val displayName: String = "",
    val artist: String = "",
    val album: String = "",
    val uri: Uri,
    val duration: Long = 0,
    val size: Int,
    val thumbnailURI: Uri
)