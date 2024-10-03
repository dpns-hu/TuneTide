package com.tunetide.music.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import androidx.media3.session.MediaController
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MediaButtonReceiver : BroadcastReceiver() {

    @Inject
    lateinit var controllerFuture: ListenableFuture<MediaController>
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == AudioManager.ACTION_AUDIO_BECOMING_NOISY) {
            // Check if musicService is playing before stopping playback
//            //todo solve this
//            if (controllerFuture.get().isPlaying()) {
//                // Assuming playbackState is a LiveData<Boolean> representing the playback state
////                musicService.playbackState.postValue(false)
//                controllerFuture.get().pause()

//            }
            //todo
        }
    }
}
