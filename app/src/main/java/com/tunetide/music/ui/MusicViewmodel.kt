package com.tunetide.music.ui

import android.content.Context
import android.database.ContentObserver
import android.os.Handler
import android.provider.MediaStore
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.session.MediaController
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.google.common.util.concurrent.ListenableFuture
import com.tunetide.music.model.Music
import com.tunetide.music.util.ITEMS_PER_PAGE
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class MusicViewmodel @Inject constructor(
    @ApplicationContext val context: Context,
    private val musicQuery: MusicQuery,
    private val pagingRepository: PagingRepository
) :
    ViewModel() {

    // LiveData to track if a song has been selected.
    private val _songSelected = MutableLiveData<Boolean>(false)
    val songSelected: LiveData<Boolean> = _songSelected

    fun markSongAsSelected() {
        _songSelected.value = true
    }
   val isSongSuppressed =   MutableLiveData<Boolean>(false)
    val currentSongTitle = MutableLiveData<String>()

    private val _currentMusic = MutableLiveData<Music>()
    val currentMusic: LiveData<Music> =
        _currentMusic

    private val _currentPosition = MutableLiveData<Int>()
    val currentPosition: LiveData<Int> =
        _currentPosition

    private val _allMusicList = MutableLiveData<List<Music>>()
    val allMusicList: LiveData<List<Music>> =
        _allMusicList
      var isControllerInitialized = MutableLiveData<ListenableFuture<MediaController>>(null)


    fun markControllerInitialized(value:ListenableFuture<MediaController>){
        isControllerInitialized.postValue(value)
    }
    private var musicListForExoplayer = mutableListOf<Music>()

    val permissionGranted = MutableLiveData<Boolean>().apply { value = false }

    private val _contentObserverUpdated = MutableLiveData<String?>()
    val contentObserverUpdated: LiveData<String?> =
        _contentObserverUpdated

    fun updateCurrentMusic(music: Music) {
        Timber.d("updateCurrentMusic: Music:$music")
        _currentMusic.postValue(music)
    }

    fun updateCurrentPosition(position: Int) {
        _currentPosition.postValue(position)
    }

    val items: Flow<PagingData<Music>> = Pager(
        config = PagingConfig(
            pageSize = ITEMS_PER_PAGE,
            enablePlaceholders = false,
            initialLoadSize = 30
        ),
        pagingSourceFactory = { MusicPagingSource(musicQuery) }
    )
        .flow
        .cachedIn(viewModelScope)

    private val contentObserverM = object : ContentObserver(Handler()) {
        override fun onChange(selfChange: Boolean) {
            Timber.d("onChange: called")
            _contentObserverUpdated.postValue(null) //this is passed, just to notify, all the observers
        }
    }

    private fun registerContentObserver() {
        context.contentResolver.registerContentObserver(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            true,
            contentObserverM
        )
    }

    fun mediaItemForExoplayer(): MutableList<MediaItem> {
        musicListForExoplayer = musicQuery.queryAudioFilesWithPaging(0, 10000)
        val mediaItemsForExoplayer: MutableList<MediaItem> = ArrayList()
        for (music in musicListForExoplayer) {
            val metadata = MediaItem.Builder()
                .setUri(music.uri)
                .setMediaMetadata(getMediaMetaData(music))
                .build()
            mediaItemsForExoplayer.add(metadata)
        }
        return mediaItemsForExoplayer
    }

    private fun getMediaMetaData(music: Music): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(music.title)
            .setArtist(music.artist)
            .setArtworkUri(music.thumbnailURI)
            .setAlbumTitle(music.album)
            .build()
    }

    init {
        registerContentObserver()
    }

    override fun onCleared() {
        context.contentResolver.unregisterContentObserver(contentObserverM)
    }

}

var startIndexGlobal = 0
