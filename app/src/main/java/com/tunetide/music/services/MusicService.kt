package com.tunetide.music.service

import android.content.Intent
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.tunetide.music.services.MediaButtonReceiver
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class MusicService : MediaSessionService() {
    @Inject
    lateinit var mediaSession: MediaSession
      private var isSongPlaying:Boolean? = false
    private val stateBuilder = PlaybackStateCompat.Builder()
        .setActions(
            PlaybackStateCompat.ACTION_PLAY
                    or PlaybackStateCompat.ACTION_PLAY_PAUSE
                    or PlaybackStateCompat.ACTION_STOP
                    or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
                    or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
        )

    private val mediaButtonReceiver = MediaButtonReceiver()
    override fun onCreate() {
        super.onCreate()
        Timber.d("Music service created")
//        val filter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
//        registerReceiver(mediaButtonReceiver, filter)

    }

        override fun onTaskRemoved(rootIntent: Intent?) {
//            mediaSession?.let {
//                val player = mediaSession!!.player
//                if (player.playWhenReady) {
//                }
//            }
//            stopSelf()
        }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? =
        mediaSession

    override fun onDestroy() {
        Timber.d("Music service Destroyed")
        mediaSession?.run {
            player.release()
            release()
        }
        super.onDestroy()
    }

}