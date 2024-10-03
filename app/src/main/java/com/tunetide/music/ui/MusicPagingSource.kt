package com.tunetide.music.ui

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.tunetide.music.model.Music
import javax.inject.Inject

class MusicPagingSource @Inject constructor(
    private val musicQuery: MusicQuery
) : PagingSource<Int, Music>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Music> {
        try {
            val startIndex = params.key ?: 0
            val musicList = musicQuery.queryAudioFilesWithPaging(startIndex, params.loadSize)

            return if (musicList.isNotEmpty()) {
                LoadResult.Page(
                    data = musicList,
                    prevKey = if (startIndex == 0) null else startIndex - params.loadSize,
                    nextKey = if (musicList.size < params.loadSize) null else startIndex + params.loadSize
                )
            } else {
                if (startIndex == 0) {
                    LoadResult.Error(Exception("No data available"))
                } else {
                    LoadResult.Page(
                        data = emptyList(),
                        prevKey = startIndex - params.loadSize,
                        nextKey = null
                    )
                }
            }
        } catch (e: Exception) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Music>): Int? {
        val anchorPage = state.closestPageToPosition(state.anchorPosition ?: 0)
        return 0//anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
    }
}