package com.tunetide.music.ui

import javax.inject.Inject

class PagingRepository @Inject constructor(private val musicPagingSource: MusicPagingSource) {
    fun musicPagingSource() = musicPagingSource
}