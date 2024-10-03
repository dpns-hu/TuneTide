package com.tunetide.music.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaController
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import com.tunetide.music.Preferences.LastPlayPreference
import com.tunetide.music.R
import com.tunetide.music.databinding.FragmentNowPlayingBinding
import com.tunetide.music.model.Music
import com.tunetide.music.services.PlayerState

import com.tunetide.music.util.songPlayedOnce
import com.tunetide.music.util.toFormattedDuration
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class NowPlayingFrag : Fragment() {

    @Inject
    lateinit var lastPlayPreference: LastPlayPreference

    private var _binding: FragmentNowPlayingBinding? = null
    @Inject
    lateinit var playerState: PlayerState
    val binding get() = _binding
      @Inject
      lateinit var controllerFuture: ListenableFuture<MediaController>
    private lateinit var exoplayer: MediaController
    //variable name exoplayer is used, just to avoid refactoring all the code. Actually it is a mediaController.
    //this is initialized in the onStart

    //    private lateinit var musicService: MusicService
    private var isServiceBound = false
    private var currentSong = -1

    //    private lateinit var exoPlayer: ExoPlayer
    private val musicVm: MusicViewmodel by activityViewModels()
    private lateinit var currentMusicItem: Music
    private var progressUpdateHandler: Handler? = null
    private val UPDATE_INTERVAL_MILLIS = 1000L
    private var shuffleModeEnabled = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNowPlayingBinding.inflate(inflater, container, false)
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Timber.d("onViewCreated: ")
        if (!songPlayedOnce) {
            songPlayedOnce = true
        }
            isServiceBound = true

        exoplayer = controllerFuture.get()
        controllerFuture.addListener(
            {
                startProgressUpdateTask()
                updateUiData()
                setupPlayerUI()
                setUpSeekbarListener()
                setupExoplayerItemChangedListener()
                setRepeatButtonUi()
                updateShuffleButtonUi()
            },
            MoreExecutors.directExecutor()
        )
        setupCurrentMusicLD()
//        updateSeekbarProgress()
        backButton()

    }

    override fun onStart() {
        super.onStart()
//        controllerFuture = (activity as? MainActivity)?.controllerFuture!!
        exoplayer = controllerFuture.get()
        exoplayer.addListener(playerListener)
        controllerFuture.addListener(
            {
                startProgressUpdateTask()
                updateUiData()
                setupPlayerUI()
                setUpSeekbarListener()
                setupExoplayerItemChangedListener()
                setRepeatButtonUi()
                updateShuffleButtonUi()
            },
            MoreExecutors.directExecutor()
        )


    }

    private fun updateCurrentSongPosition(currentPosition: Int) {
        // Update UI with the current song position
        // For example, update a TextView with the current position
        binding?.queue?.text = currentPosition.toString()
    }

    @OptIn(UnstableApi::class)
    private fun setupExoplayerItemChangedListener() {
        val exoPlayer = controllerFuture.get()
        exoPlayer.addListener(object : Player.Listener {
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                super.onMediaItemTransition(mediaItem, reason)
                if (binding != null) {
                    updateUiData()
                    startProgressUpdateTask()
                }
                musicVm.updateCurrentPosition(exoPlayer.currentMediaItemIndex)
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                if (binding != null) {
                    updateUiData()
                    if (playbackState == ExoPlayer.STATE_READY) {

                        startProgressUpdateTask()

                    } else {
                        stopProgressUpdateTask()
                    }
                }
            }

            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playWhenReady && playbackState == Player.STATE_READY) {
                    // Media actually playing
                    if (binding != null) {
                        updateUiData()
                    }
                } else if (playWhenReady) {
                    // Might be idle (plays after prepare()), buffering (plays when data available), or ended (plays when seek away from end)

                } else {
                    // Player paused in any state
                    if (binding != null) {
                        updateUiData()
                    }
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                super.onIsPlayingChanged(isPlaying)
                updateUiData()
            }
        })

    }

    private fun startProgressUpdateTask() {
        stopProgressUpdateTask() // Stop any existing task to avoid multiple tasks running simultaneously
        progressUpdateHandler = Handler(Looper.getMainLooper())
        progressUpdateHandler?.post(object : Runnable {
            override fun run() {
                updateSeekbarProgress() // Update the SeekBar position
                progressUpdateHandler?.postDelayed(this, UPDATE_INTERVAL_MILLIS)
            }
        })
    }

    private fun stopProgressUpdateTask() {
        progressUpdateHandler?.removeCallbacksAndMessages(null) // Remove any pending update tasks
        progressUpdateHandler = null
    }

    // for call

    private fun setupPlayerUI() {
        val exoPlayer = controllerFuture.get()
        binding!!.playBut.setOnClickListener {
            if (isServiceBound) {

                if(musicVm.isSongSuppressed.value == true){
                    Toast.makeText(requireContext(), "Can't play during call", Toast.LENGTH_SHORT)
                } else if (exoPlayer.isPlaying()) {
                        exoPlayer.pause()

                        binding!!.playBut.setImageResource(R.drawable.play_button)
                    }
                else {
                        exoPlayer.play()
                        binding!!.playBut.setImageResource(R.drawable.pause_button)
                    }
            } else {
                // Handle the case where the service is not bound yet
                // You can show a message to the user or handle it based on your app's logic
                Toast.makeText(requireContext(), "service is not bound yet", Toast.LENGTH_SHORT)
                    .show()
            }
            binding!!.playBut.setBackgroundColor(resources.getColor(R.color.transparent))

        }

        binding!!.previousArrow.setOnClickListener {
            if (exoPlayer.hasPreviousMediaItem()) {
                exoPlayer.seekToPrevious()
                binding!!.seekbar.value = 0f
            }
            binding!!.previousArrow.setBackgroundColor(resources.getColor(R.color.transparent))
        }
        binding!!.nextArrow.setOnClickListener {
            if (exoPlayer.hasNextMediaItem()) {
                exoPlayer.seekToNext()
                binding!!.seekbar.value = 0f
            }
            binding!!.nextArrow.setBackgroundColor(resources.getColor(R.color.transparent))
        }

        binding!!.shuffleBut.setOnClickListener {
            toggleShuffle()
            binding!!.shuffleBut.setBackgroundColor(resources.getColor(R.color.transparent))
        }

        binding!!.repeatBut.setOnClickListener {
            toggleRepeatMode()
            binding!!.repeatBut.setBackgroundColor(resources.getColor(R.color.transparent))
        }
    }

    private fun toggleShuffle() {
        val exoPlayer = controllerFuture.get()
        exoPlayer.shuffleModeEnabled = !exoPlayer.shuffleModeEnabled
        shuffleModeEnabled = exoPlayer.shuffleModeEnabled

//        val message = if (shuffleModeEnabled) "Shuffle on" else "Shuffle off"
//        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()

        updateShuffleButtonUi()
    }

    private fun updateShuffleButtonUi() {
        val exoPlayer = controllerFuture.get()
        if (exoPlayer.shuffleModeEnabled) {
            binding!!.shuffleBut.setImageResource(R.drawable.shuffle)
        } else {
            binding!!.shuffleBut.setImageResource(R.drawable.shuffle_off)
        }
    }

    private fun toggleRepeatMode() {
        val exoPlayer = controllerFuture.get()
        val repeatMode = exoPlayer.repeatMode
        exoPlayer.repeatMode = when (repeatMode) {
            Player.REPEAT_MODE_OFF -> Player.REPEAT_MODE_ONE
            Player.REPEAT_MODE_ONE -> Player.REPEAT_MODE_ALL
            else -> Player.REPEAT_MODE_OFF
        }

        val message = when (exoPlayer.repeatMode) {
            Player.REPEAT_MODE_OFF -> "Repeat off"
            Player.REPEAT_MODE_ONE -> "Repeat one"
            Player.REPEAT_MODE_ALL -> "Repeat all"
            else -> ""
        }
//        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        setRepeatButtonUi()
    }

    private fun setRepeatButtonUi() {
        val exoPlayer = controllerFuture.get()
        val img = when (exoPlayer.repeatMode) {
            Player.REPEAT_MODE_ONE -> (R.drawable.repeat_one)
            Player.REPEAT_MODE_ALL -> (R.drawable.repeat_all)
            else -> (R.drawable.repeat_off)
        }
        binding!!.repeatBut.setImageResource(img)
    }

    public fun updateUiData() {
        if (binding != null) {
            try {
                if (exoplayer.isPlaying()) {
                    binding!!.playBut.setImageResource(R.drawable.pause_button)

                } else {
                    binding!!.playBut.setImageResource(R.drawable.play_button)

                }
                val musicMetadata = exoplayer.currentMediaItem?.mediaMetadata
                val ind = exoplayer.currentMediaItemIndex.toString()
                musicMetadata?.let {
                    binding?.let {
                        it.songTitle.text =
                            "${musicMetadata.title.toString()}"
//                        "${musicMetadata.title.toString().toFilenameWithoutExtension()}"
                        it.artist.text = "${musicMetadata.artist}"
                        if (musicMetadata.albumTitle != null) {
                            it.album.text = "• ${musicMetadata.albumTitle}"
                        } else {
                            it.album.visibility = View.GONE
                        }

                        musicVm.currentSongTitle.postValue(musicMetadata.title.toString())

                        it.totalDurationTv.text =
                            exoplayer.duration.toFormattedDuration(
                                isAlbum = false,
                                isSeekBar = true
                            )
                        it.seekbar.valueFrom = 0F
                        if (exoplayer.duration.toFloat() >= 0.0) {
                            it.seekbar.valueTo = exoplayer.duration.toFloat()
                        } else {
                            it.seekbar.valueTo = Float.MAX_VALUE
                        }
                        Glide.with(this.requireContext())
                            .load(musicMetadata.artworkUri)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .placeholder(R.drawable.no_music_icon)
                            .into(it.musicIconImgV)

                    }
                    setUpQueueValue()
                    updateLastPlayedSong()
                }
            } catch (e: Exception) {
                Timber.d("$e")
            }
        }

    }

    private fun updateLastPlayedSong() {
        lastPlayPreference.updateLastPlayed(exoplayer.currentMediaItemIndex)
    }

    fun setUpQueueValue() {
        val exoPlayer = controllerFuture.get()
        currentSong = exoPlayer.currentMediaItemIndex + 1
        val RemainingSong = musicVm.mediaItemForExoplayer().size

        binding?.queue?.text = "• ${currentSong.toString()}/${RemainingSong}"
    }

    private fun setUpSeekbarListener() {
        binding!!.seekbar.addOnChangeListener { _, value, fromUser ->
            lifecycleScope.launch {
                if (fromUser) {
                    controllerFuture.get().seekTo(value.toLong())
                }
//                 Update UI after seeking
//                updateSeekbarProgress()
            }
        }
    }

    private fun backButton() {
        binding!!.backButton.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    override fun onStop() {
        super.onStop()
        stopProgressUpdateTask()

    }

    private fun updateSeekbarProgress() {
        // Update seek bar and elapsed duration continuously
        try {
            val currentPosition = exoplayer.currentPosition.toFloat()
            if (currentPosition in binding!!.seekbar.valueFrom..binding!!.seekbar.valueTo) {
                binding!!.seekbar.value = currentPosition
            }

            binding!!.elapsedDurationTv.text =
                currentPosition.toLong().toFormattedDuration(isAlbum = false, isSeekBar = true)
        } catch (e: Exception) {
        }

    }

    private fun setupCurrentMusicLD() {
        musicVm.currentMusic.observe(viewLifecycleOwner) { music ->
            Timber.d("setupCurrentMusicLD")


        }
    }

    private val playerListener = object : Player.Listener {
        val handler = Handler(Looper.getMainLooper())
        override fun onIsPlayingChanged(isPlaying: Boolean) {
            super.onIsPlayingChanged(isPlaying)
            if(isPlaying){
                handler.post{
                    binding?.playBut?.setImageResource(R.drawable.pause_button)

                }
            }else{
                binding?.playBut?.setImageResource(R.drawable.pause_button)
            }
        }

        override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
            super.onPlaybackSuppressionReasonChanged(playbackSuppressionReason)
            Timber.tag("Searchwithme").d("This is me from suppressed")
            // Check if playback is suppressed due to audio focus loss

            if (playbackSuppressionReason == Player.PLAYBACK_SUPPRESSION_REASON_TRANSIENT_AUDIO_FOCUS_LOSS) {
                // Handle the situation when playback is suppressed (e.g., pause playback)
                // For example:
                Timber.tag("Searchwithme").d("Auido Suppressed Due to call")

             handler.post {

                    binding?.playBut?.setImageResource(R.drawable.play_button)
                }
                musicVm.isSongSuppressed.value = true

            }
            else {
               handler.post {

                    binding?.playBut?.setImageResource(R.drawable.pause_button)
                }
                musicVm.isSongSuppressed.value = false
                Timber.tag("Searchwithme").d("Auido back Due to call")

            }
        }
    }


    override fun onResume() {
        super.onResume()
        updateUiData()

    }

    override fun onDestroyView() {
        super.onDestroyView()
//        if (isServiceBound) {
//            requireActivity().unbindService(serviceConnection)
//            isServiceBound = false
//        }

        _binding = null
    }
}